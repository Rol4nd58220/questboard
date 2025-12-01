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

        try {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            // Check if user is logged in
            if (auth.currentUser == null) {
                android.util.Log.w("ProfileFragment", "User not logged in")
                Toast.makeText(context, "Please login again", Toast.LENGTH_SHORT).show()
                return
            }

            // Load user profile data
            loadUserProfile(view)

            // Set up logout button if it exists
            view.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
                logoutUser()
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile(view: View) {
        try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                android.util.Log.w("ProfileFragment", "No user ID available")
                return
            }

            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    try {
                        if (document.exists()) {
                            val firstName = document.getString("firstName") ?: ""
                            val lastName = document.getString("lastName") ?: ""
                            val fullName = document.getString("fullName") ?: "$firstName $lastName"

                            // Update UI with user data - safely
                            view.findViewById<TextView>(R.id.txtName)?.text = fullName.ifEmpty { "User" }

                            android.util.Log.d("ProfileFragment", "Profile loaded: $fullName")
                        } else {
                            android.util.Log.w("ProfileFragment", "User document does not exist")
                            // Set default name
                            view.findViewById<TextView>(R.id.txtName)?.text = "User Profile"
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileFragment", "Error processing profile data: ${e.message}", e)
                        view.findViewById<TextView>(R.id.txtName)?.text = "User"
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("ProfileFragment", "Error loading profile: ${e.message}", e)
                    // Don't crash - just show error
                    Toast.makeText(context, "Could not load profile", Toast.LENGTH_SHORT).show()
                    view.findViewById<TextView>(R.id.txtName)?.text = "User"
                }
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error in loadUserProfile: ${e.message}", e)
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

