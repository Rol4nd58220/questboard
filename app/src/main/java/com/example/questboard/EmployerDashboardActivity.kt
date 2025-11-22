package com.example.questboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class EmployerDashboardActivity : AppCompatActivity() {

    private lateinit var rvActiveJobPosts: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvAppName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employer_dashboard)

        // Initialize views
        rvActiveJobPosts = findViewById(R.id.rvActiveJobPosts)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        tvAppName = findViewById(R.id.tvAppName)

        // Set QuestBoard title with colored "Q"
        UIHelper.setQuestBoardTitle(tvAppName)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup bottom navigation
        setupBottomNavigation()

        // Load data
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        rvActiveJobPosts.layoutManager = LinearLayoutManager(this)

        // Sample data for demonstration
        val sampleJobPosts = listOf(
            JobPost(
                id = "1",
                title = "Errand Runner",
                postedTime = "2 days ago",
                status = "Open",
                applicantsCount = 3,
                imageUrl = null
            ),
            JobPost(
                id = "2",
                title = "Event Setup Crew",
                postedTime = "5 days ago",
                status = "Open",
                applicantsCount = 3,
                imageUrl = null
            ),
            JobPost(
                id = "3",
                title = "Construction Helper",
                postedTime = "1 week ago",
                status = "Open",
                applicantsCount = 10,
                imageUrl = null
            )
        )

        val adapter = ActiveJobPostsAdapter(sampleJobPosts) { jobPost ->
            // Handle view applicants click
            // TODO: Navigate to applicants screen
        }

        rvActiveJobPosts.adapter = adapter
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_my_jobs -> {
                    // Already on this screen
                    true
                }
                R.id.nav_applicants -> {
                    // Navigate to Applicants screen
                    true
                }
                R.id.nav_post_job -> {
                    // Navigate to Post Job screen
                    true
                }
                R.id.nav_messages -> {
                    // Navigate to Messages screen
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to Profile screen
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDashboardData() {
        // TODO: Load statistics from database/API
        // Total Jobs Posted: 12
        // Active Jobs: 5
        // Pending Applicants: 8
        // Completed Jobs: 7
    }
}

