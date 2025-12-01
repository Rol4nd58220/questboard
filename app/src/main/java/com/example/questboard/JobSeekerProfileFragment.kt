package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Job Seeker Profile Fragment
 * Displays: User profile, stats, settings, logout
 */
class JobSeekerProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobseeker_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Load user profile data
        loadUserProfile(view)

        // Set up logout button if it exists
        view.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserProfile(view: View) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")
                    val phone = document.getString("phone")

                    // Update UI with user data
                    view.findViewById<TextView>(R.id.txtName)?.text = "$firstName $lastName"
                    // Add more fields as needed based on your layout
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logoutUser() {
        auth.signOut()

        // Navigate back to login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}

