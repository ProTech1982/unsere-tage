package com.unseretage.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        applyThemeMode(Prefs(this).themeMode)
    }

    companion object {
        /** Setzt hell, dunkel oder der Systemeinstellung folgend. */
        fun applyThemeMode(mode: String) {
            AppCompatDelegate.setDefaultNightMode(
                when (mode) {
                    "light" -> AppCompatDelegate.MODE_NIGHT_NO
                    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }
    }
}
