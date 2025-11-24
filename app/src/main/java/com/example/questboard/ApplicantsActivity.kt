package com.example.questboard

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ApplicantsActivity : AppCompatActivity() {

    private lateinit var rvJobPosts: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var btnMenu: ImageView
    private lateinit var btnNotification: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applicants)

        // Initialize views
        initViews()

        // Setup UI
        setupRecyclerView()
        setupBottomNavigation()
        setupClickListeners()
    }

    private fun initViews() {
        rvJobPosts = findViewById(R.id.rvJobPosts)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        btnMenu = findViewById(R.id.btnMenu)
        btnNotification = findViewById(R.id.btnNotification)
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            // Open menu/drawer
        }

        btnNotification.setOnClickListener {
            // Open notifications
        }
    }

    private fun setupRecyclerView() {
        rvJobPosts.layoutManager = LinearLayoutManager(this)
        loadJobPosts()
    }

    private fun loadJobPosts() {
        // Sample data
        val jobPosts = listOf(
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

        val adapter = JobPostsAdapter(jobPosts) { _ ->
            // Navigate to specific job applicants view
            // For now, we can pass the job details to another activity
            // or show a different fragment
        }

        rvJobPosts.adapter = adapter
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_applicants
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_my_jobs -> {
                    finish()
                    true
                }
                R.id.nav_applicants -> true
                R.id.nav_post_job -> {
                    // Navigate to post job
                    true
                }
                R.id.nav_messages -> {
                    // Navigate to messages
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to profile
                    true
                }
                else -> false
            }
        }
    }
}


