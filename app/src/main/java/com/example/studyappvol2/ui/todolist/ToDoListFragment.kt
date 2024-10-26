package com.example.studyappvol2.ui.todolist

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.studyappvol2.database.DBHelper
import com.example.studyappvol2.database.DatabaseManager
import com.example.studyappvol2.databinding.FragmentToDoListBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ToDoListFragment : Fragment() {

    private lateinit var databaseManager: DatabaseManager
    private lateinit var dbHelper: DBHelper
    private lateinit var todoListAdapter: ArrayAdapter<String>

    private lateinit var toDoEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var todoListView: ListView
    private lateinit var deleteButton: Button
    private lateinit var navigationSetTasks: Button

    private var selectedTask: String? = null

    private var _binding: FragmentToDoListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val toDoListViewModel =
            ViewModelProvider(this).get(ToDoListViewModel::class.java)

        _binding = FragmentToDoListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initializeUI()

        // 近日タスク管理画面への遷移
//        navigationSetTasks.setOnClickListener {
//            val intent = Intent(requireActivity(), SetTasksActivity::class.java)
//            startActivity(intent)
//        }

        databaseManager = DatabaseManager.getInstance(requireContext(),)
        dbHelper = DBHelper(requireContext(), "study_app.db", 1)

        postponeIncompleteTasks()
        refreshTaskList()

        saveButton.setOnClickListener {
            val task = toDoEditText.text.toString().trim()
            if (task.isNotEmpty()) {
                insertTask(task)
                toDoEditText.text.clear()
                refreshTaskList()
            }
        }

        deleteButton.setOnClickListener {
            selectedTask?.let {
                deleteTask(it)
                refreshTaskList()
                selectedTask = null
            } ?: run {
                Toast.makeText(requireContext(), "削除するタスクを選択してください", Toast.LENGTH_SHORT).show()
            }
        }

        todoListView.setOnItemClickListener { _, _, position, _ ->
            selectedTask = todoListAdapter.getItem(position)
            Toast.makeText(requireContext(), "$selectedTask を選択しました", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    private fun deleteTask(task: String) {
        databaseManager.openDatabase().use { db ->
            db.delete("tasks", "task_name = ?", arrayOf(task))
        }
    }

    private fun initializeUI() {
        toDoEditText = binding.ToDoEditText
        saveButton = binding.saveButton
        todoListView = binding.todoListView
        deleteButton = binding.deleteButton
        navigationSetTasks = binding.setTasks
    }

    private fun refreshTaskList() {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val formattedDate = currentDate.format(formatter)

        val tasks = getTasksForDate(formattedDate.toString())
        todoListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tasks.map { it.toString() })
        todoListView.adapter = todoListAdapter
    }

    private fun postponeIncompleteTasks() {
        val db = dbHelper.writableDatabase

        val yesterday = LocalDate.now().minusDays(1)
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val formattedYesterday = yesterday.format(formatter)
        val formattedToday = today.format(formatter)

        val yesterdayTasks = getTasksForDate(formattedYesterday)
        if (yesterdayTasks.isNotEmpty()) {
            db.execSQL("UPDATE tasks SET date=? WHERE date=?", arrayOf(formattedToday, formattedYesterday))
            Toast.makeText(requireContext(), "昨日完了されなかったタスクを今日に繰越しました", Toast.LENGTH_LONG).show()
        }
    }

    private fun insertTask(task: String) {
        databaseManager.openDatabase().use { db ->
            val values = ContentValues().apply {
                put("task_name", task)
            }
            db.insert("tasks", null, values)
        }
    }

    private fun getTasksForDate(selectDate: String): List<Task> {
        val db = dbHelper.readableDatabase
        val tasks = mutableListOf<Task>()

        val cursor = db.rawQuery("SELECT * FROM tasks WHERE date=?", arrayOf(selectDate))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val taskName = cursor.getString(cursor.getColumnIndexOrThrow("task_name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))

            tasks.add(Task(id, taskName, description, date))
        }
        cursor.close()

        return tasks
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseManager.closeDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
