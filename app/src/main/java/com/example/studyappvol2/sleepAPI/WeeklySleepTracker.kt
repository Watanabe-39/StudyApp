package com.example.studyappvol2.sleepAPI

import android.content.Context
import android.util.Log
import com.example.studyappvol2.database.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

// 1週間分の睡眠データをトラッキングするクラス
class WeeklySleepTracker(private val context: Context) {

    // データベースヘルパーを初期化
    private val dbHelper = DBHelper(context, "study_app.db", 1)

    // 非同期で1週間分の睡眠時間を計算するメソッド
    suspend fun getWeeklySleepDuration(): Long = withContext(Dispatchers.IO) {
        try {
            // 現在時刻を取得
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis

            // 1週間前の時刻を計算
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startTime = calendar.timeInMillis

            // データベースから睡眠データを取得
            val db = dbHelper.readableDatabase
            var totalSleepMillis = 0L

            // データ取得条件を設定
            val selection = "startTimeMillis >= ? AND endTimeMillis <= ?"
            val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

            // データベースクエリを実行
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
                    // 各データセグメントの開始時刻と終了時刻を取得
                    val segmentStart = cursor.getLong(cursor.getColumnIndexOrThrow("startTimeMillis"))
                    val segmentEnd = cursor.getLong(cursor.getColumnIndexOrThrow("endTimeMillis"))
                    totalSleepMillis += segmentEnd - segmentStart
                }
            }

            // ログに合計時間を記録
            Log.d(TAG, "## Total sleep time for the week: ${TimeUnit.MILLISECONDS.toHours(totalSleepMillis)} hours")
            return@withContext totalSleepMillis

        } catch (e: Exception) {
            // エラー発生時のログ
            Log.e(TAG, "## Error getting sleep data", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "WeeklySleepTracker"
    }
}
