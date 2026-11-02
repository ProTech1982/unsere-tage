package com.unseretage.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Sucht auf GitHub nach einer neueren Version und oeffnet deren Seite im
 * Browser. Die App laedt und installiert absichtlich nichts selbst: die
 * dafuer noetige Berechtigung ist dasselbe Muster, das Schadsoftware
 * benutzt, und laesst Play Protect warnen.
 */
object Updater {

    const val REPO = "ProTech1982/unsere-tage"
    private const val API = "https://api.github.com/repos/$REPO/releases/latest"
    private const val RELEASES_PAGE = "https://github.com/$REPO/releases/latest"

    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    sealed class CheckResult {
        /** Eine neuere Version liegt bereit. */
        data class Available(val build: Int, val name: String, val pageUrl: String) : CheckResult()

        /** Die installierte Version ist die neueste. */
        data object UpToDate : CheckResult()

        /** Das Repository ist privat oder nicht erreichbar. */
        data class NeedsBrowser(val reason: String) : CheckResult()

        data class Failed(val reason: String) : CheckResult()
    }

    /** Die sichtbare Versionsnummer, z. B. "1.0". */
    fun installedName(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    }.getOrDefault("?")

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

            val page = json.optString("html_url").ifBlank { RELEASES_PAGE }
            val name = json.optString("name").ifBlank { tag }
            return CheckResult.Available(build, name, page)
        } finally {
            connection.disconnect()
        }
    }

    /** Oeffnet die Release-Seite im Browser; dort laedt und installiert der Nutzer. */
    fun openReleasesPage(context: Context, url: String? = null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url ?: RELEASES_PAGE))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
