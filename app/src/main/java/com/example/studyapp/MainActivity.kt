package com.example.studyapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
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

//        画面遷移
        val todo_navigate_btn = findViewById<Button>(R.id.todo_navigate_button)
        val scadule_navigate_btn = findViewById<Button>(R.id.scadule_navigate_button)
        val study_time_navigate_btn = findViewById<Button>(R.id.study_time_navigate_button)

        todo_navigate_btn.setOnClickListener{
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
        }
        scadule_navigate_btn.setOnClickListener {
            val intent = Intent(this, ScaduleActivity::class.java)
            startActivity(intent)
        }
        study_time_navigate_btn.setOnClickListener {
            val intent = Intent(this, StudyTimeActivity::class.java)
            startActivity(intent)
        }


    }



}