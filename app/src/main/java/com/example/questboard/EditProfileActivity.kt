package com.example.questboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Edit Profile Activity
 * Allows job seekers to edit their profile information
 */
class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var imgProfilePhoto: ImageView
    private lateinit var edtFirstName: EditText
    private lateinit var edtMiddleName: EditText
    private lateinit var edtLastName: EditText
    private lateinit var edtAboutMe: EditText
    private lateinit var edtLocation: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnChangePhoto: ImageView

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imgProfilePhoto.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        loadCurrentProfile()
        setupListeners()
    }

    private fun initializeViews() {
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto)
        edtFirstName = findViewById(R.id.edtFirstName)
        edtMiddleName = findViewById(R.id.edtMiddleName)
        edtLastName = findViewById(R.id.edtLastName)
        edtAboutMe = findViewById(R.id.edtAboutMe)
        edtLocation = findViewById(R.id.edtLocation)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
    }

    private fun setupListeners() {
        btnChangePhoto.setOnClickListener {
            openImagePicker()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    edtFirstName.setText(document.getString("firstName") ?: "")
                    edtMiddleName.setText(document.getString("middleName") ?: "")
                    edtLastName.setText(document.getString("lastName") ?: "")
                    edtAboutMe.setText(document.getString("bio") ?: document.getString("aboutMe") ?: "")
                    edtLocation.setText(document.getString("location") ?: "")

                    // TODO: Load profile photo when Glide is synced
                    val profilePhotoUrl = document.getString("profilePhotoUrl")
                    if (profilePhotoUrl != null) {
                        Log.d("EditProfile", "Profile photo URL: $profilePhotoUrl")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfile", "Error loading profile: ${e.message}", e)
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val firstName = edtFirstName.text.toString().trim()
        val middleName = edtMiddleName.text.toString().trim()
        val lastName = edtLastName.text.toString().trim()
        val aboutMe = edtAboutMe.text.toString().trim()
        val location = edtLocation.text.toString().trim()

        // Validation
        if (firstName.isEmpty()) {
            edtFirstName.error = "First name is required"
            edtFirstName.requestFocus()
            return
        }

        if (lastName.isEmpty()) {
            edtLastName.error = "Last name is required"
            edtLastName.requestFocus()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        // Build full name
        val fullName = buildString {
            append(firstName)
            if (middleName.isNotEmpty()) {
                append(" ")
                append(middleName)
            }
            if (lastName.isNotEmpty()) {
                append(" ")
                append(lastName)
            }
        }.trim()

        // Update profile
        val updates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "middleName" to middleName,
            "lastName" to lastName,
            "fullName" to fullName,
            "bio" to aboutMe,
            "location" to location,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        // Show loading
        btnSave.isEnabled = false
        btnSave.text = getString(R.string.saving)

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // TODO: Upload photo if selected
                if (selectedImageUri != null) {
                    Toast.makeText(this, "Photo upload coming soon", Toast.LENGTH_SHORT).show()
                }

                // Return to profile
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditProfile", "Error saving profile: ${e.message}", e)
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
                btnSave.text = getString(R.string.save_changes)
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
}

