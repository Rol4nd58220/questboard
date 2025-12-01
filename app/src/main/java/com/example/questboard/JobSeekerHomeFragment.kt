package com.example.questboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Job Seeker Home Fragment
 * Displays: Recently viewed jobs, pending applications, and available jobs FROM FIRESTORE
 */
class JobSeekerHomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var recyclerRecentlyViewed: RecyclerView
    private lateinit var recyclerPendingApplications: RecyclerView
    private lateinit var recyclerAvailableJobs: RecyclerView

    private val recentlyViewedJobs = mutableListOf<Job>()
    private val pendingApplications = mutableListOf<Application>()
    private val availableJobs = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobseeker_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            // Initialize RecyclerViews safely
            recyclerRecentlyViewed = view.findViewById(R.id.recyclerRecentlyViewed) ?: run {
                android.util.Log.e("HomeFragment", "recyclerRecentlyViewed not found!")
                return
            }
            recyclerPendingApplications = view.findViewById(R.id.recyclerPendingApplications) ?: run {
                android.util.Log.e("HomeFragment", "recyclerPendingApplications not found!")
                return
            }
            recyclerAvailableJobs = view.findViewById(R.id.recyclerAvailableJobs) ?: run {
                android.util.Log.e("HomeFragment", "recyclerAvailableJobs not found!")
                return
            }

            // Set up horizontal layout managers for first two sections
            recyclerRecentlyViewed.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerPendingApplications.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerAvailableJobs.layoutManager = LinearLayoutManager(context)

            // Set up "See All" click listeners
            view.findViewById<TextView>(R.id.tvSeeAllRecent)?.setOnClickListener {
                Toast.makeText(context, "Show all recently viewed jobs", Toast.LENGTH_SHORT).show()
            }

            view.findViewById<TextView>(R.id.tvSeeAllPending)?.setOnClickListener {
                Toast.makeText(context, "Show all pending applications", Toast.LENGTH_SHORT).show()
            }

            view.findViewById<TextView>(R.id.tvSeeAllAvailable)?.setOnClickListener {
                Toast.makeText(context, "Show all available jobs", Toast.LENGTH_SHORT).show()
            }

            // Load REAL data from Firestore
            loadRecentlyViewedJobs()
            loadPendingApplications()
            loadAvailableJobs()

        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(context, "Error loading home page: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadRecentlyViewedJobs() {
        try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                android.util.Log.w("HomeFragment", "User not logged in")
                loadSampleRecentlyViewed()
                return
            }

            // For now, just show most recent jobs as "Recently Viewed"
            // TODO: Implement actual recently viewed tracking
            loadSampleRecentlyViewed()

        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error loading recently viewed: ${e.message}", e)
            loadSampleRecentlyViewed()
        }
    }

    private fun loadSampleRecentlyViewed() {
        try {
            // Show most recent jobs as suggestion
            db.collection("jobs")
                .whereEqualTo("status", "Open")
                .limit(5)
                .get()
                .addOnSuccessListener { snapshot ->
                    recentlyViewedJobs.clear()
                    if (snapshot != null && !snapshot.isEmpty) {
                        recentlyViewedJobs.addAll(snapshot.toObjects(Job::class.java))
                    }
                    updateRecentlyViewedAdapter()
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("HomeFragment", "Error loading sample jobs: ${e.message}", e)
                    updateRecentlyViewedAdapter() // Update with empty list
                }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in loadSampleRecentlyViewed: ${e.message}", e)
            updateRecentlyViewedAdapter()
        }
    }

    private fun updateRecentlyViewedAdapter() {
        try {
            val adapter = RecentlyViewedJobsAdapter(recentlyViewedJobs) { job ->
                Toast.makeText(context, "Viewing: ${job.title}", Toast.LENGTH_SHORT).show()
                saveAsRecentlyViewed(job)
            }
            recyclerRecentlyViewed.adapter = adapter
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating recently viewed adapter: ${e.message}", e)
        }
    }

    private fun saveAsRecentlyViewed(job: Job) {
        try {
            val userId = auth.currentUser?.uid ?: return

            val viewedData = hashMapOf(
                "jobId" to job.id,
                "viewedAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("recentlyViewed")
                .document(userId)
                .collection("jobs")
                .document(job.id)
                .set(viewedData)
                .addOnFailureListener { e ->
                    android.util.Log.e("HomeFragment", "Error saving recently viewed: ${e.message}", e)
                }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in saveAsRecentlyViewed: ${e.message}", e)
        }
    }

    private fun loadPendingApplications() {
        try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                android.util.Log.w("HomeFragment", "User not logged in for applications")
                updatePendingApplicationsAdapter()
                return
            }

            // Load user's pending applications from Firestore
            db.collection("applications")
                .whereEqualTo("applicantId", userId)
                .whereEqualTo("status", "Pending")
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("HomeFragment", "Error loading applications: ${error.message}", error)
                        updatePendingApplicationsAdapter() // Update with empty list
                        return@addSnapshotListener
                    }

                    pendingApplications.clear()

                    if (snapshot != null && !snapshot.isEmpty) {
                        try {
                            pendingApplications.addAll(snapshot.toObjects(Application::class.java))
                        } catch (e: Exception) {
                            android.util.Log.e("HomeFragment", "Error parsing applications: ${e.message}", e)
                        }
                    }

                    updatePendingApplicationsAdapter()
                }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in loadPendingApplications: ${e.message}", e)
            updatePendingApplicationsAdapter()
        }
    }

    private fun updatePendingApplicationsAdapter() {
        try {
            val adapter = PendingApplicationsAdapter(pendingApplications) { application ->
                Toast.makeText(context, "Viewing application: ${application.jobTitle}", Toast.LENGTH_SHORT).show()
            }
            recyclerPendingApplications.adapter = adapter
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating applications adapter: ${e.message}", e)
        }
    }

    private fun loadAvailableJobs() {
        try {
            // Load all available/open jobs posted by employers from Firestore
            db.collection("jobs")
                .whereEqualTo("status", "Open")
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("HomeFragment", "Error loading jobs: ${error.message}", error)
                        Toast.makeText(context, "Error loading jobs. Please try again.", Toast.LENGTH_SHORT).show()
                        updateAvailableJobsAdapter() // Update with empty list
                        return@addSnapshotListener
                    }

                    availableJobs.clear()

                    if (snapshot != null && !snapshot.isEmpty) {
                        try {
                            availableJobs.addAll(snapshot.toObjects(Job::class.java))
                            android.util.Log.d("HomeFragment", "Loaded ${availableJobs.size} jobs")
                        } catch (e: Exception) {
                            android.util.Log.e("HomeFragment", "Error parsing jobs: ${e.message}", e)
                        }
                    } else {
                        android.util.Log.d("HomeFragment", "No jobs available")
                    }

                    updateAvailableJobsAdapter()
                }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in loadAvailableJobs: ${e.message}", e)
            updateAvailableJobsAdapter()
        }
    }

    private fun updateAvailableJobsAdapter() {
        try {
            val adapter = AvailableJobsAdapter(availableJobs) { job ->
                Toast.makeText(context, "Viewing: ${job.title}", Toast.LENGTH_SHORT).show()
                saveAsRecentlyViewed(job)
            }
            recyclerAvailableJobs.adapter = adapter
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating jobs adapter: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Data will auto-update via Firestore listeners
    }
}

