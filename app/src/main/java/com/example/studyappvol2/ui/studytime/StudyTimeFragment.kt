package com.example.studyappvol2.ui.studytime

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Color
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
import com.example.studyappvol2.R
import com.example.studyappvol2.database.DBHelper
import com.example.studyappvol2.databinding.FragmentStudyTimeBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Date

class StudyTimeFragment : Fragment() {
    // データベース管理クラスのインスタンス
    private lateinit var dbHelper: DBHelper

    // UIコンポーネント
    private lateinit var addButton: Button
    private lateinit var subjectListView: ListView
    private lateinit var pieChartStudyTime: PieChart

    private var _binding: FragmentStudyTimeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyTimeBinding.inflate(inflater, container, false)
        val view = _binding!!.root

        initializeUI(view)

        // DBHelperの初期化
        dbHelper = DBHelper(requireContext(), "study_app.db", 1)

        addButton.setOnClickListener {
            showAddSubjectDialog()
        }

        refreshSubjectList()

        // テーブルリストの要素のイベントハンドラを設定
        subjectListView.setOnItemClickListener { parent, view, position, id ->
            val selectedTable = parent.getItemAtPosition(position) as String
            Toast.makeText(requireContext(), "$selectedTable が選択されました", Toast.LENGTH_SHORT).show()
            showAddMinutesDialog(selectedTable)
        }

        showPieChartStudyTime()

        return view
    }

    // UIコンポーネントの初期化
    private fun initializeUI(view: View) {
        addButton = view.findViewById(R.id.add_subject_button)
        subjectListView = view.findViewById(R.id.subject_list_view)
        pieChartStudyTime = view.findViewById(R.id.pieChartStudyTime)
    }

    // 今日の科目ごとの勉強時間の割合を表示
    private fun showPieChartStudyTime() {
        val dimensions = ArrayList<String>()
        val values = ArrayList<Float>()
        var totalTime = 0f

        val adapter = subjectListView.adapter as? ArrayAdapter<*>
        adapter?.let {
            for (i in 0 until it.count) {
                val item: String = (it.getItem(i) ?: continue).toString()
                val t: Int = dbHelper.getTodayStudyTime(item)
                if (t > 0) {
                    dimensions.add(item)
                    values.add(t.toFloat())
                    totalTime += t
                }
            }
        }

        if (totalTime > 0) {
            val entryList = values.mapIndexed { index, value ->
                PieEntry(value / totalTime, dimensions[index])
            }

            val pieDataSet = PieDataSet(entryList, "Study Time")
            pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

            val pieData = PieData(pieDataSet)
            pieData.setValueFormatter(PercentFormatter(pieChartStudyTime))
            pieData.setValueTextSize(11f)

            pieChartStudyTime.data = pieData
            pieChartStudyTime.apply {
                description.isEnabled = false
                legend.isEnabled = true
                setUsePercentValues(true)
                setEntryLabelTextSize(12f)
                centerText = "今日の勉強$totalTime" + "分"
                setCenterTextSize(16f)
                legend.textColor = Color.YELLOW
            }
            pieChartStudyTime.invalidate()
        } else {
            pieChartStudyTime.clear()
            pieChartStudyTime.centerText = "No study data for today"
            pieChartStudyTime.invalidate()
        }

        val params = pieChartStudyTime.layoutParams
        params.width = 800
        params.height = 800
        pieChartStudyTime.layoutParams = params
    }

    // テーブルのリストを表示
    private fun refreshSubjectList() {
        val studyTables = dbHelper.getStudyTables()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, studyTables)
        subjectListView.adapter = adapter
    }

    private fun showAddMinutesDialog(selectedTable: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_minutes, null)
        val db = dbHelper.writableDatabase
        val studyTimeEditText = dialogView.findViewById<EditText>(R.id.studyTimeEdit)

        AlertDialog.Builder(requireContext())
            .setTitle("勉強時間")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val studyMinutes = studyTimeEditText.text.toString()

                val currentDate = Date()
                val formatter = SimpleDateFormat("yyyy-MM-dd")
                val formattedDate = formatter.format(currentDate)

                if (studyMinutes.isNotEmpty()) {
                    val values = ContentValues().apply {
                        put("total_minutes", studyMinutes)
                        put("study_date", formattedDate)
                    }
                    val result = db.insert(selectedTable, null, values)
                    if (result != -1L) {
                        showPieChartStudyTime()
                    } else {
                        Toast.makeText(requireContext(), "保存に失敗しました", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "勉強時間を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun showAddSubjectDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_subject, null)
        val subjectNameEditText = dialogView.findViewById<EditText>(R.id.subjectNameEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("科目を追加")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val subjectName = subjectNameEditText.text.toString()

                if (subjectName.isNotEmpty()) {
                    if (subjectName == "tasks" || subjectName == "events") {
                        Toast.makeText(requireContext(), "この名前は使えません", Toast.LENGTH_SHORT).show()
                    } else {
                        dbHelper.createStudyTable(subjectName)
                        refreshSubjectList()
                        Toast.makeText(requireContext(), "科目 '$subjectName' が追加されました", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "科目名を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
