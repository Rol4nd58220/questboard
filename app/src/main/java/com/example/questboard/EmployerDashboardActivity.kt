package com.example.questboard

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * EmployerDashboardActivity - Employer Dashboard
 * Main dashboard for employers with bottom navigation
 */
class EmployerDashboardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main_employer)

            bottomNavigation = findViewById(R.id.bottom_navigation_employer)

            // Set up bottom navigation
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_my_jobs -> {
                        loadFragment(EmployerMyJobsFragment())
                        true
                    }
                    R.id.nav_applicants -> {
                        loadFragment(EmployerApplicantsFragment())
                        true
                    }
                    R.id.nav_post_job -> {
                        loadFragment(EmployerPostJobFragment())
                        true
                    }
                    R.id.nav_messages -> {
                        loadFragment(EmployerMessagesFragment())
                        true
                    }
                    R.id.nav_profile -> {
                        loadFragment(EmployerProfileFragment())
                        true
                    }
                    else -> false
                }
            }

            // Load default fragment (My Jobs)
            if (savedInstanceState == null) {
                loadFragment(EmployerMyJobsFragment())
                bottomNavigation.selectedItemId = R.id.nav_my_jobs
            }
        } catch (e: Exception) {
            android.util.Log.e("EmployerDashboard", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading dashboard: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container_employer, fragment)
            .commitNow()
    }
}

