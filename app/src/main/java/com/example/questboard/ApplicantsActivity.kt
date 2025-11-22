package com.example.questboard

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip

class ApplicantsActivity : AppCompatActivity() {

    private lateinit var tvApplicantsForJob: TextView
    private lateinit var etSearchApplicants: EditText
    private lateinit var chipMostRecent: Chip
    private lateinit var chipHighestRated: Chip
    private lateinit var rvApplicants: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    private var jobTitle: String = "Event Setup Crew"
    private var applicantCount: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applicants)

        // Get job title and count from intent
        jobTitle = intent.getStringExtra("JOB_TITLE") ?: "Event Setup Crew"
        applicantCount = intent.getIntExtra("APPLICANT_COUNT", 3)

        // Initialize views
        initViews()

        // Setup UI
        setupJobTitle()
        setupFilterChips()
        setupRecyclerView()
        setupBottomNavigation()

        // Setup back button
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        tvApplicantsForJob = findViewById(R.id.tvApplicantsForJob)
        etSearchApplicants = findViewById(R.id.etSearchApplicants)
        chipMostRecent = findViewById(R.id.chipMostRecent)
        chipHighestRated = findViewById(R.id.chipHighestRated)
        rvApplicants = findViewById(R.id.rvApplicants)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupJobTitle() {
        tvApplicantsForJob.text = getString(R.string.applicants_for_job, jobTitle, applicantCount)
    }

    private fun setupFilterChips() {
        chipMostRecent.setOnClickListener {
            // Sort by most recent
            loadApplicants(sortBy = "recent")
        }

        chipHighestRated.setOnClickListener {
            // Sort by highest rated
            loadApplicants(sortBy = "rating")
        }
    }

    private fun setupRecyclerView() {
        rvApplicants.layoutManager = LinearLayoutManager(this)
        loadApplicants()
    }

    private fun loadApplicants(sortBy: String = "recent") {
        // Sample data
        val applicants = listOf(
            Applicant(
                id = "1",
                name = "Anya Petrova",
                jobTitle = "Event Setup Crew",
                appliedTime = "Applied on 2 days ago",
                photoUrl = null,
                skills = listOf("Event Planning", "Teamwork", "Problem Solving"),
                distance = "1.2 miles away",
                rating = 4.8f,
                reviewCount = 12,
                ratingBreakdown = mapOf(5 to 75, 4 to 13, 3 to 5, 2 to 3, 1 to 2),
                bio = "I have 3 years of experience in event setup and coordination. I am reliable, detail-oriented, and work well under pressure.",
                isShortlisted = false
            ),
            Applicant(
                id = "2",
                name = "Ethan Blackwood",
                jobTitle = "Event Setup Crew",
                appliedTime = "Applied on 3 days ago",
                photoUrl = null,
                skills = listOf("Event Planning", "Teamwork", "Problem Solving"),
                distance = "2.5 miles away",
                rating = 4.5f,
                reviewCount = 8,
                ratingBreakdown = mapOf(5 to 62, 4 to 20, 3 to 10, 2 to 5, 1 to 3),
                bio = "I have 2 years of experience in event setup and coordination. I am reliable, detail-oriented, and work well under pressure.",
                isShortlisted = false
            )
        )

        val adapter = ApplicantsAdapter(applicants,
            onViewClick = { applicant ->
                // Navigate to applicant detail
            },
            onRejectClick = { applicant ->
                // Show reject confirmation
            },
            onShortlistClick = { applicant ->
                // Toggle shortlist
            }
        )

        rvApplicants.adapter = adapter
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

data class Applicant(
    val id: String,
    val name: String,
    val jobTitle: String,
    val appliedTime: String,
    val photoUrl: String?,
    val skills: List<String>,
    val distance: String,
    val rating: Float,
    val reviewCount: Int,
    val ratingBreakdown: Map<Int, Int>, // star rating to percentage
    val bio: String,
    var isShortlisted: Boolean
)

