package com.example.questboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Job Seeker Home Fragment
 * Displays: Featured jobs, recent activities, quick stats, recommendations
 */
class JobSeekerHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Temporarily using simple layout for testing
        return inflater.inflate(R.layout.fragment_jobseeker_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize views and set up functionality
        // Example: Featured jobs, recent activities, quick stats
    }
}

