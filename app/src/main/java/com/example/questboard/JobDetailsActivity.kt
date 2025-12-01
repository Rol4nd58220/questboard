package com.example.questboard

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Job Details Activity
 * Shows complete job information when JobSeeker clicks "View" on a job card
 */
class JobDetailsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var jobId: String? = null
    private var currentApplication: Application? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get job ID from intent
        jobId = intent.getStringExtra("JOB_ID")

        if (jobId == null) {
            Toast.makeText(this, "Error: Job not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        initializeViews()

        // Load job details
        loadJobDetails()

        // Check if user already applied
        checkIfAlreadyApplied()
    }

    private fun initializeViews() {
        // Close button
        findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            finish()
        }

        // Apply button
        findViewById<Button>(R.id.btnApply).setOnClickListener {
            applyToJob()
        }

        // Cancel application button
        findViewById<Button>(R.id.btnCancelApplication).setOnClickListener {
            showCancelConfirmation()
        }
    }

    private fun checkIfAlreadyApplied() {
        val currentUser = auth.currentUser ?: return
        val jobId = this.jobId ?: return

        db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .whereEqualTo("applicantId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    // User already applied
                    val application = snapshot.documents[0].toObject(Application::class.java)
                    currentApplication = application

                    // Hide apply button, show cancel button
                    findViewById<Button>(R.id.btnApply).visibility = android.view.View.GONE
                    findViewById<Button>(R.id.btnCancelApplication).visibility = android.view.View.VISIBLE

                    // Update button text based on status
                    when (application?.status) {
                        "Pending" -> {
                            findViewById<Button>(R.id.btnCancelApplication).text = "Cancel Application"
                        }
                        "Accepted" -> {
                            findViewById<Button>(R.id.btnCancelApplication).visibility = android.view.View.GONE
                            Toast.makeText(this, "Your application has been accepted!", Toast.LENGTH_LONG).show()
                        }
                        "Rejected" -> {
                            findViewById<Button>(R.id.btnCancelApplication).visibility = android.view.View.GONE
                        }
                    }
                }
            }
    }

    private fun showCancelConfirmation() {
        val application = currentApplication ?: return

        android.app.AlertDialog.Builder(this)
            .setTitle("Cancel Application")
            .setMessage("Are you sure you want to cancel your application for this job?\n\nThis action cannot be undone.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelApplication()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelApplication() {
        val application = currentApplication ?: return

        db.collection("applications").document(application.id)
            .delete()
            .addOnSuccessListener {
                // Update job applicants count
                val jobId = this.jobId
                if (jobId != null) {
                    db.collection("jobs").document(jobId)
                        .get()
                        .addOnSuccessListener { doc ->
                            val currentCount = doc.getLong("applicantsCount")?.toInt() ?: 0
                            if (currentCount > 0) {
                                db.collection("jobs").document(jobId)
                                    .update("applicantsCount", currentCount - 1)
                            }
                        }
                }

                Toast.makeText(this, "Application cancelled successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("JobDetailsActivity", "Error cancelling: ${e.message}", e)
                Toast.makeText(this, "Failed to cancel application", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadJobDetails() {
        val jobId = this.jobId ?: return

        db.collection("jobs").document(jobId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val job = document.toObject(Job::class.java)
                        if (job != null) {
                            displayJobDetails(job)
                        } else {
                            Toast.makeText(this, "Error loading job details", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("JobDetailsActivity", "Error parsing job: ${e.message}", e)
                        Toast.makeText(this, "Error displaying job", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("JobDetailsActivity", "Error loading job: ${e.message}", e)
                Toast.makeText(this, "Error loading job details", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displayJobDetails(job: Job) {
        // Job Image - placeholder for now
        val imgJobPhoto = findViewById<ImageView>(R.id.imgJobPhoto)
        // TODO: Load from Cloudinary when implemented
        // imgJobPhoto.load(job.imageUrl)

        // Job Title
        findViewById<TextView>(R.id.tvJobTitle).text = job.title

        // Employer Name
        findViewById<TextView>(R.id.tvEmployerName).text = job.employerName

        // Job Description
        findViewById<TextView>(R.id.tvJobDescription).text = job.description

        // Payment Type
        findViewById<TextView>(R.id.tvPaymentType).text = job.paymentType

        // Amount Offered
        findViewById<TextView>(R.id.tvAmount).text = "₱${String.format("%.2f", job.amount)}"

        // Job Category
        findViewById<TextView>(R.id.tvJobCategory).text = job.category

        // Date and Time
        findViewById<TextView>(R.id.tvDateTime).text = job.dateTime

        // Job Location
        findViewById<TextView>(R.id.tvJobLocation).text = job.location

        // Requirements
        val requirements = job.requirements.ifEmpty { "No specific requirements" }
        findViewById<TextView>(R.id.tvRequirements).text = requirements
    }

    private fun applyToJob() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to apply", Toast.LENGTH_SHORT).show()
            return
        }

        val jobId = this.jobId ?: return

        // Get custom message from input field
        val customMessage = findViewById<android.widget.EditText>(R.id.etApplicationMessage)?.text?.toString()?.trim() ?: ""
        val applicationMessage = if (customMessage.isNotEmpty()) {
            customMessage
        } else {
            "I would like to apply for this position."
        }

        // Get job details first
        db.collection("jobs").document(jobId)
            .get()
            .addOnSuccessListener { jobDoc ->
                if (!jobDoc.exists()) {
                    Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val job = jobDoc.toObject(Job::class.java)
                if (job == null) {
                    Toast.makeText(this, "Error loading job", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Check if already applied
                db.collection("applications")
                    .whereEqualTo("jobId", jobId)
                    .whereEqualTo("applicantId", currentUser.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            Toast.makeText(this, "You have already applied to this job", Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }

                        // Get user details
                        db.collection("users").document(currentUser.uid)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val fullName = userDoc.getString("fullName") ?: "User"
                                val phone = userDoc.getString("phone") ?: ""

                                // Create application
                                val application = Application(
                                    jobId = jobId,
                                    jobTitle = job.title,
                                    employerId = job.employerId,
                                    employerName = job.employerName,
                                    employerEmail = job.employerEmail,
                                    applicantId = currentUser.uid,
                                    applicantName = fullName,
                                    applicantEmail = currentUser.email ?: "",
                                    applicantPhone = phone,
                                    status = "Pending",
                                    appliedAt = com.google.firebase.Timestamp.now(),
                                    message = applicationMessage,  // Use custom message or default
                                    isRead = false,
                                    notificationSent = false
                                )

                                // Save application to Firestore
                                db.collection("applications")
                                    .add(application)
                                    .addOnSuccessListener {
                                        // Update applicants count
                                        db.collection("jobs").document(jobId)
                                            .update("applicantsCount", job.applicantsCount + 1)

                                        Toast.makeText(
                                            this,
                                            "Application submitted successfully!",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Close activity and return to previous screen
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        android.util.Log.e("JobDetailsActivity", "Error submitting application: ${e.message}", e)
                                        Toast.makeText(
                                            this,
                                            "Failed to submit application. Please try again.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("JobDetailsActivity", "Error getting user details: ${e.message}", e)
                                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("JobDetailsActivity", "Error checking applications: ${e.message}", e)
                        Toast.makeText(this, "Error checking application status", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("JobDetailsActivity", "Error loading job: ${e.message}", e)
                Toast.makeText(this, "Error loading job details", Toast.LENGTH_SHORT).show()
            }
    }
}

