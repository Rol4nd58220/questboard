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
import java.util.*

/**
 * Employer Post Job Fragment
 * Displays: Form to create new job posting
 */
class EmployerPostJobFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employer_post_job, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etJobTitle = view.findViewById<EditText>(R.id.etJobTitle)
        val etJobDescription = view.findViewById<EditText>(R.id.etJobDescription)
        val spinnerPaymentType = view.findViewById<Spinner>(R.id.spinnerPaymentType)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val spinnerJobCategory = view.findViewById<Spinner>(R.id.spinnerJobCategory)
        val etDateTime = view.findViewById<EditText>(R.id.etDateTime)
        val etJobLocation = view.findViewById<EditText>(R.id.etJobLocation)
        val etRequirements = view.findViewById<EditText>(R.id.etRequirements)
        val imgJobPreview = view.findViewById<ImageView>(R.id.imgJobPreview)
        val btnUploadImage = view.findViewById<Button>(R.id.btnUploadImage)
        val btnPostJob = view.findViewById<Button>(R.id.btnPostJob)

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
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Post Job Button
        btnPostJob.setOnClickListener {
            val title = etJobTitle.text.toString().trim()
            val description = etJobDescription.text.toString().trim()
            val paymentType = spinnerPaymentType.selectedItem.toString()
            val amount = etAmount.text.toString().trim()
            val category = spinnerJobCategory.selectedItem.toString()
            val dateTime = etDateTime.text.toString().trim()
            val location = etJobLocation.text.toString().trim()

            if (validateForm(title, description, paymentType, amount, category, dateTime, location)) {
                Toast.makeText(context, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                // TODO: Save job to Firebase
                clearForm()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && data != null) {
            selectedImageUri = data.data
            view?.findViewById<ImageView>(R.id.imgJobPreview)?.setImageURI(selectedImageUri)
        }
    }
}

