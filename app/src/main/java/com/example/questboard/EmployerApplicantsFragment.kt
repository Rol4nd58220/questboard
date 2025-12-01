package com.example.questboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Employer Applicants Fragment
 * Displays: All applicants across jobs, manage applications, shortlist candidates
 */
class EmployerApplicantsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Using the applicants activity layout
        return inflater.inflate(R.layout.activity_applicants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize views and set up functionality
        // Example: Applicant list, filter by job, shortlist, reject
    }
}

