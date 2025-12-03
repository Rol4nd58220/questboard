package com.example.questboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

/**
 * Job Seeker Profile Fragment
 * Displays: User profile, stats, settings, logout
 */
class JobSeekerProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedImageUri: Uri? = null

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Reload profile data after successful edit (includes rating)
            loadUserProfile()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                view?.findViewById<ImageView>(R.id.imgProfilePhoto)?.setImageURI(uri)
                // TODO: Upload photo when Firebase Storage is ready
                Toast.makeText(context, "Photo upload coming soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                Log.w("ProfileFragment", "User not logged in")
                Toast.makeText(context, "Please login again", Toast.LENGTH_SHORT).show()
                return
            }

            setupViews(view)
            loadUserProfile()
            // Rating is loaded within loadUserProfile

        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViews(view: View) {
        // Edit Profile button
        view.findViewById<Button>(R.id.btnEditProfile)?.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        // Logout button
        view.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            logoutUser()
        }

        // Change photo button
        view.findViewById<ImageView>(R.id.btnChangePhoto)?.setOnClickListener {
            openImagePicker()
        }

        // Profile photo click to view full size
        view.findViewById<ImageView>(R.id.imgProfilePhoto)?.setOnClickListener {
            // TODO: Open full-size photo viewer
        }
    }

    private fun loadUserProfile() {
        try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.w("ProfileFragment", "No user ID available")
                return
            }

            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    try {
                        if (document.exists()) {
                            val firstName = document.getString("firstName") ?: ""
                            val middleName = document.getString("middleName") ?: ""
                            val lastName = document.getString("lastName") ?: ""

                            // Build full name with middle name
                            val fullName = buildString {
                                append(firstName)
                                if (middleName.isNotEmpty()) {
                                    append(" ")
                                    append(middleName)
                                }
                                if (lastName.isNotEmpty()) {
                                    append(" ")
                                    append(lastName)
                                }
                            }.trim()

                            val email = auth.currentUser?.email ?: ""
                            val aboutMe = document.getString("bio") ?: document.getString("aboutMe") ?: "No bio available"
                            val location = document.getString("location") ?: "Not specified"
                            val profilePhotoUrl = document.getString("profilePhotoUrl")

                            // Get rating from user profile if available
                            val averageRating = document.getDouble("averageRating")
                            val totalReviews = document.getLong("totalReviews")?.toInt()
                            val jobsCompleted = document.getLong("jobsCompleted")?.toInt()

                            // Update UI
                            view?.findViewById<TextView>(R.id.txtName)?.text = fullName.ifEmpty { "User" }
                            view?.findViewById<TextView>(R.id.txtEmail)?.text = email
                            view?.findViewById<TextView>(R.id.txtAboutMe)?.text = aboutMe
                            view?.findViewById<TextView>(R.id.txtLocation)?.text = location

                            // Update rating UI if available from profile
                            if (averageRating != null && totalReviews != null && jobsCompleted != null) {
                                view?.findViewById<TextView>(R.id.txtRating)?.text = String.format(Locale.US, "%.1f", averageRating)
                                view?.findViewById<TextView>(R.id.txtReviewCount)?.text = getString(R.string.review_count, totalReviews)
                                view?.findViewById<TextView>(R.id.txtJobsCompleted)?.text = getString(R.string.jobs_completed, jobsCompleted)
                                view?.findViewById<RatingBar>(R.id.ratingBar)?.rating = averageRating.toFloat()

                                Log.d("ProfileFragment", "Rating loaded from profile: $averageRating from $totalReviews reviews")
                            } else {
                                // Fallback: Calculate rating from job completions (backward compatibility)
                                loadRatingData()
                            }

                            // Load profile photo if available
                            // TODO: Implement image loading when Glide is synced
                            if (profilePhotoUrl != null) {
                                Log.d("ProfileFragment", "Profile photo URL: $profilePhotoUrl")
                            }

                            Log.d("ProfileFragment", "Profile loaded: $fullName")
                        } else {
                            Log.w("ProfileFragment", "User document does not exist")
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error processing profile data: ${e.message}", e)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error loading profile: ${e.message}", e)
                    Toast.makeText(context, "Could not load profile", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error in loadUserProfile: ${e.message}", e)
        }
    }

    private fun loadRatingData() {
        val userId = auth.currentUser?.uid ?: return

        // Load job completions where the user was reviewed
        db.collection("jobCompletions")
            .whereEqualTo("jobSeekerId", userId)
            .whereEqualTo("employerReviewed", true)
            .get()
            .addOnSuccessListener { documents ->
                var totalRating = 0.0
                var reviewCount = 0
                var jobsCompleted = 0

                for (document in documents) {
                    jobsCompleted++
                    val rating = document.getDouble("employerRating")
                    if (rating != null && rating > 0) {
                        totalRating += rating
                        reviewCount++
                    }
                }

                val averageRating = if (reviewCount > 0) totalRating / reviewCount else 0.0

                // Update UI
                view?.findViewById<TextView>(R.id.txtRating)?.text = String.format(Locale.US, "%.1f", averageRating)
                view?.findViewById<TextView>(R.id.txtReviewCount)?.text = getString(R.string.review_count, reviewCount)
                view?.findViewById<TextView>(R.id.txtJobsCompleted)?.text = getString(R.string.jobs_completed, jobsCompleted)
                view?.findViewById<RatingBar>(R.id.ratingBar)?.rating = averageRating.toFloat()

                Log.d("ProfileFragment", "Rating loaded: $averageRating from $reviewCount reviews")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error loading rating: ${e.message}", e)
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun logoutUser() {
        try {
            // Sign out from Firebase
            auth.signOut()

            // Check if fragment is still attached to activity
            if (!isAdded || activity == null) {
                return
            }

            // Navigate back to login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Finish the activity safely
            activity?.finish()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error during logout: ${e.message}", e)
            // Still try to navigate to login even if there's an error
            try {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } catch (ex: Exception) {
                Log.e("ProfileFragment", "Failed to navigate to login: ${ex.message}", ex)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when fragment becomes visible
        loadUserProfile()
        // Rating is loaded within loadUserProfile, so no need to call loadRatingData separately
    }
}

