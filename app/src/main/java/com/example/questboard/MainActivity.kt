package com.example.questboard

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MainActivity - Job Seeker Dashboard
 * Main dashboard for job seekers with bottom navigation
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var searchFilterSection: LinearLayout
    private var selectedChipId: Int = R.id.chipAll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main_jobseeker)

            bottomNavigation = findViewById(R.id.bottom_navigation)
            searchFilterSection = findViewById(R.id.searchFilterSection)

            // Set up top bar buttons
            setupTopBar()

            // Set up search and filters
            setupSearchAndFilters()

            // Set up bottom navigation
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        showSearchFilter(true)
                        loadFragment(JobSeekerHomeFragment())
                        true
                    }
                    R.id.nav_jobs -> {
                        showSearchFilter(false)
                        loadFragment(JobSeekerJobsFragment())
                        true
                    }
                    R.id.nav_community -> {
                        showSearchFilter(false)
                        loadFragment(JobSeekerCommunityFragment())
                        true
                    }
                    R.id.nav_messages -> {
                        showSearchFilter(false)
                        loadFragment(JobSeekerMessagesFragment())
                        true
                    }
                    R.id.nav_profile -> {
                        showSearchFilter(false)
                        loadFragment(JobSeekerProfileFragment())
                        true
                    }
                    else -> false
                }
            }

            // Load default fragment (Home)
            if (savedInstanceState == null) {
                showSearchFilter(true)
                loadFragment(JobSeekerHomeFragment())
                bottomNavigation.selectedItemId = R.id.nav_home
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading dashboard: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSearchFilter(show: Boolean) {
        searchFilterSection.visibility = if (show) View.VISIBLE else View.GONE
    }

    // ...existing code...

    private fun setupTopBar() {
        findViewById<ImageView>(R.id.btnMenu)?.setOnClickListener {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open navigation drawer or menu
        }

        findViewById<ImageView>(R.id.btnNotifications)?.setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open notifications screen
        }
    }

    private fun setupSearchAndFilters() {
        val searchBar = findViewById<EditText>(R.id.etSearch)
        val chipAll = findViewById<TextView>(R.id.chipAll)
        val chipNearby = findViewById<TextView>(R.id.chipNearby)
        val chipHourly = findViewById<TextView>(R.id.chipHourly)
        val chipUrgent = findViewById<TextView>(R.id.chipUrgent)
        val btnFilter = findViewById<LinearLayout>(R.id.btnFilter)

        // Search functionality
        searchBar?.setOnEditorActionListener { _, _, _ ->
            val query = searchBar.text.toString()
            if (query.isNotEmpty()) {
                Toast.makeText(this, "Searching for: $query", Toast.LENGTH_SHORT).show()
                // TODO: Implement search functionality
            }
            false
        }

        // Filter chip click listeners
        chipAll?.setOnClickListener { selectChip(R.id.chipAll) }
        chipNearby?.setOnClickListener { selectChip(R.id.chipNearby) }
        chipHourly?.setOnClickListener { selectChip(R.id.chipHourly) }
        chipUrgent?.setOnClickListener { selectChip(R.id.chipUrgent) }

        // Filter button
        btnFilter?.setOnClickListener {
            Toast.makeText(this, "Filter options clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open filter dialog
        }
    }

    private fun selectChip(chipId: Int) {
        // Deselect previous chip
        findViewById<TextView>(selectedChipId)?.setBackgroundResource(R.drawable.chip_unselected)

        // Select new chip
        findViewById<TextView>(chipId)?.setBackgroundResource(R.drawable.chip_selected)
        selectedChipId = chipId

        // Handle filter action
        val filterName = when (chipId) {
            R.id.chipAll -> "All"
            R.id.chipNearby -> "Nearby"
            R.id.chipHourly -> "Hourly"
            R.id.chipUrgent -> "Urgent"
            else -> "Unknown"
        }

        Toast.makeText(this, "Filter: $filterName", Toast.LENGTH_SHORT).show()
        // TODO: Implement actual filtering
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}