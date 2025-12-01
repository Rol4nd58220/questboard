package com.example.questboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Job Seeker Jobs Fragment
 * Displays: Available jobs, job search, filters, applications status
 */
class JobSeekerJobsFragment : Fragment() {

    private var isAppliedTabSelected = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobseeker_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabAppliedContainer = view.findViewById<LinearLayout>(R.id.tabAppliedContainer)
        val tabActiveContainer = view.findViewById<LinearLayout>(R.id.tabActiveContainer)
        val tabApplied = view.findViewById<TextView>(R.id.tabApplied)
        val tabActive = view.findViewById<TextView>(R.id.tabActive)
        val underlineApplied = view.findViewById<View>(R.id.underlineApplied)
        val underlineActive = view.findViewById<View>(R.id.underlineActive)
        val tvJobsContent = view.findViewById<TextView>(R.id.tvJobsContent)

        // Set up tab click listeners
        tabAppliedContainer?.setOnClickListener {
            if (!isAppliedTabSelected) {
                selectAppliedTab(tabApplied, tabActive, underlineApplied, underlineActive, tvJobsContent)
            }
        }

        tabActiveContainer?.setOnClickListener {
            if (isAppliedTabSelected) {
                selectActiveTab(tabApplied, tabActive, underlineApplied, underlineActive, tvJobsContent)
            }
        }
    }

    private fun selectAppliedTab(
        tabApplied: TextView,
        tabActive: TextView,
        underlineApplied: View,
        underlineActive: View,
        tvJobsContent: TextView
    ) {
        isAppliedTabSelected = true

        // Applied tab - 100% opacity
        tabApplied.alpha = 1.0f
        underlineApplied.alpha = 1.0f
        tabApplied.setTextAppearance(android.R.style.TextAppearance_Medium)

        // Active tab - 50% opacity
        tabActive.alpha = 0.5f
        underlineActive.alpha = 0.5f

        tvJobsContent.text = "Applied Jobs"
        Toast.makeText(context, "Showing Applied Jobs", Toast.LENGTH_SHORT).show()
        // TODO: Load applied jobs data
    }

    private fun selectActiveTab(
        tabApplied: TextView,
        tabActive: TextView,
        underlineApplied: View,
        underlineActive: View,
        tvJobsContent: TextView
    ) {
        isAppliedTabSelected = false

        // Applied tab - 50% opacity
        tabApplied.alpha = 0.5f
        underlineApplied.alpha = 0.5f

        // Active tab - 100% opacity
        tabActive.alpha = 1.0f
        underlineActive.alpha = 1.0f
        tabActive.setTextAppearance(android.R.style.TextAppearance_Medium)

        tvJobsContent.text = "Active Jobs"
        Toast.makeText(context, "Showing Active Jobs", Toast.LENGTH_SHORT).show()
        // TODO: Load active jobs data
    }
}

