package com.unseretage.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Nach Neustart oder App-Update muss der Wecker neu gestellt werden. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        AlarmScheduler.scheduleNext(context)
    }
}
