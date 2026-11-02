package com.unseretage.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {

    private const val REQUEST_CODE = 1001

    /** Setzt den Wecker auf die nächste fällige Uhrzeit (heute oder morgen). */
    fun scheduleNext(context: Context) {
        val prefs = Prefs(context)
        if (!prefs.isConfigured) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAt = nextTriggerMillis(prefs)
        val pending = pendingIntent(context)

        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        try {
            if (canBeExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pending
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pending
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun nextTriggerMillis(prefs: Prefs): Long {
        val zone = ZoneId.systemDefault()
        val time = LocalTime.of(prefs.notifyHour, prefs.notifyMinute)
        val now = LocalDateTime.now(zone)
        var next = LocalDateTime.of(LocalDate.now(zone), time)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.atZone(zone).toInstant().toEpochMilli()
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
