package com.example.studyapp

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class StudyTimeActivity : AppCompatActivity() {
    // データベース管理クラスのインスタンス
    private lateinit var databaseManager: DatabaseManager

    private lateinit var timeListAdapter: ArrayAdapter<String>

    // UIコンポーネント
    private lateinit var time_EditText: EditText
    private lateinit var time_save_button: Button
    private lateinit var time_list_view: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_time)

        // UIコンポーネントの初期化
        initializeUI()

        // データベースマネージャーの初期化
        databaseManager = DatabaseManager.getInstance(this)

        // DBの内容を表示
        refreshTimeList()

        // 保存ボタンのクリックリスナー
        time_save_button.setOnClickListener {
            val studyTime = time_EditText.text.toString().toIntOrNull()
            if (studyTime != null) {
                insertTime(studyTime)
                time_EditText.text.clear()
                refreshTimeList() // データの更新
            }
        }
    }

    private fun refreshTimeList() {
        val times = getTime()
        timeListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, times)
        time_list_view.adapter = timeListAdapter
    }

    private fun getTime(): List<String> {
        val times = mutableListOf<String>()
        databaseManager.openDatabase().use { db ->
            val cursor = db.query("study_sessions", arrayOf("total_minutes"), null, null, null, null, null)
            cursor.use {
                while (it.moveToNext()) {
                    val minutes = it.getInt(it.getColumnIndexOrThrow("total_minutes"))
                    times.add("$minutes 分")
                }
            }
        }
        return times
    }

    private fun insertTime(studyTime: Int) {
        databaseManager.openDatabase().use { db ->
            val values = ContentValues().apply {
                put("total_minutes", studyTime)
            }
            db.insert("study_sessions", null, values)
        }
    }

    private fun initializeUI() {
        time_EditText = findViewById(R.id.time_EditText)
        time_save_button = findViewById(R.id.time_save_button)
        time_list_view = findViewById(R.id.time_list_view)
    }
}

