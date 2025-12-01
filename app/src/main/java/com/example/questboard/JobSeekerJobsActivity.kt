package com.example.questboard

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

/**
 * JobSeekerJobsActivity
 * Displays job seeker jobs with Applied/Active tabs
 */
class JobSeekerJobsActivity : AppCompatActivity() {

    private lateinit var tabAppliedContainer: LinearLayout
    private lateinit var tabActiveContainer: LinearLayout
    private lateinit var tabApplied: TextView
    private lateinit var tabActive: TextView
    private lateinit var underlineApplied: View
    private lateinit var underlineActive: View

    private var isAppliedTabSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobseeker_jobs)

        // Initialize views
        tabAppliedContainer = findViewById(R.id.tabAppliedContainer)
        tabActiveContainer = findViewById(R.id.tabActiveContainer)
        tabApplied = findViewById(R.id.tabApplied)
        tabActive = findViewById(R.id.tabActive)
        underlineApplied = findViewById(R.id.underlineApplied)
        underlineActive = findViewById(R.id.underlineActive)

        // Set up tab click listeners
        setupTabListeners()
    }

    private fun setupTabListeners() {
        tabAppliedContainer.setOnClickListener {
            if (!isAppliedTabSelected) {
                selectAppliedTab()
            }
        }

        tabActiveContainer.setOnClickListener {
            if (isAppliedTabSelected) {
                selectActiveTab()
            }
        }
    }

    private fun selectAppliedTab() {
        isAppliedTabSelected = true

        // Applied tab - 100% opacity
        tabApplied.alpha = 1.0f
        underlineApplied.alpha = 1.0f
        tabApplied.setTextColor(resources.getColor(android.R.color.white, null))

        // Active tab - 50% opacity
        tabActive.alpha = 0.5f
        underlineActive.alpha = 0.5f

        Toast.makeText(this, "Applied Jobs", Toast.LENGTH_SHORT).show()
        // TODO: Load applied jobs data
    }

    private fun selectActiveTab() {
        isAppliedTabSelected = false

        // Applied tab - 50% opacity
        tabApplied.alpha = 0.5f
        underlineApplied.alpha = 0.5f

        // Active tab - 100% opacity
        tabActive.alpha = 1.0f
        underlineActive.alpha = 1.0f
        tabActive.setTextColor(resources.getColor(android.R.color.white, null))

        Toast.makeText(this, "Active Jobs", Toast.LENGTH_SHORT).show()
        // TODO: Load active jobs data
    }
}

