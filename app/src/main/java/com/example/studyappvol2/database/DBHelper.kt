package com.example.studyappvol2.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(
    context: Context,
    databaseName: String,
    databaseVersion: Int
) : SQLiteOpenHelper(context, databaseName, null, databaseVersion) {

    companion object {
        private const val SQL_CREATE_TASKS_TABLE = """
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                task_name TEXT,
                description TEXT,
                date TEXT NOT NULL
            )
        """
        private const val SQL_CREATE_EVENTS_TABLE = """
            CREATE TABLE events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_name TEXT,
                event_date TEXT,
                start_time TEXT,
                end_time TEXT,
                description TEXT
            )
        """
        private const val SQL_CREATE_STUDY_TABLE_TEMPLATE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                study_date TEXT,
                total_minutes INTEGER
            )
        """
        // 時間がちょっとずれる -> 日付はKotlinで取得するようにした

        /** 睡眠時間データ
         * id: 識別子
         * startTimeMillis: 睡眠セグメントの開始時刻（ミリ秒）
         * endTimeMillis: 睡眠セグメントの終了時刻（ミリ秒）
         * durationMillis: 睡眠時間（ミリ秒、endTimeMillis - startTimeMillis）
         * confidence: 睡眠セグメントの信頼度（0-100）
         * insertedAt: データの挿入時刻（タイムスタンプ）
         */
        private const val SQL_CREATE_SLEEP_DATA_TABLE = """
           CREATE TABLE IF NOT EXISTS sleep_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                startTimeMillis BIGINT NOT NULL,
                endTimeMillis BIGINT NOT NULL,
                durationMillis BIGINT NOT NULL,
                insertedAt BIGINT NOT NULL
            )
        """

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.apply {
            execSQL(SQL_CREATE_TASKS_TABLE)
            execSQL(SQL_CREATE_EVENTS_TABLE)
            execSQL(SQL_CREATE_SLEEP_DATA_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.apply {
            execSQL("DROP TABLE IF EXISTS tasks")
            execSQL("DROP TABLE IF EXISTS events")
            execSQL("DROP TABLE IF EXISTS sleep_data")
        }
        onCreate(db)
    }

    // 指定された名前で勉強時間管理のテーブルを作成
    fun createStudyTable(tableName: String) {
        val db = writableDatabase
        val createTableSQL = String.format(SQL_CREATE_STUDY_TABLE_TEMPLATE, tableName)
        db.execSQL(createTableSQL)
    }

    // DB内に存在する勉強時間管理テーブルの一覧を取得する
    fun getStudyTables(): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name != 'android_metadata' AND name != 'sqlite_sequence' AND name != 'tasks' AND name != 'events' AND name != 'sleep_data'", null)
        val tables = mutableListOf<String>()

        cursor.use {
            while (it.moveToNext()) {
                tables.add(it.getString(0))
            }
        }

        return tables
    }

    // 今日の勉強時間を取得する
    // 引数 : 科目名
    fun getTodayStudyTime(s: String): Int {
        val db = readableDatabase
        var totalMinutesToday = 0

        val cursor = db.rawQuery("SELECT SUM(total_minutes) AS total_minutes_today FROM ${s} WHERE DATE(study_date) = DATE('now', 'localtime')", null)
        cursor.use {
            if (it.moveToFirst()) {
                // total_minutes_todayがnullの場合、0を返すようにする
                totalMinutesToday = it.getInt(it.getColumnIndexOrThrow("total_minutes_today"))
            }
        }

        return totalMinutesToday
    }

    // 日ごとの勉強時間を取得
    fun getAggregatedMinutes(): Cursor {
        val db = this.readableDatabase
        val tableNames = getStudyTables()
        val unionQuery = buildUnionQuery(tableNames)
        return db.rawQuery(unionQuery, null)
    }
    // 各テーブルに対するクエリを生成
    fun buildUnionQuery(tableNames: List<String>): String {
        val queryParts = tableNames.map { tableName ->
            "SELECT study_date, total_minutes FROM $tableName"
        }
        val unionQuery = queryParts.joinToString(" UNION ALL ")
        return """
        SELECT study_date, SUM(total_minutes) as total_minutes
        FROM ($unionQuery)
        GROUP BY study_date
        ORDER BY study_date
        """
    }

    // 特定の日の勉強時間を取得
    fun getStudyTimeP(d: String): Int {
        val db = readableDatabase
        val subjects = getStudyTables()

        var totalTime = 0

        // 現在あるテーブルのそれぞれついてクエリ処理
        subjects.forEachIndexed { i, item ->
            val query = "SELECT SUM(total_minutes) FROM $item WHERE study_date = ?"
            val cursor = db.rawQuery(query, arrayOf(d))

            if (cursor != null && cursor.moveToFirst()) {
                // カーソルからSUMの結果を取得。getInt(0)は最初の列（SUM結果）を意味する
                totalTime += cursor.getInt(0)
            }
            cursor.close()
        }

        return totalTime
    }

    fun addStudyTime(subject: String, minutes: Int, currentDate: String) {
        val db = writableDatabase
        try {
            // 新しいレコードを直接挿入
            val insertQuery = "INSERT INTO $subject (study_date, total_minutes) VALUES (?, ?)"
            db.execSQL(insertQuery, arrayOf(currentDate, minutes))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 睡眠データの挿入
    fun insertSleepData(
        startTimeMillis: Long,
        endTimeMillis: Long,
        durationMillis: Long,
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("startTimeMillis", startTimeMillis)
            put("endTimeMillis", endTimeMillis)
            put("durationMillis", durationMillis)
            put("insertedAt", System.currentTimeMillis())
        }
        return db.insert("sleep_data", null, values)
    }

    /** 特定期間の睡眠データを取得
     * 引数:
     *      startTime (Long): 対象期間の開始時刻（ミリ秒単位のタイムスタンプ）
     *      endTime (Long): 対象期間の終了時刻（ミリ秒単位のタイムスタンプ）
     * 戻り値: List<SleepSegment>
     *      データベースから取得した睡眠セグメントをリスト形式で返す
     *      各要素は以下のプロパティを持つSleepSegmentデータクラスのインスタンスである：
     *      startTimeMillis: セグメントの開始時刻
     *      endTimeMillis: セグメントの終了時刻
     *      durationMillis: セグメントの期間（終了時刻 - 開始時刻）
     */
    fun getSleepData(startTime: Long, endTime: Long): List<SleepSegment> {
        val db = readableDatabase
        val cursor = db.query(
            "sleep_data",
            null,
            "startTimeMillis >= ? AND endTimeMillis <= ?",
            arrayOf(startTime.toString(), endTime.toString()),
            null,
            null,
            "startTimeMillis DESC"
        )
        val sleepSegments = mutableListOf<SleepSegment>()
        cursor.use {
            while (it.moveToNext()) {
                val start = it.getLong(it.getColumnIndexOrThrow("startTimeMillis"))
                val end = it.getLong(it.getColumnIndexOrThrow("endTimeMillis"))
                val duration = it.getLong(it.getColumnIndexOrThrow("durationMillis"))
                sleepSegments.add(SleepSegment(start, end, duration))
            }
        }
        return sleepSegments
    }

}

// 睡眠セグメントデータのモデル
data class SleepSegment(
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long,
)