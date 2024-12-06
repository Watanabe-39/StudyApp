package com.example.studyappvol2.sleepAPI

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.SleepSegmentEvent

class SleepReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (SleepSegmentEvent.hasEvents(intent)) {
            val sleepSegmentEvents = SleepSegmentEvent.extractEvents(intent)
            for (event in sleepSegmentEvents) {
                Log.d("SleepReceiver", "Sleep event: ${event.toString()}")
                // 必要に応じて、受け取った睡眠データを処理
            }
        }
    }
}
