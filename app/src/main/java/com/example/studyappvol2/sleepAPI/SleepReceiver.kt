package com.example.studyappvol2.sleepAPI

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.studyappvol2.database.DBHelper
import com.google.android.gms.location.SleepSegmentEvent

class SleepReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // SleepSegmentEvent が含まれているかを確認
        if (SleepSegmentEvent.hasEvents(intent)) {
            // Intentからすべての SleepSegmentEvent を抽出
            val sleepSegmentEvents = SleepSegmentEvent.extractEvents(intent)

            // データベースヘルパーを初期化
            val dbHelper = DBHelper(context, "study_app.db", 1)

            // 各 SleepSegmentEvent を処理
            for (event in sleepSegmentEvents) {
                val startTime = event.startTimeMillis   // 睡眠開始時刻
                val endTime = event.endTimeMillis       // 睡眠終了時刻
                val duration = endTime - startTime     // 睡眠期間（ミリ秒）

                // ログでデバッグ用にデータを表示
                Log.d("## SleepReceiver", "Sleep Event - Start: $startTime, End: $endTime, Duration: $duration ms")

                // データベースに保存
                val result = dbHelper.insertSleepData(
                    startTimeMillis = startTime,
                    endTimeMillis = endTime,
                    durationMillis = duration,
                )

                // 保存結果をログ出力
                if (result > 0) {
                    Log.d("## SleepReceiver", "Sleep data saved successfully with ID: $result")
                } else {
                    Log.e("## SleepReceiver", "Failed to save sleep data.")
                }
            }
        } else {
            Log.d("## SleepReceiver", "No sleep events found in the intent.")
        }

        // Sleep API Debug
        val testIntent = Intent(context, SleepReceiver::class.java)
        context.sendBroadcast(testIntent)

    }
}
