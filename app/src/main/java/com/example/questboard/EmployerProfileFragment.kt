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
 * Employer Profile Fragment
 * Displays: Business profile, settings, stats, logout
 */
class EmployerProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employer_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Load employer profile data
        loadEmployerProfile(view)

        // Set up logout button
        view.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadEmployerProfile(view: View) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")
                    val businessPermitType = document.getString("businessPermitType")

                    // Update UI with employer data
                    view.findViewById<TextView>(R.id.tvEmployerName)?.text = "$firstName $lastName"
                    view.findViewById<TextView>(R.id.tvBusinessType)?.text = businessPermitType
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

