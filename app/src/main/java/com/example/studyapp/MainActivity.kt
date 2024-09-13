package com.example.studyapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import com.github.mikephil.charting.charts.LineChart
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper

    private lateinit var studyTimeLineChart: LineChart

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DBHelper(this, "study_app.db", 1)
        studyTimeLineChart = findViewById(R.id.study_time_lineChart)

        showStudyTimeChart()

        // Screen transition
        val todo_navigate_btn = findViewById<Button>(R.id.todo_navigate_button)
        val scadule_navigate_btn = findViewById<Button>(R.id.scadule_navigate_button)
        val study_time_navigate_btn = findViewById<Button>(R.id.study_time_navigate_button)

        todo_navigate_btn.setOnClickListener{
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
        }
        scadule_navigate_btn.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }
        study_time_navigate_btn.setOnClickListener {
            val intent = Intent(this, StudyTimeActivity::class.java)
            startActivity(intent)
        }

    }

    // 直近1週間の勉強時間の折れ線グラフを表示する
    private fun showStudyTimeChart(){
        val dateList = getLastWeekDates()
        // それぞれの日付でgetStudyTimePを呼び出し、
        // 折れ線グラフを生成する

    }

    // 今日から直近の１週間の日付を取得
    private fun getLastWeekDates(): List<Date> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val dates = mutableListOf<Date>()

        for (i in 0..6) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        return dates.reversed()
    }

}