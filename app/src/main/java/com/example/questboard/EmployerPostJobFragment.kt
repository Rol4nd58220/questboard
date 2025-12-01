package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Employer Post Job Fragment
 * Displays: Form to create new job posting
 */
class EmployerPostJobFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Create a simple layout for posting jobs
        // For now, show a message. You can create a dedicated layout later
        return inflater.inflate(R.layout.fragment_employer_post_job, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize form views
        // Example: Job title, description, pay rate, requirements, location
    }
}

