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

    /** Alter Schalter aus Version 1.0, dient jetzt als Vorgabe fuer die Tausender. */
    private val legacyMilestones: Boolean
        get() = sp.getBoolean(KEY_MILESTONES, true)

    var milestoneHundreds: Boolean
        get() = sp.getBoolean(KEY_MS_HUNDRED, false)
        set(v) = sp.edit().putBoolean(KEY_MS_HUNDRED, v).apply()

    var milestoneThousands: Boolean
        get() = sp.getBoolean(KEY_MS_THOUSAND, legacyMilestones)
        set(v) = sp.edit().putBoolean(KEY_MS_THOUSAND, v).apply()

    var milestoneRepdigits: Boolean
        get() = sp.getBoolean(KEY_MS_REPDIGIT, true)
        set(v) = sp.edit().putBoolean(KEY_MS_REPDIGIT, v).apply()

    /** "light", "dark" oder "system". */
    var themeMode: String
        get() = sp.getString(KEY_THEME_MODE, "system") ?: "system"
        set(v) = sp.edit().putString(KEY_THEME_MODE, v).apply()

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

    /* --- Muster im Kopfbereich --- */

    var headerPattern: Pattern
        get() = Pattern.fromKey(sp.getString(KEY_HEADER_PATTERN, null))
        set(v) = sp.edit().putString(KEY_HEADER_PATTERN, v.key).apply()

    var headerPatternColor: Int
        get() = sp.getInt(KEY_HEADER_PATTERN_COLOR, PatternColors.DEFAULT_ON_HEADER)
        set(v) = sp.edit().putInt(KEY_HEADER_PATTERN_COLOR, v).apply()

    /** Staerke des Musters in Prozent, 5..100, immer in Schritten von 5. */
    var headerPatternStrength: Int
        get() = snapStrength(sp.getInt(KEY_HEADER_PATTERN_STRENGTH, 20))
        set(v) = sp.edit().putInt(KEY_HEADER_PATTERN_STRENGTH, snapStrength(v)).apply()

    /* --- Muster im Seitenhintergrund --- */

    var backgroundPattern: Pattern
        get() = Pattern.fromKey(sp.getString(KEY_BG_PATTERN, null))
        set(v) = sp.edit().putString(KEY_BG_PATTERN, v.key).apply()

    var backgroundPatternColor: Int
        get() = sp.getInt(KEY_BG_PATTERN_COLOR, Theming.DEFAULT_ACCENT)
        set(v) = sp.edit().putInt(KEY_BG_PATTERN_COLOR, v).apply()

    /** Staerke des Musters in Prozent, 5..100, immer in Schritten von 5. */
    var backgroundPatternStrength: Int
        get() = snapStrength(sp.getInt(KEY_BG_PATTERN_STRENGTH, 15))
        set(v) = sp.edit().putInt(KEY_BG_PATTERN_STRENGTH, snapStrength(v)).apply()

    val isConfigured: Boolean
        get() = meetDate != null || weddingDate != null

    /**
     * Der Regler in den Einstellungen laeuft in Schritten von 5 ab 5.
     * Ein Wert dazwischen laesst Material eine Ausnahme werfen, deshalb
     * wird hier gerundet - auch fuer Werte, die schon gespeichert sind.
     */
    private fun snapStrength(value: Int): Int =
        ((value + 2) / 5 * 5).coerceIn(5, 100)

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
        const val KEY_MS_HUNDRED = "milestone_hundreds"
        const val KEY_MS_THOUSAND = "milestone_thousands"
        const val KEY_MS_REPDIGIT = "milestone_repdigits"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_HEADER_PATTERN = "header_pattern"
        const val KEY_HEADER_PATTERN_COLOR = "header_pattern_color"
        const val KEY_HEADER_PATTERN_STRENGTH = "header_pattern_strength"
        const val KEY_BG_PATTERN = "bg_pattern"
        const val KEY_BG_PATTERN_COLOR = "bg_pattern_color"
        const val KEY_BG_PATTERN_STRENGTH = "bg_pattern_strength"
    }
}
