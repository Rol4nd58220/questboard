package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmailPasswordSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnContinue: Button
    private lateinit var tvLogin: TextView
    private var isJobSeeker: Boolean = true // true = job seeker, false = employer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_password_setup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        isJobSeeker = intent.getBooleanExtra("IS_JOB_SEEKER", true)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnContinue = findViewById(R.id.btnContinue)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupClickListeners() {
        btnContinue.setOnClickListener {
            createAccountAndSaveProfile()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun createAccountAndSaveProfile() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }

        // Create Firebase account
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserProfile()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        val firstName = intent.getStringExtra("FIRST_NAME") ?: ""
        val middleName = intent.getStringExtra("MIDDLE_NAME") ?: ""
        val lastName = intent.getStringExtra("LAST_NAME") ?: ""
        val fullName = "$firstName $middleName $lastName".trim().replace("\\s+".toRegex(), " ")

        val userData = hashMapOf(
            "firstName" to firstName,
            "middleName" to middleName,
            "lastName" to lastName,
            "fullName" to fullName,  // Added for easier display
            "phone" to intent.getStringExtra("PHONE"),
            "address1" to intent.getStringExtra("ADDRESS1"),
            "address2" to intent.getStringExtra("ADDRESS2"),
            "birthday" to intent.getStringExtra("BIRTHDAY"),
            "idType" to intent.getStringExtra("ID_TYPE"),
            "email" to (auth.currentUser?.email ?: ""),
            "isJobSeeker" to isJobSeeker, // true = job seeker, false = employer
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        // Add business permit type for employers
        if (!isJobSeeker) {
            userData["businessPermitType"] = intent.getStringExtra("BUSINESS_PERMIT_TYPE")
        }

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMainActivity() {
        val intent = if (isJobSeeker) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, EmployerDashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}

