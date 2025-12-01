package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Choose_Account_Type : AppCompatActivity() {

    private lateinit var btnContinueJobSeeker: Button
    private lateinit var btnContinueEmployer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_account_type)

        btnContinueJobSeeker = findViewById(R.id.btnContinueJobSeeker)
        btnContinueEmployer = findViewById(R.id.btnContinueEmployer)

        btnContinueJobSeeker.setOnClickListener {
            navigateToJobSeekerRegistration()
        }

        btnContinueEmployer.setOnClickListener {
            navigateToEmployerRegistration()
        }
    }

    private fun navigateToJobSeekerRegistration() {
        val intent = Intent(this, JobSeekerRegister::class.java)
        startActivity(intent)
    }

    private fun navigateToEmployerRegistration() {
        val intent = Intent(this, EmployerRegister::class.java)
        startActivity(intent)
    }
}
