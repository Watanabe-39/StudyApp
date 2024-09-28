package com.example.studyappvol2.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class DatabaseManager private constructor(context: Context) {
    private val dbHelper: DBHelper = DBHelper(context.applicationContext, DATABASE_NAME, DATABASE_VERSION)
    private var database: SQLiteDatabase? = null

    companion object {
        private const val DATABASE_NAME = "study_app.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: DatabaseManager(context).also { instance = it }
            }
        }
    }

    fun openDatabase(): SQLiteDatabase {
        if (database == null || !database!!.isOpen) {
            database = dbHelper.writableDatabase
        }
        return database!!
    }

    fun closeDatabase() {
        database?.close()
    }
}