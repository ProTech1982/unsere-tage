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
    }
}
