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

        android.util.Log.d("MainActivity", "onCreate started")

        try {
            setContentView(R.layout.activity_main_jobseeker)
            android.util.Log.d("MainActivity", "Layout set successfully")

            bottomNavigation = findViewById(R.id.bottom_navigation)
            searchFilterSection = findViewById(R.id.searchFilterSection)

            android.util.Log.d("MainActivity", "Views initialized")

            // Set up top bar buttons
            setupTopBar()

            // Set up search and filters
            setupSearchAndFilters()

            // Set up bottom navigation
            bottomNavigation.setOnItemSelectedListener { item ->
                android.util.Log.d("MainActivity", "Navigation item selected: ${item.itemId}")

                try {
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
                        else -> {
                            android.util.Log.w("MainActivity", "Unknown navigation item: ${item.itemId}")
                            false
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error switching fragment: ${e.message}", e)
                    Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show()
                    false
                }
            }

            // Load default fragment (Home)
            if (savedInstanceState == null) {
                android.util.Log.d("MainActivity", "Loading default fragment (Home)")
                showSearchFilter(true)
                loadFragment(JobSeekerHomeFragment())
                bottomNavigation.selectedItemId = R.id.nav_home
            }

            android.util.Log.d("MainActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Critical error in onCreate: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(this, "Critical error loading dashboard. Please restart the app.", Toast.LENGTH_LONG).show()

            // Try to show a basic error message
            try {
                val errorText = TextView(this).apply {
                    text = "Error: ${e.message}\n\nPlease restart the app or contact support."
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                }
                setContentView(errorText)
            } catch (e2: Exception) {
                android.util.Log.e("MainActivity", "Could not show error view: ${e2.message}", e2)
            }
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
        try {
            android.util.Log.d("MainActivity", "Loading fragment: ${fragment.javaClass.simpleName}")

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss() // Use commitAllowingStateLoss to prevent crashes

            android.util.Log.d("MainActivity", "Fragment loaded successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading fragment: ${e.message}", e)
            Toast.makeText(this, "Error loading screen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}