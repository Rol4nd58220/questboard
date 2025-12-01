package com.example.questboard

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class JobSeekerRegister : AppCompatActivity() {

    // ========== TESTING FLAG ==========
    // Set to true when you want to enable ID validation
    // Set to false to skip ID validation for testing
    private val ENABLE_ID_VALIDATION = false
    // ==================================

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Form fields
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress1: EditText
    private lateinit var etAddress2: EditText
    private lateinit var etBirthday: EditText
    private lateinit var spinnerValidId: Spinner
    private lateinit var imgFrontPreview: ImageView
    private lateinit var imgBackPreview: ImageView
    private lateinit var btnUploadFront: Button
    private lateinit var btnUploadBack: Button
    private lateinit var btnSignUp: Button
    private lateinit var tvFooter: TextView

    private var frontIdUri: Uri? = null
    private var backIdUri: Uri? = null

    companion object {
        private const val PICK_FRONT_IMAGE = 1
        private const val PICK_BACK_IMAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobseeker_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupValidIdSpinner()
        setupClickListeners()
    }

    private fun initializeViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etLastName = findViewById(R.id.etLastName)
        etPhone = findViewById(R.id.etPhone)
        etAddress1 = findViewById(R.id.etAddress1)
        etAddress2 = findViewById(R.id.etAddress2)
        etBirthday = findViewById(R.id.etBirthday)
        spinnerValidId = findViewById(R.id.spinnerValidId)
        imgFrontPreview = findViewById(R.id.imgFrontPreview)
        imgBackPreview = findViewById(R.id.imgBackPreview)
        btnUploadFront = findViewById(R.id.btnUploadFront)
        btnUploadBack = findViewById(R.id.btnUploadBack)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvFooter = findViewById(R.id.tvFooter)
    }

    private fun setupValidIdSpinner() {
        val idTypes = arrayOf("Select ID Type", "National ID", "Driver's License", "Passport", "Voter's ID", "SSS ID", "PhilHealth ID")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, idTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerValidId.adapter = adapter

        // Note: ID validation is currently disabled (ENABLE_ID_VALIDATION = false)
        // You can skip ID selection and uploads during testing
    }

    private fun setupClickListeners() {
        etBirthday.setOnClickListener {
            showDatePicker()
        }

        btnUploadFront.setOnClickListener {
            openImagePicker(PICK_FRONT_IMAGE)
        }

        btnUploadBack.setOnClickListener {
            openImagePicker(PICK_BACK_IMAGE)
        }

        btnSignUp.setOnClickListener {
            validateAndRegister()
        }

        tvFooter.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear)
                etBirthday.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun openImagePicker(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            when (requestCode) {
                PICK_FRONT_IMAGE -> {
                    frontIdUri = imageUri
                    imgFrontPreview.setImageURI(imageUri)
                }
                PICK_BACK_IMAGE -> {
                    backIdUri = imageUri
                    imgBackPreview.setImageURI(imageUri)
                }
            }
        }
    }

    private fun validateAndRegister() {
        val firstName = etFirstName.text.toString().trim()
        val middleName = etMiddleName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address1 = etAddress1.text.toString().trim()
        val address2 = etAddress2.text.toString().trim()
        val birthday = etBirthday.text.toString().trim()
        val idType = spinnerValidId.selectedItem.toString()

        // Validation
        if (firstName.isEmpty()) {
            etFirstName.error = "Required"
            etFirstName.requestFocus()
            return
        }

        if (middleName.isEmpty()) {
            etMiddleName.error = "Required"
            etMiddleName.requestFocus()
            return
        }

        if (lastName.isEmpty()) {
            etLastName.error = "Required"
            etLastName.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            etPhone.error = "Required"
            etPhone.requestFocus()
            return
        }

        if (address1.isEmpty()) {
            etAddress1.error = "Required"
            etAddress1.requestFocus()
            return
        }

        if (address2.isEmpty()) {
            etAddress2.error = "Required"
            etAddress2.requestFocus()
            return
        }

        if (birthday.isEmpty()) {
            etBirthday.error = "Required"
            etBirthday.requestFocus()
            return
        }

        // Only validate ID if the flag is enabled
        if (ENABLE_ID_VALIDATION) {
            if (idType == "Select ID Type") {
                Toast.makeText(this, "Please select a valid ID type", Toast.LENGTH_SHORT).show()
                return
            }

            if (frontIdUri == null || backIdUri == null) {
                Toast.makeText(this, "Please upload both front and back ID", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Create email and password first, then save profile data
        showEmailPasswordDialog(firstName, middleName, lastName, phone, address1, address2, birthday, idType)
    }

    private fun showEmailPasswordDialog(
        firstName: String,
        middleName: String,
        lastName: String,
        phone: String,
        address1: String,
        address2: String,
        birthday: String,
        idType: String
    ) {
        // For now, navigate to a separate email/password screen
        val intent = Intent(this, EmailPasswordSetupActivity::class.java)
        intent.putExtra("ACCOUNT_TYPE", "job_seeker")
        intent.putExtra("FIRST_NAME", firstName)
        intent.putExtra("MIDDLE_NAME", middleName)
        intent.putExtra("LAST_NAME", lastName)
        intent.putExtra("PHONE", phone)
        intent.putExtra("ADDRESS1", address1)
        intent.putExtra("ADDRESS2", address2)
        intent.putExtra("BIRTHDAY", birthday)
        intent.putExtra("ID_TYPE", idType)
        startActivity(intent)
        finish()
    }
}

