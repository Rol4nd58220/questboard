package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding)

        // Initialize views
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        // Set up click listeners
        btnLogin.setOnClickListener {
            // Navigate to Login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close onboarding so user can't go back
        }

        tvRegister.setOnClickListener {
            // Navigate directly to Choose Account Type for registration
            val intent = Intent(this, Choose_Account_Type::class.java)
            startActivity(intent)
            finish() // Close onboarding so user can't go back
        }
    }
}

