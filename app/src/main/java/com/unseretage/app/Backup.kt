package com.unseretage.app

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/**
 * Sicherung und Wiederherstellung aller Einstellungen als JSON-Datei.
 *
 * Die Datei wird ueber den Android-Dateiauswahldialog an einen frei
 * waehlbaren Ort geschrieben und ueberlebt deshalb eine Neuinstallation.
 */
object Backup {

    private const val PREFS_NAME = "unsere_tage"
    private const val FORMAT = "unsere-tage-backup"
    private const val VERSION = 1

    /** Das Foto wird nur als Verweis gespeichert; nach einer Neuinstallation ist er ungueltig. */
    private const val KEY_PHOTO_URI = "photo_uri"

    fun suggestedName(): String {
        val d = java.time.LocalDate.now()
        return "unsere-tage-sicherung-$d.json"
    }

    fun write(context: Context, uri: Uri): Result<Int> = runCatching {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val items = JSONArray()
        for ((key, value) in sp.all) {
            val (type, json) = when (value) {
                is Boolean -> "boolean" to value
                is Int -> "int" to value
                is Long -> "long" to value
                is Float -> "float" to value.toDouble()
                is String -> "string" to value
                is Set<*> -> "set" to JSONArray(value.map { it.toString() })
                else -> continue
            }
            items.put(JSONObject().put("k", key).put("t", type).put("v", json))
        }
        val root = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("erstellt", java.time.LocalDateTime.now().toString())
            .put("eintraege", items)
        context.contentResolver.openOutputStream(uri, "wt").use { out ->
            requireNotNull(out) { "Datei konnte nicht geschrieben werden" }
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }
        items.length()
    }

    /** Liest die Datei ein und ersetzt die vorhandenen Einstellungen. Gibt die Anzahl der Werte zurueck. */
    fun restore(context: Context, uri: Uri): Result<Int> = runCatching {
        val text = context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Datei konnte nicht gelesen werden" }
            input.readBytes().toString(Charsets.UTF_8)
        }
        val root = JSONObject(text)
        require(root.optString("format") == FORMAT) { "Das ist keine Sicherung von Unsere Tage." }
        require(root.optInt("version", 0) <= VERSION) { "Die Sicherung stammt von einer neueren App-Version." }
        val items = root.getJSONArray("eintraege")

        // Erst alles pruefen, dann schreiben - eine kaputte Datei darf nichts zerstoeren.
        val editor = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.clear()
        var count = 0
        for (i in 0 until items.length()) {
            val o = items.getJSONObject(i)
            val key = o.getString("k")
            if (key == KEY_PHOTO_URI) continue
            when (o.getString("t")) {
                "boolean" -> editor.putBoolean(key, o.getBoolean("v"))
                "int" -> editor.putInt(key, o.getInt("v"))
                "long" -> editor.putLong(key, o.getLong("v"))
                "float" -> editor.putFloat(key, o.getDouble("v").toFloat())
                "string" -> editor.putString(key, o.getString("v"))
                "set" -> {
                    val arr = o.getJSONArray("v")
                    editor.putStringSet(key, (0 until arr.length()).map { arr.getString(it) }.toSet())
                }
                else -> continue
            }
            count++
        }
        check(editor.commit()) { "Einstellungen konnten nicht gespeichert werden" }
        count
    }
}
