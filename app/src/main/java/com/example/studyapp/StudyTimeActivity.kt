package com.example.studyapp

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudyTimeActivity : AppCompatActivity() {
    // データベース管理クラスのインスタンス
    private lateinit var dbHelper: DBHelper

    // UIコンポーネント
    private lateinit var addButton: Button
    private lateinit var subjectListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_time)

        // UIコンポーネントの初期化
        initializeUI()

        // DBHelperの初期化
        dbHelper = DBHelper(this, "study_app.db", 1)

        addButton.setOnClickListener {
            showAddSubjectDialog()
        }

        // テーブルリストの表示
        refreshSubjectList()

        // テーブルリストの要素のイベントハンドラを設定
        subjectListView.setOnItemClickListener { parent, view, position, id ->
            val selectedTable = parent.getItemAtPosition(position) as String    // クリックした要素のテキストを取得、格納
            Toast.makeText(this, "$selectedTable が選択されました", Toast.LENGTH_SHORT).show()
            // ここに選択されたアイテムに対する処理を追加
            showAddMinutesDialog(selectedTable)
        }
    }


    private fun refreshSubjectList() {
        val studyTables = dbHelper.getStudyTables()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studyTables)
        subjectListView.adapter = adapter
    }

    private fun initializeUI() {
        addButton = findViewById(R.id.add_subject_button)
        subjectListView = findViewById(R.id.subject_list_view)
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
                    db.insert(selectedTable, null, values)
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