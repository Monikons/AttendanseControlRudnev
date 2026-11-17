package com.example.attendancecontrol

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}