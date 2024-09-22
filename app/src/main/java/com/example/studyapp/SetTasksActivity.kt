package com.example.studyapp

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner

class SetTasksActivity : AppCompatActivity(){
    private lateinit var dbHelper: DBHelper

    // UI Components
    private lateinit var calendar: CalendarView
    private lateinit var addTaskButton: Button
    private lateinit var taskListView: ListView

    private var selectedDate: String = ""   // 選択された日付

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_tasks)

        dbHelper = DBHelper(this, "study_app.db", 1)

        initializeUI()

        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            updateTaskList(selectedDate)
        }

        addTaskButton.setOnClickListener {
            showAddTasksDialog()
        }

        /***
         * <実装メモ>
         *     ユーザーがよく使うタスクは保存しておき、入力しやすくする <-- showTaskDetailsで処理？
         *     周期的なタスクはテンプレート化する(平日はこう、土日はこう、など) <-- そのためにDB再設計
         *
         */

    }

    private fun initializeUI() {
        calendar = findViewById(R.id.calendarView)
        addTaskButton = findViewById(R.id.add_task)
        taskListView = findViewById(R.id.taskListView)
    }

    // 特定の日のタスクを追加する
    private fun showAddTasksDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_tasks, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.taskNameEditText)
        val taskDescriptionEditText = dialogView.findViewById<EditText>(R.id.taskDescriptionEditText)
        val repeatSpinner = dialogView.findViewById<AppCompatSpinner>(R.id.repeat_spinner)

        // spinner 参考: https://qiita.com/kenmaeda51415/items/b5abf3fe92d5ce349ad9
        val repeatDay = arrayOf("繰り返さない", "特定の曜日")
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, repeatDay)
        repeatSpinner.adapter = arrayAdapter

        AlertDialog.Builder(this)
            .setTitle("タスクを追加")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val taskName = taskNameEditText.text.toString()
                val taskDescription = taskDescriptionEditText.text.toString()
                val repeat = repeatSpinner.selectedItem.toString()

                /**
                 * <実装メモ>
                 *     周期的なタスクの自動入力 -> 一度に全部処理するわけにはいかない？
                 *     特定の曜日を選べるようにする
                 *     繰り返し設定の選択を工夫する
                 *     繰り返すタスクはあとで一括削除、編集ができるとよい
                 * */

                if (taskName.isNotEmpty()) {
                    saveTask(taskName, taskDescription)
                    updateTaskList(selectedDate)
                } else {
                    Toast.makeText(this, "タスク名を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    // 入力をtasksテーブルに保存
    private fun saveTask(taskName: String, taskDescription: String) {
        val db = dbHelper.writableDatabase

        // selectedDate を使用
        val values = ContentValues().apply {
            put("task_name", taskName)
            put("description", taskDescription)
            put("date", selectedDate)
        }

        val newRowId = db.insert("tasks", null, values)
        if (newRowId != -1L) {
            Toast.makeText(this, "タスクが保存されました", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "タスクの保存に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    // 選択された日のタスク一覧を表示
    private fun updateTaskList(date: String) {
        val tasks = getTasksForDate(date)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks.map { it.toString() })
        taskListView.adapter = adapter

        taskListView.setOnItemClickListener { _, _, position, _ ->
            val task = tasks[position]
            showTaskDetails(task)
        }
    }

    // 選択されたタスクの詳細を表示
    private fun showTaskDetails(task: Task) {
        AlertDialog.Builder(this)
            .setTitle(task.taskName)
            .setMessage("タスク名: ${task.taskName}\n説明: ${task.description}")
            .setPositiveButton("閉じる", null)
            .show()
    }

    // 選択された日のタスク一覧を取得
    private fun getTasksForDate(selectDate: String): List<Task> {
        val db = dbHelper.readableDatabase
        val tasks = mutableListOf<Task>()

        val cursor = db.rawQuery(
            "SELECT * FROM tasks WHERE date=?",
            arrayOf(selectDate)
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val taskName = cursor.getString(cursor.getColumnIndexOrThrow("task_name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))

            tasks.add(Task(id, taskName, description, date))  // ここでリストに追加
        }
        cursor.close()

        return tasks
    }
}

data class Task(
    val id: Int,
    val taskName: String,
    val description: String,
    val date: String,
) {
    override fun toString(): String {
        return taskName
    }
}
