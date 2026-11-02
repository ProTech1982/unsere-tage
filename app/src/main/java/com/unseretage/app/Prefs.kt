package com.unseretage.app

import android.content.Context
import java.time.LocalDate

/** Alle Einstellungen der App an einem Ort. */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("unsere_tage", Context.MODE_PRIVATE)

    var nameSelf: String
        get() = sp.getString(KEY_NAME_SELF, "") ?: ""
        set(v) = sp.edit().putString(KEY_NAME_SELF, v).apply()

    var namePartner: String
        get() = sp.getString(KEY_NAME_PARTNER, "") ?: ""
        set(v) = sp.edit().putString(KEY_NAME_PARTNER, v).apply()

    var meetDate: LocalDate?
        get() = readDate(KEY_MEET)
        set(v) = writeDate(KEY_MEET, v)

    var weddingDate: LocalDate?
        get() = readDate(KEY_WEDDING)
        set(v) = writeDate(KEY_WEDDING, v)

    var notifyHour: Int
        get() = sp.getInt(KEY_HOUR, 9)
        set(v) = sp.edit().putInt(KEY_HOUR, v).apply()

    var notifyMinute: Int
        get() = sp.getInt(KEY_MINUTE, 0)
        set(v) = sp.edit().putInt(KEY_MINUTE, v).apply()

    /** 0 = keine Vorwarnung, sonst Anzahl Tage vorher. */
    var preWarnDays: Int
        get() = sp.getInt(KEY_PREWARN, 7)
        set(v) = sp.edit().putInt(KEY_PREWARN, v).apply()

    var milestonesEnabled: Boolean
        get() = sp.getBoolean(KEY_MILESTONES, true)
        set(v) = sp.edit().putBoolean(KEY_MILESTONES, v).apply()

    var monthlyEnabled: Boolean
        get() = sp.getBoolean(KEY_MONTHLY, false)
        set(v) = sp.edit().putBoolean(KEY_MONTHLY, v).apply()

    /** Uri des gewaehlten Fotos, als String gespeichert. */
    var photoUri: String?
        get() = sp.getString(KEY_PHOTO_URI, null)
        set(v) {
            val e = sp.edit()
            if (v == null) e.remove(KEY_PHOTO_URI) else e.putString(KEY_PHOTO_URI, v)
            e.apply()
        }

    var photoStyle: PhotoStyle
        get() = PhotoStyle.fromKey(sp.getString(KEY_PHOTO_STYLE, null))
        set(v) = sp.edit().putString(KEY_PHOTO_STYLE, v.key).apply()

    /** Eckenradius der Kacheln in dp. */
    var cardCornerDp: Int
        get() = sp.getInt(KEY_CARD_CORNER, 24)
        set(v) = sp.edit().putInt(KEY_CARD_CORNER, v).apply()

    /** Deckkraft der Kacheln in Prozent, 30..100. */
    var cardOpacity: Int
        get() = sp.getInt(KEY_CARD_OPACITY, 100)
        set(v) = sp.edit().putInt(KEY_CARD_OPACITY, v.coerceIn(30, 100)).apply()

    /** Akzentfarbe als ARGB-Wert. */
    var accentColor: Int
        get() = sp.getInt(KEY_ACCENT, Theming.DEFAULT_ACCENT)
        set(v) = sp.edit().putInt(KEY_ACCENT, v).apply()

    val isConfigured: Boolean
        get() = meetDate != null || weddingDate != null

    private fun readDate(key: String): LocalDate? {
        val epochDay = sp.getLong(key, Long.MIN_VALUE)
        return if (epochDay == Long.MIN_VALUE) null else LocalDate.ofEpochDay(epochDay)
    }

    private fun writeDate(key: String, value: LocalDate?) {
        val editor = sp.edit()
        if (value == null) editor.remove(key) else editor.putLong(key, value.toEpochDay())
        editor.apply()
    }

    private companion object {
        const val KEY_NAME_SELF = "name_self"
        const val KEY_NAME_PARTNER = "name_partner"
        const val KEY_MEET = "meet_date"
        const val KEY_WEDDING = "wedding_date"
        const val KEY_HOUR = "notify_hour"
        const val KEY_MINUTE = "notify_minute"
        const val KEY_PREWARN = "prewarn_days"
        const val KEY_MILESTONES = "milestones"
        const val KEY_MONTHLY = "monthly"
        const val KEY_PHOTO_URI = "photo_uri"
        const val KEY_PHOTO_STYLE = "photo_style"
        const val KEY_CARD_CORNER = "card_corner"
        const val KEY_CARD_OPACITY = "card_opacity"
        const val KEY_ACCENT = "accent_color"
    }
}
