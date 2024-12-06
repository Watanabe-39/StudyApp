package com.example.studyappvol2.ui.studytime

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studyappvol2.database.DBHelper
import com.example.studyappvol2.databinding.FragmentStudyTimeBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class StudyTimeFragment : Fragment() {
    private var _binding: FragmentStudyTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHelper
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext(), "study_app.db", 1)
        setupRecyclerView()
        setupAddSubjectButton()
        updatePieChart()
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter(dbHelper.getStudyTables()) { subject ->
            showAddMinutesDialog(subject)
        }
        binding.subjectRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subjectAdapter
        }
    }

    private fun setupAddSubjectButton() {
        binding.addSubjectButton.setOnClickListener {
            showAddSubjectDialog()
        }
    }

    private fun showAddSubjectDialog() {
        val input = TextInputEditText(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Subject")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val subjectName = input.text.toString()
                if (subjectName.isNotEmpty() && subjectName !in listOf("tasks", "events")) {
                    dbHelper.createStudyTable(subjectName)
                    refreshSubjectList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddMinutesDialog(subject: String) {
        val input = TextInputEditText(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Study Time for $subject")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val minutes = input.text.toString().toIntOrNull() ?: 0
                if (minutes > 0) {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    dbHelper.addStudyTime(subject, minutes, currentDate)
                    updatePieChart()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshSubjectList() {
        subjectAdapter.updateSubjects(dbHelper.getStudyTables())
    }

    private fun updatePieChart() {
        // 今日の日付を取得
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDate.format(formatter)

        val subjects = dbHelper.getStudyTables()
        val totalStudyTime = dbHelper.getStudyTimeP(formattedDate)
        val entries = subjects.mapNotNull { subject ->
            val time = dbHelper.getTodayStudyTime(subject)
            if (time > 0) PieEntry(time.toFloat(), subject) else null
        }

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "Study Time").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 12f
                valueTextColor = Color.WHITE
            }

            binding.pieChartStudyTime.apply {
                data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter(binding.pieChartStudyTime)) // 修正箇所
                }
                description.isEnabled = false
                legend.isEnabled = true
                setUsePercentValues(true)
                setEntryLabelColor(Color.WHITE)
                setEntryLabelTextSize(12f)
                centerText = "${totalStudyTime}\nminutes"
                setCenterTextSize(16f)
                animateY(1000)
            }

        } else {
            binding.pieChartStudyTime.apply {
                clear()
                centerText = "No study data\nfor today"
                invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}