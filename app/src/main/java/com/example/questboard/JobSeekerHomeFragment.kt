package com.example.questboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Job Seeker Home Fragment
 * Displays: Available jobs from employers that job seekers can apply to
 */
class JobSeekerHomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var recyclerView: RecyclerView? = null
    private var adapter: JobSeekerJobsAdapter? = null
    private var progressBar: ProgressBar? = null
    private var tvNoJobs: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobseeker_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.d("JobSeekerHomeFragment", "onViewCreated started")

        try {
            // Initialize Firebase
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            android.util.Log.d("JobSeekerHomeFragment", "Firebase initialized")

            // Initialize views safely
            try {
                recyclerView = view.findViewById(R.id.recyclerAvailableJobs)
                progressBar = view.findViewById(R.id.progressBarJobs)
                tvNoJobs = view.findViewById(R.id.tvNoJobsAvailable)

                android.util.Log.d("JobSeekerHomeFragment", "Views found - RecyclerView: ${recyclerView != null}, ProgressBar: ${progressBar != null}, TextView: ${tvNoJobs != null}")
            } catch (e: Exception) {
                android.util.Log.e("JobSeekerHomeFragment", "Error finding views: ${e.message}", e)
                Toast.makeText(context, "Error loading view components", Toast.LENGTH_SHORT).show()
                return
            }

            // Setup RecyclerView only if it exists
            recyclerView?.let { rv ->
                android.util.Log.d("JobSeekerHomeFragment", "Setting up RecyclerView")

                try {
                    rv.layoutManager = LinearLayoutManager(context)
                    adapter = JobSeekerJobsAdapter(
                        mutableListOf(),
                        onApplyClick = { job ->
                            android.util.Log.d("JobSeekerHomeFragment", "Apply clicked for job: ${job.title}")
                            applyToJob(job)
                        },
                        onViewDetailsClick = { job ->
                            android.util.Log.d("JobSeekerHomeFragment", "View details clicked for job: ${job.title}")
                            showJobDetails(job)
                        }
                    )
                    rv.adapter = adapter

                    android.util.Log.d("JobSeekerHomeFragment", "RecyclerView setup complete, loading jobs...")

                    // Load available jobs
                    loadAvailableJobs()
                } catch (e: Exception) {
                    android.util.Log.e("JobSeekerHomeFragment", "Error setting up RecyclerView: ${e.message}", e)
                    Toast.makeText(context, "Error setting up jobs list: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } ?: run {
                // RecyclerView not found - show error
                android.util.Log.e("JobSeekerHomeFragment", "RecyclerView not found in layout!")
                Toast.makeText(context, "Unable to load jobs view. Please restart the app.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("JobSeekerHomeFragment", "Critical error in onViewCreated: ${e.message}", e)
            Toast.makeText(context, "Critical error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadAvailableJobs()
    }

    private fun loadAvailableJobs() {
        progressBar?.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE
        tvNoJobs?.visibility = View.GONE

        // Load all open/active jobs - SIMPLIFIED to avoid Firestore index errors
        firestore.collection("jobs")
            .whereEqualTo("status", "Open")
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                progressBar?.visibility = View.GONE

                if (error != null) {
                    android.util.Log.e("JobSeekerHomeFragment", "Firestore error: ${error.message}", error)
                    Toast.makeText(context, "Error loading jobs: ${error.message}", Toast.LENGTH_SHORT).show()
                    tvNoJobs?.visibility = View.VISIBLE
                    tvNoJobs?.text = "Unable to load jobs. Please try again later."
                    return@addSnapshotListener
                }

                try {
                    if (snapshot != null && !snapshot.isEmpty) {
                        val jobs = snapshot.toObjects(Job::class.java)
                        android.util.Log.d("JobSeekerHomeFragment", "Loaded ${jobs.size} jobs")

                        if (adapter != null) {
                            adapter?.updateJobs(jobs)
                            recyclerView?.visibility = View.VISIBLE
                            tvNoJobs?.visibility = View.GONE
                        } else {
                            android.util.Log.e("JobSeekerHomeFragment", "Adapter is null!")
                        }
                    } else {
                        android.util.Log.d("JobSeekerHomeFragment", "No jobs found")
                        recyclerView?.visibility = View.GONE
                        tvNoJobs?.visibility = View.VISIBLE
                        tvNoJobs?.text = "No jobs available at the moment."
                    }
                } catch (e: Exception) {
                    android.util.Log.e("JobSeekerHomeFragment", "Error processing jobs: ${e.message}", e)
                    Toast.makeText(context, "Error displaying jobs", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun applyToJob(job: Job) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login to apply", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if already applied
        firestore.collection("applications")
            .whereEqualTo("jobId", job.id)
            .whereEqualTo("applicantId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(context, "You have already applied to this job", Toast.LENGTH_SHORT).show()
                } else {
                    submitApplication(job, currentUser.uid)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error checking application: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitApplication(job: Job, applicantId: String) {
        // Get applicant details
        firestore.collection("users").document(applicantId)
            .get()
            .addOnSuccessListener { userDoc ->
                val applicantName = userDoc.getString("fullName") ?: "Unknown"
                val applicantEmail = userDoc.getString("email") ?: ""

                // Create application
                val application = hashMapOf(
                    "jobId" to job.id,
                    "jobTitle" to job.title,
                    "employerId" to job.employerId,
                    "employerName" to job.employerName,
                    "applicantId" to applicantId,
                    "applicantName" to applicantName,
                    "applicantEmail" to applicantEmail,
                    "status" to "Pending",
                    "appliedAt" to Timestamp.now(),
                    "message" to "I would like to apply for this position."
                )

                firestore.collection("applications")
                    .add(application)
                    .addOnSuccessListener {
                        // Update applicants count in job
                        firestore.collection("jobs").document(job.id)
                            .update("applicantsCount", job.applicantsCount + 1)

                        Toast.makeText(context, "Application submitted successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to submit application: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showJobDetails(job: Job) {
        val detailsView = LayoutInflater.from(context).inflate(R.layout.dialog_job_details, null)

        // Populate job details
        detailsView.findViewById<TextView>(R.id.tvDialogJobTitle).text = job.title
        detailsView.findViewById<TextView>(R.id.tvDialogEmployerName).text = "Employer: ${job.employerName}"
        detailsView.findViewById<TextView>(R.id.tvDialogCategory).text = "Category: ${job.category}"
        detailsView.findViewById<TextView>(R.id.tvDialogLocation).text = "Location: ${job.location}"
        detailsView.findViewById<TextView>(R.id.tvDialogAmount).text = "Pay: ₱${String.format(java.util.Locale.US, "%.2f", job.amount)} / ${job.paymentType}"
        detailsView.findViewById<TextView>(R.id.tvDialogDateTime).text = "Date/Time: ${job.dateTime}"
        detailsView.findViewById<TextView>(R.id.tvDialogDescription).text = job.description
        detailsView.findViewById<TextView>(R.id.tvDialogRequirements).text = if (job.requirements.isNotEmpty()) job.requirements else "No specific requirements"

        AlertDialog.Builder(requireContext())
            .setView(detailsView)
            .setPositiveButton("Apply") { _, _ -> applyToJob(job) }
            .setNegativeButton("Close", null)
            .show()
    }
}


