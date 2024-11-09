package com.example.studyappvol2.sleepAPI

import android.content.Context
import android.util.Log
import com.example.studyappvol2.database.DBHelper
import com.google.android.gms.location.ActivityRecognition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WeeklySleepTracker(private val context: Context) {
    private val dbHelper = DBHelper(context, "study_app.db", 1)

    suspend fun getWeeklySleepDuration(): Long = withContext(Dispatchers.IO) {
        try {
            // 現在の時刻を取得
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis

            // 1週間前の時刻を計算
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startTime = calendar.timeInMillis

            // データベースから睡眠データを取得
            val db = dbHelper.readableDatabase
            var totalSleepMillis = 0L

            val selection = "startTimeMillis >= ? AND endTimeMillis <= ?"
            val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

            db.query(
                "sleep_data",
                arrayOf("startTimeMillis", "endTimeMillis"),
                selection,
                selectionArgs,
                null,
                null,
                "startTimeMillis DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val segmentStart = cursor.getLong(cursor.getColumnIndexOrThrow("startTimeMillis"))
                    val segmentEnd = cursor.getLong(cursor.getColumnIndexOrThrow("endTimeMillis"))
                    totalSleepMillis += segmentEnd - segmentStart
                }
            }

            Log.d(TAG, "Total sleep time for the week: ${TimeUnit.MILLISECONDS.toHours(totalSleepMillis)} hours")
            return@withContext totalSleepMillis

        } catch (e: Exception) {
            Log.e(TAG, "Error getting sleep data", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "WeeklySleepTracker"
    }
}