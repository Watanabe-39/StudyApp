package com.example.studyapp

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity

class SetTasksActivity : AppCompatActivity(){
    private lateinit var databaseManager: DatabaseManager

    // UI Components
    private lateinit var calender: CalendarView
    private lateinit var addTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_tasks)

        initializeUI()

        /***
         * <実装メモ>
         * ユーザーが指定した日付のタスクを追加削除できるようにする
         * ユーザーがよく使うタスクは保存しておき、入力しやすくする
         * 周期的なタスクはテンプレート化する(平日はこう、土日はこう、など) <-- そのためにDB再設計
         *
         */

    }

    private fun initializeUI() {
        calender = findViewById(R.id.calendarView)
        addTaskButton = findViewById(R.id.add_task)
    }

}