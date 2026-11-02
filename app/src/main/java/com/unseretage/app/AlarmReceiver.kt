package com.unseretage.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = Prefs(context)
        val today = LocalDate.now()

        Reminders.forDay(prefs, today).forEachIndexed { index, reminder ->
            Notifier.show(context, BASE_ID + index, reminder.title, reminder.text)
        }

        // Wecker für morgen neu stellen.
        AlarmScheduler.scheduleNext(context)
    }

    private companion object {
        const val BASE_ID = 100
    }
}
