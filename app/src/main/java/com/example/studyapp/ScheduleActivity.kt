package com.example.studyapp

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private var selectedDate: String = ""
    private lateinit var eventListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        dbHelper = DBHelper(this, "study_app.db", 1)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val addEventButton = findViewById<Button>(R.id.addEventButton)
        eventListView = findViewById(R.id.eventListView)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            updateEventList(selectedDate)
        }

        addEventButton.setOnClickListener {
            showAddEventDialog()
        }

        // 初期表示時のイベントリスト更新
        val calendar = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(calendar.time)
        updateEventList(selectedDate)
    }

    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val eventNameEditText = dialogView.findViewById<EditText>(R.id.eventNameEditText)
        val eventDescriptionEditText = dialogView.findViewById<EditText>(R.id.eventDescriptionEditText)
        val startTimeEditText = dialogView.findViewById<EditText>(R.id.startTimeEditText)
        val endTimeEditText = dialogView.findViewById<EditText>(R.id.endTimeEditText)

        AlertDialog.Builder(this)
            .setTitle("イベントを追加")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val eventName = eventNameEditText.text.toString()
                val eventDescription = eventDescriptionEditText.text.toString()
                val startTime = startTimeEditText.text.toString()
                val endTime = endTimeEditText.text.toString()

                if (eventName.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                    saveEvent(eventName, eventDescription, startTime, endTime)
                    updateEventList(selectedDate)
                } else {
                    Toast.makeText(this, "すべての必須フィールドを入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun saveEvent(eventName: String, eventDescription: String, startTime: String, endTime: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("event_name", eventName)
            put("event_date", selectedDate)
            put("start_time", startTime)
            put("end_time", endTime)
            put("description", eventDescription)
        }

        val newRowId = db.insert("events", null, values)
        if (newRowId != -1L) {
            Toast.makeText(this, "イベントが保存されました", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "イベントの保存に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEventsForDate(date: String): List<Event> {
        val db = dbHelper.readableDatabase
        val events = mutableListOf<Event>()

        val cursor = db.query(
            "events",
            null,
            "event_date = ?",
            arrayOf(date),
            null,
            null,
            "start_time ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow("id"))
                val eventName = getString(getColumnIndexOrThrow("event_name"))
                val eventDate = getString(getColumnIndexOrThrow("event_date"))
                val startTime = getString(getColumnIndexOrThrow("start_time"))
                val endTime = getString(getColumnIndexOrThrow("end_time"))
                val description = getString(getColumnIndexOrThrow("description"))

                events.add(Event(id, eventName, eventDate, startTime, endTime, description))
            }
        }
        cursor.close()

        return events
    }

    private fun updateEventList(date: String) {
        val events = getEventsForDate(date)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, events.map { it.toString() })
        eventListView.adapter = adapter

        eventListView.setOnItemClickListener { _, _, position, _ ->
            val event = events[position]
            showEventDetails(event)
        }
    }

    private fun showEventDetails(event: Event) {
        AlertDialog.Builder(this)
            .setTitle(event.eventName)
            .setMessage("日付: ${event.eventDate}\n開始時間: ${event.startTime}\n終了時間: ${event.endTime}\n説明: ${event.description}")
            .setPositiveButton("閉じる", null)
            .show()
    }
}

data class Event(
    val id: Int,
    val eventName: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val description: String
) {
    override fun toString(): String {
        return "$eventName ($startTime - $endTime)"
    }
}