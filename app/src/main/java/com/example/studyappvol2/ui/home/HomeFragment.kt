package com.example.studyappvol2.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.studyappvol2.R
import com.example.studyappvol2.database.DBHelper
import com.example.studyappvol2.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var dbHelper: DBHelper
    private lateinit var studyTimeLineChart: LineChart

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dbHelper = DBHelper(requireContext(), "study_app.db", 1)
        studyTimeLineChart = root.findViewById(R.id.study_time_lineChart)

        // グラフの大きさを調節
        val params = studyTimeLineChart.layoutParams
        params.height = 650 // 高さを 600 ピクセルに設定
        params.width = ViewGroup.LayoutParams.MATCH_PARENT // 幅を親ビューに合わせる
        studyTimeLineChart.layoutParams = params

        showStudyTimeChart()

        evaluate(2)

        return root
    }

    // 直近1週間の勉強時間の折れ線グラフを表示する
    // 参考: https://appdev-room.com/android-mpandroidchart
    // https://re-engines.com/2019/03/11/kotlin-mpandroidchart%E3%83%A9%E3%82%A4%E3%83%96%E3%83%A9%E3%83%AA%E3%82%92%E4%BD%BF%E3%81%84%E7%A7%BB%E5%8B%95%E5%B9%B3%E5%9D%87%E7%B7%9A%E3%82%92%E6%8F%8F%E7%94%BB%E3%81%97%E3%81%A6%E3%81%BF/
    private fun showStudyTimeChart() {
        val dateList = getWeekDates(true)
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

    /**
     * １週間の日付を返す関数
     * 引数: 今日から(true)か、昨日から(false)
     * */
    private fun getWeekDates(startFromToday: Boolean): List<String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = mutableListOf<String>()

        // startFromToday が false の場合、開始日を昨日にする
        if (!startFromToday) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        // 7日分の日付を取得
        for (i in 0..6) {
            dates.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, -1) // 日付を1日戻す
        }

        return dates.reversed() // 昨日から今日の順にするために逆順にする
    }

    // 学習の取り組みを評価する関数
    // 引数: 学年(Int)
    private fun evaluate(schoolYear: Int) {
        var StudyTimeEvaluation = true
        var SleepTimeEvaluation = true
        var PlanningEvaluation = true

        val dateList = getWeekDates(false)  // 先週の日付を取得
        val Timedata: MutableList<Float> = mutableListOf()  // 各日の勉強時間
        dateList.forEachIndexed { index, date ->
            val t = dbHelper.getStudyTimeP(date).toFloat()
            Timedata.add(t)
        }

        // 勉強時間を評価
        var totalTime = 0.0
        Timedata.forEachIndexed {index, time ->
            totalTime += time
        }
        val avgTime = totalTime / 7

        // 勉強時間の評価基準は仮
        if (schoolYear == 1 && avgTime < 90) StudyTimeEvaluation = false
        if (schoolYear == 2 && avgTime < 240) StudyTimeEvaluation = false
        if (schoolYear == 3 && avgTime < 420) StudyTimeEvaluation = false

        println("###study time: $Timedata")
        println("###average time: $avgTime")
        println("###evaluate: $StudyTimeEvaluation")

        // 睡眠時間を評価


        // 計画を評価


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}