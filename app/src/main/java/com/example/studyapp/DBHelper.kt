package com.example.studyapp

import android.content.Context
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
                task_name TEXT
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
                study_date DATE DEFAULT CURRENT_TIMESTAMP,
                total_minutes INTEGER
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.apply {
            execSQL(SQL_CREATE_TASKS_TABLE)
            execSQL(SQL_CREATE_EVENTS_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.apply {
            execSQL("DROP TABLE IF EXISTS tasks")
            execSQL("DROP TABLE IF EXISTS events")
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
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name != 'android_metadata' AND name != 'sqlite_sequence' AND name != 'tasks' AND name != 'events'", null)
        val tables = mutableListOf<String>()

        cursor.use {
            while (it.moveToNext()) {
                tables.add(it.getString(0))
            }
        }

        return tables
    }
}

