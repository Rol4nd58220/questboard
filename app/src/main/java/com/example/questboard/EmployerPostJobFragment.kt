package com.example.questboard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

/**
 * Employer Post Job Fragment
 * Displays: Form to create new job posting with Firebase integration
 */
class EmployerPostJobFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 100
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var editingJobId: String? = null
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employer_post_job, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val etJobTitle = view.findViewById<EditText>(R.id.etJobTitle)
        val etJobDescription = view.findViewById<EditText>(R.id.etJobDescription)
        val spinnerPaymentType = view.findViewById<Spinner>(R.id.spinnerPaymentType)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val spinnerJobCategory = view.findViewById<Spinner>(R.id.spinnerJobCategory)
        val etDateTime = view.findViewById<EditText>(R.id.etDateTime)
        val etJobLocation = view.findViewById<EditText>(R.id.etJobLocation)
        val etRequirements = view.findViewById<EditText>(R.id.etRequirements)
        val btnUploadImage = view.findViewById<Button>(R.id.btnUploadImage)
        val btnPostJob = view.findViewById<Button>(R.id.btnPostJob)

        // Check if editing existing job
        arguments?.getString("jobId")?.let { jobId ->
            editingJobId = jobId
            isEditMode = true
            btnPostJob.text = getString(R.string.update_job)
            loadJobData(jobId)
        }

        // Setup Payment Type Spinner
        val paymentTypes = arrayOf("Select Payment Type", "Hourly", "Daily", "Weekly", "Monthly", "Fixed Price")
        val paymentAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paymentTypes)
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaymentType.adapter = paymentAdapter

        // Setup Job Category Spinner
        val categories = arrayOf(
            "Select Category",
            "Construction",
            "Delivery",
            "Cleaning",
            "Gardening",
            "Household Help",
            "Event Staff",
            "Tutoring",
            "Tech Support",
            "Other"
        )
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJobCategory.adapter = categoryAdapter

        // Date and Time Picker
        etDateTime.setOnClickListener {
            showDateTimePicker(etDateTime)
        }

        // Upload Image
        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Post/Update Job Button
        btnPostJob.setOnClickListener {
            val title = etJobTitle.text.toString().trim()
            val description = etJobDescription.text.toString().trim()
            val paymentType = spinnerPaymentType.selectedItem.toString()
            val amount = etAmount.text.toString().trim()
            val category = spinnerJobCategory.selectedItem.toString()
            val dateTime = etDateTime.text.toString().trim()
            val location = etJobLocation.text.toString().trim()
            val requirements = etRequirements.text.toString().trim()

            if (validateForm(title, description, paymentType, amount, category, dateTime, location)) {
                btnPostJob.isEnabled = false
                btnPostJob.text = if (isEditMode) "Updating..." else "Posting..."

                if (isEditMode) {
                    updateJob(title, description, paymentType, amount, category, dateTime, location, requirements)
                } else {
                    postJob(title, description, paymentType, amount, category, dateTime, location, requirements)
                }
            }
        }
    }

    private fun loadJobData(jobId: String) {
        firestore.collection("jobs").document(jobId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val job = document.toObject(Job::class.java)
                    job?.let {
                        view?.findViewById<EditText>(R.id.etJobTitle)?.setText(it.title)
                        view?.findViewById<EditText>(R.id.etJobDescription)?.setText(it.description)
                        view?.findViewById<EditText>(R.id.etAmount)?.setText(it.amount.toString())
                        view?.findViewById<EditText>(R.id.etDateTime)?.setText(it.dateTime)
                        view?.findViewById<EditText>(R.id.etJobLocation)?.setText(it.location)
                        view?.findViewById<EditText>(R.id.etRequirements)?.setText(it.requirements)

                        // Set spinner selections
                        @Suppress("UNCHECKED_CAST")
                        val paymentAdapter = view?.findViewById<Spinner>(R.id.spinnerPaymentType)?.adapter as? ArrayAdapter<String>
                        paymentAdapter?.let { adapter ->
                            val position = adapter.getPosition(it.paymentType)
                            if (position >= 0) view?.findViewById<Spinner>(R.id.spinnerPaymentType)?.setSelection(position)
                        }

                        @Suppress("UNCHECKED_CAST")
                        val categoryAdapter = view?.findViewById<Spinner>(R.id.spinnerJobCategory)?.adapter as? ArrayAdapter<String>
                        categoryAdapter?.let { adapter ->
                            val position = adapter.getPosition(it.category)
                            if (position >= 0) view?.findViewById<Spinner>(R.id.spinnerJobCategory)?.setSelection(position)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load job: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun postJob(
        title: String,
        description: String,
        paymentType: String,
        amount: String,
        category: String,
        dateTime: String,
        location: String,
        requirements: String
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            resetButton()
            return
        }

        // Get employer details
        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val employerName = userDoc.getString("fullName") ?: "Unknown Employer"

                // Create job object
                val job = Job(
                    employerId = currentUser.uid,
                    employerName = employerName,
                    employerEmail = currentUser.email ?: "",
                    title = title,
                    description = description,
                    category = category,
                    paymentType = paymentType,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    location = location,
                    dateTime = dateTime,
                    requirements = requirements,
                    status = "Open",
                    applicantsCount = 0,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    isActive = true
                )

                saveJobToFirestore(job)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get user data: ${e.message}", Toast.LENGTH_SHORT).show()
                resetButton()
            }
    }

    private fun updateJob(
        title: String,
        description: String,
        paymentType: String,
        amount: String,
        category: String,
        dateTime: String,
        location: String,
        requirements: String
    ) {
        editingJobId?.let { jobId ->
            val updates = hashMapOf<String, Any>(
                "title" to title,
                "description" to description,
                "category" to category,
                "paymentType" to paymentType,
                "amount" to (amount.toDoubleOrNull() ?: 0.0),
                "location" to location,
                "dateTime" to dateTime,
                "requirements" to requirements,
                "updatedAt" to Timestamp.now()
            )

            updateJobInFirestore(jobId, updates)
        }
    }

    private fun saveJobToFirestore(job: Job) {
        firestore.collection("jobs")
            .add(job)
            .addOnSuccessListener {
                Toast.makeText(context, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
                resetButton()

                // Navigate back to My Jobs tab
                (activity as? EmployerDashboardActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottom_navigation_employer
                )?.selectedItemId = R.id.nav_my_jobs
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to post job: ${e.message}", Toast.LENGTH_LONG).show()
                resetButton()
            }
    }

    private fun updateJobInFirestore(jobId: String, updates: HashMap<String, Any>) {
        firestore.collection("jobs").document(jobId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Job Updated Successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
                resetButton()
                isEditMode = false
                editingJobId = null

                // Navigate back to My Jobs tab
                (activity as? EmployerDashboardActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottom_navigation_employer
                )?.selectedItemId = R.id.nav_my_jobs
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update job: ${e.message}", Toast.LENGTH_LONG).show()
                resetButton()
            }
    }

    private fun resetButton() {
        view?.findViewById<Button>(R.id.btnPostJob)?.apply {
            isEnabled = true
            text = if (isEditMode) "Update Job" else "Post Job"
        }
    }

    private fun showDateTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        val formattedDateTime = String.format(
                            Locale.US,
                            "%02d/%02d/%04d %02d:%02d",
                            month + 1, day, year, hour, minute
                        )
                        editText.setText(formattedDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun validateForm(
        title: String,
        description: String,
        paymentType: String,
        amount: String,
        category: String,
        dateTime: String,
        location: String
    ): Boolean {
        if (title.isEmpty()) {
            Toast.makeText(context, "Please enter job title", Toast.LENGTH_SHORT).show()
            return false
        }
        if (description.isEmpty()) {
            Toast.makeText(context, "Please enter job description", Toast.LENGTH_SHORT).show()
            return false
        }
        if (paymentType == "Select Payment Type") {
            Toast.makeText(context, "Please select payment type", Toast.LENGTH_SHORT).show()
            return false
        }
        if (amount.isEmpty()) {
            Toast.makeText(context, "Please enter amount", Toast.LENGTH_SHORT).show()
            return false
        }
        if (category == "Select Category") {
            Toast.makeText(context, "Please select job category", Toast.LENGTH_SHORT).show()
            return false
        }
        if (dateTime.isEmpty()) {
            Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
            return false
        }
        if (location.isEmpty()) {
            Toast.makeText(context, "Please enter job location", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun clearForm() {
        view?.findViewById<EditText>(R.id.etJobTitle)?.setText("")
        view?.findViewById<EditText>(R.id.etJobDescription)?.setText("")
        view?.findViewById<Spinner>(R.id.spinnerPaymentType)?.setSelection(0)
        view?.findViewById<EditText>(R.id.etAmount)?.setText("")
        view?.findViewById<Spinner>(R.id.spinnerJobCategory)?.setSelection(0)
        view?.findViewById<EditText>(R.id.etDateTime)?.setText("")
        view?.findViewById<EditText>(R.id.etJobLocation)?.setText("")
        view?.findViewById<EditText>(R.id.etRequirements)?.setText("")
        view?.findViewById<ImageView>(R.id.imgJobPreview)?.setImageDrawable(null)
        selectedImageUri = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && data != null) {
            selectedImageUri = data.data
            view?.findViewById<ImageView>(R.id.imgJobPreview)?.setImageURI(selectedImageUri)
        }
    }
}

