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
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val accountType = document.getString("accountType")
                    Log.d("LoginActivity", "Account type: $accountType")

                    val intent = when (accountType) {
                        "employer" -> Intent(this, EmployerDashboardActivity::class.java)
                        "job_seeker" -> Intent(this, MainActivity::class.java)
                        else -> {
                            Log.w("LoginActivity", "Unknown account type: $accountType, defaulting to MainActivity")
                            Intent(this, MainActivity::class.java)
                        }
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
                    // Default to MainActivity if no profile found
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Error fetching user data: ${e.message}", e)
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
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
