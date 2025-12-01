package com.example.questboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Job Seeker Messages Fragment
 * Displays: Conversations with employers, chat history, notifications
 */
class JobSeekerMessagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobseeker_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearchMessages = view.findViewById<EditText>(R.id.etSearchMessages)
        val fabAddMessage = view.findViewById<FloatingActionButton>(R.id.fabAddMessage)

        // Search functionality
        etSearchMessages?.setOnEditorActionListener { _, _, _ ->
            val query = etSearchMessages.text.toString()
            if (query.isNotEmpty()) {
                Toast.makeText(context, "Searching messages: $query", Toast.LENGTH_SHORT).show()
                // TODO: Implement message search
            }
            false
        }

        // FAB click listener
        fabAddMessage?.setOnClickListener {
            Toast.makeText(context, "New Message", Toast.LENGTH_SHORT).show()
            // TODO: Open new message dialog/activity
        }
    }
}

