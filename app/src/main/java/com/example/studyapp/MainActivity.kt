package com.example.studyapp

import android.annotation.SuppressLint
import android.content.Entity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.sql.Time
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.github.mikephil.charting.data.Entry
//import java.security.KeyStore.Entry

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

        // グラフの大きさを調節
        val params = studyTimeLineChart.layoutParams
        params.height = 650 // 高さを 600 ピクセルに設定
        params.width = ViewGroup.LayoutParams.MATCH_PARENT // 幅を親ビューに合わせる
        studyTimeLineChart.layoutParams = params


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
    // 参考: https://appdev-room.com/android-mpandroidchart
    // https://re-engines.com/2019/03/11/kotlin-mpandroidchart%E3%83%A9%E3%82%A4%E3%83%96%E3%83%A9%E3%83%AA%E3%82%92%E4%BD%BF%E3%81%84%E7%A7%BB%E5%8B%95%E5%B9%B3%E5%9D%87%E7%B7%9A%E3%82%92%E6%8F%8F%E7%94%BB%E3%81%97%E3%81%A6%E3%81%BF/
    private fun showStudyTimeChart() {
        val dateList = getLastWeekDates()
        val Timedata: MutableList<Float> = mutableListOf()

        // それぞれの日の総勉強時間を取得
        dateList.forEachIndexed { index, date ->
            val t = dbHelper.getStudyTimeP(date).toFloat()
            Timedata.add(t)
        }

        val dataEntries = ArrayList<Entry>()
        Timedata.forEachIndexed { index, value ->
            // X軸は配列のインデックス番号
            val dataEntry = Entry(index.toFloat(), value) // Entryを使用
            dataEntries.add(dataEntry)
        }

        // LineDataSetを作成
        val lineDataSet = LineDataSet(dataEntries, "1週間の勉強時間")
        lineDataSet.color = Color.BLUE
        lineDataSet.valueTextColor = Color.YELLOW
        lineDataSet.valueTextSize = 15F

        // LineDataを設定
        val lineData = LineData(lineDataSet)
        studyTimeLineChart.data = lineData

        // X軸とY軸の文字を白に設定
        studyTimeLineChart.xAxis.textColor = Color.WHITE
        studyTimeLineChart.axisLeft.textColor = Color.WHITE
        studyTimeLineChart.axisRight.textColor = Color.WHITE

        // ラベルなどのテキストカラーも白に設定
        studyTimeLineChart.legend.textColor = Color.WHITE
        studyTimeLineChart.description.textColor = Color.WHITE

        studyTimeLineChart.invalidate() // グラフを更新
    }


    // 今日から直近の１週間の日付を取得
    private fun getLastWeekDates(): List<String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 日付フォーマットの定義
        val dates = mutableListOf<String>()

        for (i in 0..6) {
            dates.add(dateFormat.format(calendar.time)) // 日付を yyyy-MM-dd 形式でフォーマット
            calendar.add(Calendar.DAY_OF_MONTH, -1) // 日付を1日戻す
        }

        return dates.reversed() // 昨日から今日の順にするために逆順にする
    }

}