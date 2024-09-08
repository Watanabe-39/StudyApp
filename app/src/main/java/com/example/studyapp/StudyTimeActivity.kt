package com.example.studyapp

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class StudyTimeActivity : AppCompatActivity() {
    // データベース管理クラスのインスタンス
    private lateinit var dbHelper: DBHelper

    // UIコンポーネント
    private lateinit var addButton: Button
    private lateinit var subjectListView: ListView
    private lateinit var pieChartStudyTime: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_time)

        initializeUI()

        // DBHelperの初期化
        dbHelper = DBHelper(this, "study_app.db", 1)

        addButton.setOnClickListener {
            showAddSubjectDialog()
        }

        refreshSubjectList()

        // テーブルリストの要素のイベントハンドラを設定
        subjectListView.setOnItemClickListener { parent, view, position, id ->
            val selectedTable = parent.getItemAtPosition(position) as String    // クリックした要素のテキストを取得、格納
            Toast.makeText(this, "$selectedTable が選択されました", Toast.LENGTH_SHORT).show()
            // ここに選択されたアイテムに対する処理を追加
            showAddMinutesDialog(selectedTable)
        }

        showPieChartStudyTime()

    }

    // UIコンポーネントの初期化
    private fun initializeUI() {
        addButton = findViewById(R.id.add_subject_button)
        subjectListView = findViewById(R.id.subject_list_view)
        pieChartStudyTime = findViewById<PieChart>(R.id.pieChartStudyTime)
    }

    // 今日の科目ごとの勉強時間の割合を表示
    private fun showPieChartStudyTime() {
        // 参考:  https://qiita.com/c60evaporator/items/14e63d22d860b73e6f22

        val dimensions = ArrayList<String>()    // 分割円の名称
        val values = ArrayList<Float>()   // 分割円の大きさ

        var totalTime = 0f   // 総勉強時間 円グラフの割合を算出するため

        // subjectListViewの各要素についてループ
        val adapter = subjectListView.adapter as? ArrayAdapter<*>
        adapter?.let {
            for (i in 0 until it.count) {
                val item: String = (it.getItem(i) ?: continue).toString() // 科目名
                val t: Int = dbHelper.getTodayStudyTime(item)   // 科目の勉強時間
                if (t > 0) {
                    dimensions.add(item)
                    values.add(t.toFloat())
                    totalTime += t
                }
            }
        }

        // データが存在する場合のみグラフを描画
        if (totalTime > 0) {
            // Entryにデータ格納
            val entryList = values.mapIndexed { index, value ->
                PieEntry(value / totalTime, dimensions[index])
            }

            // PieDataSetにデータ格納
            val pieDataSet = PieDataSet(entryList, "Study Time")
            // DataSetのフォーマット指定
            pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

            // PieDataにPieDataSet格納
            val pieData = PieData(pieDataSet)
            // 値のフォーマット設定
            pieData.setValueFormatter(PercentFormatter(pieChartStudyTime))
            pieData.setValueTextSize(11f)

            // pieChartStudyTimeにPieData格納
            pieChartStudyTime.data = pieData
            // Chartのフォーマット指定
            pieChartStudyTime.apply {
                description.isEnabled = false
                legend.isEnabled = true
                setUsePercentValues(true)
                setEntryLabelTextSize(12f)
                centerText = "今日の勉強$totalTime" + "分"
                setCenterTextSize(16f)
                legend.textColor = Color.YELLOW
            }
            // pieChartStudyTimeを更新
            pieChartStudyTime.invalidate()
        } else {
            // データがない場合、グラフをクリアして中央にメッセージを表示
            pieChartStudyTime.clear()
            pieChartStudyTime.centerText = "No study data for today"
            pieChartStudyTime.invalidate()
        }

        // 大きさ設定
        val params = pieChartStudyTime.layoutParams
        params.width = 800  // ピクセル単位
        params.height = 800 // ピクセル単位
        pieChartStudyTime.layoutParams = params

    }

    // テーブルのリストを表示
    private fun refreshSubjectList() {
        val studyTables = dbHelper.getStudyTables()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studyTables)
        subjectListView.adapter = adapter
    }

    private fun showAddMinutesDialog(selectedTable: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_minutes, null)
        val db = dbHelper.writableDatabase
        val studyTimeEditText = dialogView.findViewById<EditText>(R.id.studyTimeEdit)

        AlertDialog.Builder(this)
            .setTitle("勉強時間")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val studyMinutes = studyTimeEditText.text.toString()

                if (studyMinutes.isNotEmpty()) {
                    val values = ContentValues().apply {
                        put("total_minutes", studyMinutes)
                    }
                    val result = db.insert(selectedTable, null, values)
                    if (result != -1L){
                        showPieChartStudyTime() // ここで円グラフを更新
                    } else {
                        Toast.makeText(this, "保存に失敗しました", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "勉強時間を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun showAddSubjectDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_subject, null)
        val subjectNameEditText = dialogView.findViewById<EditText>(R.id.subjectNameEditText)

        AlertDialog.Builder(this)
            .setTitle("科目を追加")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val subjectName = subjectNameEditText.text.toString()

                if (subjectName.isNotEmpty()) {
                    // 予約語が入力された時の処理
                    if (subjectName == "tasks" || subjectName == "events"){
                        Toast.makeText(this, "この名前は使えません", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        dbHelper.createStudyTable(subjectName)
                        refreshSubjectList()  // リストを更新
                        Toast.makeText(this, "科目 '$subjectName' が追加されました", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "科目名を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }
}