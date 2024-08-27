package com.example.studyapp

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class TodoActivity : AppCompatActivity() {
    // データベース管理クラスのインスタンス
    private lateinit var databaseManager: DatabaseManager
    // ToDoリストを表示するためのアダプター
    private lateinit var todoListAdapter: ArrayAdapter<String>

    // UIコンポーネント
    private lateinit var toDoEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var todoListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        // UIコンポーネントの初期化
        initializeUI()

        // データベースマネージャーの初期化
        databaseManager = DatabaseManager.getInstance(this)

        // タスクリストの取得と表示
        refreshTaskList()

        // 保存ボタンのクリックリスナー設定
        saveButton.setOnClickListener {
            val task = toDoEditText.text.toString().trim()
            if (task.isNotEmpty()) {
                insertTask(task)
                toDoEditText.text.clear()
                refreshTaskList()
            }
        }
    }

    // UIコンポーネントの初期化
    private fun initializeUI() {
        toDoEditText = findViewById(R.id.ToDo_EditText)
        saveButton = findViewById(R.id.save_button)
        todoListView = findViewById(R.id.todo_list_view)
    }

    // タスクリストの更新と表示
    private fun refreshTaskList() {
        val tasks = getTasks()
        todoListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks)
        todoListView.adapter = todoListAdapter
    }

    // データベースからタスクを取得
    private fun getTasks(): List<String> {
        val tasks = mutableListOf<String>()
        databaseManager.openDatabase().use { db ->
            val cursor = db.query("tasks", arrayOf("task_name"), null, null, null, null, null)
            cursor.use {
                while (it.moveToNext()) {
                    val taskName = it.getString(it.getColumnIndexOrThrow("task_name"))
                    tasks.add(taskName)
                }
            }
        }
        return tasks
    }

    // データベースにタスクを挿入
    private fun insertTask(task: String) {
        databaseManager.openDatabase().use { db ->
            val values = ContentValues().apply {
                put("task_name", task)
            }
            db.insert("tasks", null, values)
        }
    }

    // アクティビティ破棄時にデータベース接続を閉じる
    override fun onDestroy() {
        super.onDestroy()
        databaseManager.closeDatabase()
    }
}