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
        private const val SQL_CREATE_STUDY_SESSIONS_TABLE = """
            CREATE TABLE study_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                study_date TEXT,
                total_minutes INTEGER
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.apply {
            execSQL(SQL_CREATE_TASKS_TABLE)
            execSQL(SQL_CREATE_EVENTS_TABLE)
            execSQL(SQL_CREATE_STUDY_SESSIONS_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.apply {
            execSQL("DROP TABLE IF EXISTS tasks")
            execSQL("DROP TABLE IF EXISTS events")
            execSQL("DROP TABLE IF EXISTS study_sessions")
        }
        onCreate(db)
    }
}