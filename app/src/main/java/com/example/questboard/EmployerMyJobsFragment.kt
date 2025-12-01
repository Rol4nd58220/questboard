package com.example.questboard

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Employer My Jobs Fragment
 * Displays: Posted jobs, active listings, job management with CRUD operations
 */
class EmployerMyJobsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmployerJobsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoJobs: TextView
    private lateinit var tvTotalJobsCount: TextView
    private lateinit var tvActiveJobsCount: TextView
    private lateinit var tvPendingApplicationsCount: TextView
    private lateinit var tvCompletedJobsCount: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employer_my_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerRecentJobs)
        progressBar = view.findViewById(R.id.progressBar) ?: ProgressBar(requireContext()).apply {
            visibility = View.GONE
        }
        tvNoJobs = view.findViewById(R.id.tvNoJobs) ?: TextView(requireContext()).apply {
            text = "No jobs posted yet"
            visibility = View.GONE
        }

        tvTotalJobsCount = view.findViewById(R.id.tvTotalJobsCount)
        tvActiveJobsCount = view.findViewById(R.id.tvActiveJobsCount)
        tvPendingApplicationsCount = view.findViewById(R.id.tvPendingApplicationsCount)
        tvCompletedJobsCount = view.findViewById(R.id.tvCompletedJobsCount)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = EmployerJobsAdapter(
            mutableListOf(),
            onEditClick = { job -> editJob(job) },
            onDeleteClick = { job -> deleteJob(job) },
            onViewApplicantsClick = { job -> viewApplicants(job) }
        )
        recyclerView.adapter = adapter

        // Load jobs
        loadJobs()
    }

    override fun onResume() {
        super.onResume()
        loadJobs()
    }

    private fun loadJobs() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoJobs.visibility = View.GONE

        firestore.collection("jobs")
            .whereEqualTo("employerId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(context, "Error loading jobs: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val jobs = snapshot.toObjects(Job::class.java)
                    adapter.updateJobs(jobs)
                    recyclerView.visibility = View.VISIBLE
                    tvNoJobs.visibility = View.GONE

                    // Update statistics
                    updateStatistics(jobs)
                } else {
                    recyclerView.visibility = View.GONE
                    tvNoJobs.visibility = View.VISIBLE
                    updateStatistics(emptyList())
                }
            }
    }

    private fun updateStatistics(jobs: List<Job>) {
        tvTotalJobsCount.text = jobs.size.toString()
        tvActiveJobsCount.text = jobs.count { it.status == "Open" }.toString()
        tvPendingApplicationsCount.text = jobs.sumOf { it.applicantsCount }.toString()
        tvCompletedJobsCount.text = jobs.count { it.status == "Completed" }.toString()
    }

    private fun editJob(job: Job) {
        // Navigate to Post Job fragment with job data
        val fragment = EmployerPostJobFragment()
        val bundle = Bundle().apply {
            putString("jobId", job.id)
        }
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_employer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteJob(job: Job) {
        firestore.collection("jobs").document(job.id)
            .delete()
            .addOnSuccessListener {
                adapter.removeJob(job)
                Toast.makeText(context, "Job deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete job: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun viewApplicants(job: Job) {
        // TODO: Navigate to applicants screen for this specific job
        Toast.makeText(context, "View applicants for: ${job.title}", Toast.LENGTH_SHORT).show()

        // Navigate to applicants fragment if available
        (activity as? EmployerDashboardActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            R.id.bottom_navigation_employer
        )?.selectedItemId = R.id.nav_applicants
    }
}


