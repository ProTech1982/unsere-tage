package com.unseretage.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Sucht auf GitHub nach einer neueren Version, laedt sie herunter und
 * uebergibt sie an Androids Installationsdialog.
 */
object Updater {

    const val REPO = "ProTech1982/unsere-tage"
    private const val API = "https://api.github.com/repos/$REPO/releases/latest"
    private const val RELEASES_PAGE = "https://github.com/$REPO/releases/latest"

    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    sealed class CheckResult {
        /** Eine neuere Version liegt bereit. */
        data class Available(val build: Int, val name: String, val apkUrl: String) : CheckResult()

        /** Die installierte Version ist die neueste. */
        data object UpToDate : CheckResult()

        /** Das Repository ist privat oder nicht erreichbar. */
        data class NeedsBrowser(val reason: String) : CheckResult()

        data class Failed(val reason: String) : CheckResult()
    }

    fun installedBuild(context: Context): Int = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    }.getOrDefault(0)

    fun check(context: Context, onResult: (CheckResult) -> Unit) {
        val installed = installedBuild(context)
        worker.execute {
            val result = runCatching { fetchLatest(installed) }
                .getOrElse { CheckResult.Failed("Keine Verbindung zu GitHub") }
            main.post { onResult(result) }
        }
    }

    private fun fetchLatest(installed: Int): CheckResult {
        val connection = (URL(API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "UnsereTage-App")
        }

        try {
            when (connection.responseCode) {
                200 -> Unit
                401, 403, 404 -> return CheckResult.NeedsBrowser(
                    "Das Repository ist privat. Die Seite laesst sich im Browser oeffnen."
                )
                else -> return CheckResult.Failed("GitHub antwortete mit ${connection.responseCode}")
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name")
            val build = tag.substringAfterLast('-').toIntOrNull()
                ?: return CheckResult.Failed("Versionsnummer nicht erkannt")

            if (build <= installed) return CheckResult.UpToDate

            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
            }
            val url = apkUrl ?: return CheckResult.NeedsBrowser("Keine APK im Release gefunden.")
            val name = json.optString("name").ifBlank { tag }
            return CheckResult.Available(build, name, url)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Laedt die APK und oeffnet den Installationsdialog.
     * [onProgress] liefert 0..100, oder -1 wenn die Groesse unbekannt ist.
     */
    fun downloadAndInstall(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val appContext = context.applicationContext
        worker.execute {
            try {
                val file = download(appContext, apkUrl) { percent -> main.post { onProgress(percent) } }
                main.post { startInstall(appContext, file, onError) }
            } catch (e: Exception) {
                main.post { onError(e.message ?: "Download fehlgeschlagen") }
            }
        }
    }

    private fun download(context: Context, apkUrl: String, onProgress: (Int) -> Unit): File {
        val directory = File(context.cacheDir, "update").apply {
            if (!exists()) mkdirs()
        }
        val target = File(directory, "UnsereTage.apk")
        if (target.exists()) target.delete()

        var url = URL(apkUrl)
        var redirects = 0
        var connection = open(url)

        // GitHub leitet auf einen Speicher-Server um.
        while (connection.responseCode in 301..308 && redirects < 5) {
            val location = connection.getHeaderField("Location") ?: break
            connection.disconnect()
            url = URL(url, location)
            connection = open(url)
            redirects++
        }

        if (connection.responseCode != 200) {
            connection.disconnect()
            throw IllegalStateException("Server antwortete mit ${connection.responseCode}")
        }

        val total = connection.contentLength.toLong()
        var read = 0L
        connection.inputStream.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(16 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    read += count
                    onProgress(if (total > 0) ((read * 100) / total).toInt() else -1)
                }
            }
        }
        connection.disconnect()
        return target
    }

    private fun open(url: URL): HttpURLConnection =
        (url.openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = false
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("User-Agent", "UnsereTage-App")
        }

    private fun startInstall(context: Context, file: File, onError: (String) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settings = Intent(
                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(settings) }
            onError("Bitte die Installation aus dieser App einmalig erlauben und erneut tippen.")
            return
        }

        val uri = runCatching {
            FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
        }.getOrElse {
            onError("Datei konnte nicht freigegeben werden")
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { onError("Installationsdialog liess sich nicht oeffnen") }
    }

    fun openReleasesPage(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_PAGE))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
