package com.example.questboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Employer Messages Fragment
 * Displays: Conversations with job seekers, chat history
 */
class EmployerMessagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Using employer messages fragment layout
        return inflater.inflate(R.layout.fragment_employer_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize views and set up functionality
        // Example: Chat list, conversations with applicants
    }
}

