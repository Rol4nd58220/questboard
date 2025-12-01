package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView
    private lateinit var tvForgot: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignup = findViewById(R.id.tvSignup)
        tvForgot = findViewById(R.id.tvForgot)

        btnLogin.setOnClickListener {
            loginUser()
        }

        tvSignup.setOnClickListener {
            val intent = Intent(this, Choose_Account_Type::class.java)
            startActivity(intent)
        }


        tvForgot.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Check account type and navigate to correct dashboard
                    checkAccountTypeAndNavigate()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAccountTypeAndNavigate() {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("LoginActivity", "No user ID found after login")
            Toast.makeText(this, "Login error. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("LoginActivity", "Fetching user profile for: $userId")

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                try {
                    if (document.exists()) {
                        val isJobSeeker = document.getBoolean("isJobSeeker") ?: true
                        Log.d("LoginActivity", "User found. isJobSeeker: $isJobSeeker")

                        val intent = if (isJobSeeker) {
                            Intent(this, MainActivity::class.java)
                        } else {
                            Intent(this, EmployerDashboardActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w("LoginActivity", "User document does not exist. Creating default profile...")

                        // Create a basic user profile if it doesn't exist
                        val defaultProfile = hashMapOf(
                            "email" to (auth.currentUser?.email ?: ""),
                            "isJobSeeker" to true,
                            "fullName" to "User",
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("users").document(userId)
                            .set(defaultProfile)
                            .addOnSuccessListener {
                                Log.d("LoginActivity", "Default profile created")
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("LoginActivity", "Failed to create profile: ${e.message}", e)
                                // Still navigate to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error processing user data: ${e.message}", e)
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Error fetching user data: ${e.message}", e)
                Toast.makeText(this, "Could not load profile. Logging in anyway...", Toast.LENGTH_SHORT).show()
                // Default to MainActivity on error
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }


    private fun handleForgotPassword() {
        // Create custom dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set transparent background for custom styling
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Get dialog views
        val etResetEmail = dialogView.findViewById<EditText>(R.id.etResetEmail)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSendReset = dialogView.findViewById<Button>(R.id.btnSendReset)

        // Pre-fill email if available
        val currentEmail = etEmail.text.toString().trim()
        if (currentEmail.isNotEmpty()) {
            etResetEmail.setText(currentEmail)
        }

        // Cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Send reset button
        btnSendReset.setOnClickListener {
            val email = etResetEmail.text.toString().trim()

            if (email.isEmpty()) {
                etResetEmail.error = "Email is required"
                etResetEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etResetEmail.error = "Enter a valid email"
                etResetEmail.requestFocus()
                return@setOnClickListener
            }

            // Send password reset email
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        dialog.show()
    }
}
