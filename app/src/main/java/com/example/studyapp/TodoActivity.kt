package com.example.studyapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class TodoActivity : AppCompatActivity() {
    // データベース管理クラスのインスタンス
    private lateinit var databaseManager: DatabaseManager
    // ToDoリストを表示するためのアダプター
    private lateinit var todoListAdapter: ArrayAdapter<String>

    // UIコンポーネント
    private lateinit var toDoEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var todoListView: ListView
    private lateinit var deleteButton: Button
    private lateinit var navigationSetTasks: Button

    private var selectedTask: String? = null // 選択されたタスクを保存する変数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        // UIコンポーネントの初期化
        initializeUI()

        // 近日タスク管理画面への遷移
        navigationSetTasks.setOnClickListener {
            val intent = Intent(this, SetTasksActivity::class.java)
            startActivity(intent)
        }

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

        // 削除ボタンのクリックリスナー
        deleteButton.setOnClickListener {
            selectedTask?.let {
                deleteTask(it)
                refreshTaskList()
                selectedTask = null // 削除後に選択をクリア
            } ?: run {
                Toast.makeText(this, "削除するタスクを選択してください", Toast.LENGTH_SHORT).show()
            }
        }

        todoListView.setOnItemClickListener { _, _, position, _ ->
            selectedTask = todoListAdapter.getItem(position)
            Toast.makeText(this, "$selectedTask を選択しました", Toast.LENGTH_SHORT).show()
        }

    }

    private fun deleteTask(task: String) {
        databaseManager.openDatabase().use { db ->
            db.delete("tasks", "task_name = ?", arrayOf(task))
        }
    }

    // UIコンポーネントの初期化
    private fun initializeUI() {
        toDoEditText = findViewById(R.id.ToDo_EditText)
        saveButton = findViewById(R.id.save_button)
        todoListView = findViewById(R.id.todo_list_view)
        deleteButton = findViewById(R.id.delete_button)
        navigationSetTasks = findViewById(R.id.set_tasks)
    }

    // タスクリストの更新と表示
    private fun refreshTaskList() {
        val tasks = getTasks()
        todoListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks)
        todoListView.adapter = todoListAdapter
    }

    // データベースからタスクを取得
    // 正しく動作しない
    private fun getTasks(): List<String> {
        val tasks = mutableListOf<String>()

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDate.format(formatter)

        val db = databaseManager.openDatabase()
        db.use {
            val cursor = db.rawQuery("SELECT task_name FROM tasks WHERE date = ?", arrayOf(formattedDate))
            cursor.use { cur ->
                val taskNameIndex = cur.getColumnIndexOrThrow("task_name")
                while (cur.moveToNext()) {
                    val taskName = cur.getString(taskNameIndex)
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