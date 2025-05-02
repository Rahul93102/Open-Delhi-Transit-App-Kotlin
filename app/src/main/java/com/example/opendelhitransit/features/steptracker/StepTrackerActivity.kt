package com.example.opendelhitransit.features.steptracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.opendelhitransit.R

class StepTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_tracker)
        
        // Get height from intent or use default
        val height = intent.getIntExtra("height", 170)
        
        // Redirect to CountStepsActivity with the height parameter
        val intent = Intent(this, CountStepsActivity::class.java).apply {
            putExtra("height", height)
        }
        startActivity(intent)
        finish() // Close this activity after redirecting
    }
} 