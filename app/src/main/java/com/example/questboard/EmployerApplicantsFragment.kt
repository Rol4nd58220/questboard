package com.example.questboard

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questboard.models.JobCompletion
import com.example.questboard.repository.MessagingRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Employer Applicants Fragment
 * Displays: All applicants across jobs, manage applications, shortlist candidates
 */
class EmployerApplicantsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagingRepository: MessagingRepository
    private var recyclerView: RecyclerView? = null
    private var adapter: ApplicantsAdapter? = null
    private var progressBar: ProgressBar? = null
    private var tvNoApplicants: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employer_applicants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        messagingRepository = MessagingRepository()

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewApplicants)
        progressBar = view.findViewById(R.id.progressBarApplicants)
        tvNoApplicants = view.findViewById(R.id.tvNoApplicants)

        // Setup RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ApplicantsAdapter(
            mutableListOf(),
            onAcceptClick = { application -> acceptApplication(application) },
            onRejectClick = { application -> rejectApplication(application) },
            onViewApplicantClick = { application -> viewApplicantDetails(application) }
        )
        recyclerView?.adapter = adapter

        // Load applicants
        loadApplicants()
    }

    private fun loadApplicants() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar?.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE
        tvNoApplicants?.visibility = View.GONE

        firestore.collection("applications")
            .whereEqualTo("employerId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                progressBar?.visibility = View.GONE

                if (error != null) {
                    android.util.Log.e("EmployerApplicantsFragment", "Error loading applicants: ${error.message}", error)
                    // Don't show toast - just log
                    tvNoApplicants?.visibility = View.VISIBLE
                    tvNoApplicants?.text = "Error loading applicants"
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val applications = snapshot.toObjects(Application::class.java)
                    adapter?.updateApplications(applications)
                    recyclerView?.visibility = View.VISIBLE
                    tvNoApplicants?.visibility = View.GONE
                } else {
                    recyclerView?.visibility = View.GONE
                    tvNoApplicants?.visibility = View.VISIBLE
                    tvNoApplicants?.text = "No applicants yet.\nApplications will appear here when job seekers apply."
                }
            }
    }

    private fun acceptApplication(application: Application) {
        AlertDialog.Builder(requireContext())
            .setTitle("Accept Application")
            .setMessage("Accept ${application.applicantName} for '${application.jobTitle}'?\n\nThe applicant will be notified and this will appear in their Active Jobs.")
            .setPositiveButton("Accept") { _, _ ->
                updateApplicationStatus(application, "Accepted")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rejectApplication(application: Application) {
        AlertDialog.Builder(requireContext())
            .setTitle("Reject Application")
            .setMessage("Reject ${application.applicantName} for '${application.jobTitle}'?\n\nThe applicant will be notified.")
            .setPositiveButton("Reject") { _, _ ->
                updateApplicationStatus(application, "Rejected")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateApplicationStatus(application: Application, newStatus: String) {
        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "respondedAt" to Timestamp.now(),
            "notificationSent" to false // Will trigger notification for job seeker
        )

        firestore.collection("applications").document(application.id)
            .update(updates)
            .addOnSuccessListener {
                val message = if (newStatus == "Accepted") {
                    "Application accepted! ${application.applicantName} has been notified."
                } else {
                    "Application rejected. ${application.applicantName} has been notified."
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating application: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun viewApplicantDetails(application: Application) {
        // For completed applications, directly open the review dialog
        if (application.status == "Completed") {
            reviewJobCompletion(application)
            return
        }

        // For other statuses, show applicant details dialog
        val detailsView = LayoutInflater.from(context).inflate(R.layout.dialog_applicant_details, null)

        // Populate applicant details
        detailsView.findViewById<TextView>(R.id.tvDialogApplicantName).text = application.applicantName
        detailsView.findViewById<TextView>(R.id.tvDialogApplicantEmail).text = "Email: ${application.applicantEmail}"

        if (application.applicantPhone.isNotEmpty()) {
            detailsView.findViewById<TextView>(R.id.tvDialogApplicantPhone).text = "Phone: ${application.applicantPhone}"
            detailsView.findViewById<TextView>(R.id.tvDialogApplicantPhone).visibility = View.VISIBLE
        } else {
            detailsView.findViewById<TextView>(R.id.tvDialogApplicantPhone).visibility = View.GONE
        }

        detailsView.findViewById<TextView>(R.id.tvDialogJobTitle).text = "Applied for: ${application.jobTitle}"

        // Show cover letter/message if available
        if (application.message.isNotEmpty() || application.coverLetter.isNotEmpty()) {
            val message = if (application.coverLetter.isNotEmpty()) application.coverLetter else application.message
            detailsView.findViewById<TextView>(R.id.tvDialogMessage).text = message
            detailsView.findViewById<TextView>(R.id.tvDialogMessage).visibility = View.VISIBLE
        } else {
            detailsView.findViewById<TextView>(R.id.tvDialogMessage).visibility = View.GONE
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(detailsView)
            .setPositiveButton("Close", null)
            .setNeutralButton("Contact") { _, _ ->
                contactApplicant(application)
            }

        // Show Accept button only for Pending applications
        if (application.status == "Pending") {
            dialogBuilder.setNegativeButton("Accept") { _, _ ->
                acceptApplication(application)
            }
        }

        dialogBuilder.show()
    }

    private fun contactApplicant(application: Application) {
        val options = arrayOf(
            "Send Email",
            "Call Applicant",
            "Send Message"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Contact ${application.applicantName}")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Send email
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${application.applicantEmail}")
                            putExtra(Intent.EXTRA_SUBJECT, "Regarding your application: ${application.jobTitle}")
                        }
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        // Call applicant (would need phone number)
                        if (application.applicantPhone.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${application.applicantPhone}")
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                    2 -> {
                        // Send message - create conversation and open chat
                        openChatWithApplicant(application)
                    }
                }
            }
            .show()
    }

    private fun openChatWithApplicant(application: Application) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to send messages", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        val progressDialog = AlertDialog.Builder(requireContext())
            .setMessage("Opening chat...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        // Get employer name from Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val employerDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val employerName = employerDoc.getString("name") ?: "Employer"

                // Create or get conversation
                val result = messagingRepository.getOrCreateConversation(
                    jobSeekerId = application.applicantId,
                    employerId = currentUser.uid,
                    jobId = application.jobId,
                    jobTitle = application.jobTitle,
                    applicationId = application.id,
                    jobSeekerName = application.applicantName,
                    employerName = employerName
                )

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    result.onSuccess { conversationId ->
                        // Open chat activity
                        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                            putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversationId)
                            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, application.applicantName)
                            putExtra(ChatActivity.EXTRA_JOB_TITLE, application.jobTitle)
                        }
                        startActivity(intent)
                    }.onFailure { error ->
                        Toast.makeText(
                            context,
                            "Failed to open chat: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        android.util.Log.e("EmployerApplicants", "Error opening chat", error)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        context,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    android.util.Log.e("EmployerApplicants", "Error getting employer name", e)
                }
            }
        }
    }

    private fun reviewJobCompletion(application: Application) {
        // Load the job completion data
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val completionSnapshot = firestore.collection("jobCompletions")
                    .whereEqualTo("applicationId", application.id)
                    .limit(1)
                    .get()
                    .await()

                if (completionSnapshot.isEmpty) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No completion form found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val completion = completionSnapshot.documents[0].toObject(JobCompletion::class.java)
                if (completion == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading completion data", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    showReviewDialog(application, completion)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("EmployerApplicants", "Error loading completion", e)
                }
            }
        }
    }

    private fun showReviewDialog(application: Application, completion: JobCompletion) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_employer_review_completion, null)

        // Populate job details
        dialogView.findViewById<TextView>(R.id.tvReviewJobTitle).text = completion.jobTitle
        dialogView.findViewById<TextView>(R.id.tvReviewJobSeekerName).text = "Job Seeker: ${completion.jobSeekerName}"

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
        dialogView.findViewById<TextView>(R.id.tvReviewSubmittedDate).text =
            "Submitted: ${dateFormat.format(completion.submittedAt.toDate())}"

        // Show completion status
        val tvCompletionStatus = dialogView.findViewById<TextView>(R.id.tvCompletionStatus)
        if (completion.isCompleted) {
            tvCompletionStatus.text = "✓ Job completed successfully"
            tvCompletionStatus.setBackgroundColor(android.graphics.Color.parseColor("#1B5E20"))
        } else {
            tvCompletionStatus.text = "⚠ Issues reported"
            tvCompletionStatus.setBackgroundColor(android.graphics.Color.parseColor("#E65100"))
        }

        // Show issues if any
        if (completion.hasIssues && completion.concerns.isNotEmpty()) {
            dialogView.findViewById<TextView>(R.id.tvIssuesLabel).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tvIssuesText).apply {
                visibility = View.VISIBLE
                text = completion.concerns
            }
        }

        // Show additional notes if any
        if (completion.additionalNotes.isNotEmpty()) {
            dialogView.findViewById<TextView>(R.id.tvNotesLabel).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tvNotesText).apply {
                visibility = View.VISIBLE
                text = completion.additionalNotes
            }
        }

        // Get UI elements for employer review
        val cbConfirmCompletion = dialogView.findViewById<CheckBox>(R.id.cbConfirmCompletion)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etEmployerFeedback = dialogView.findViewById<TextInputEditText>(R.id.etEmployerFeedback)
        val rgPaymentMethod = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val rbCash = dialogView.findViewById<RadioButton>(R.id.rbCash)
        val rbGCash = dialogView.findViewById<RadioButton>(R.id.rbGCash)
        val cbPaymentConfirmed = dialogView.findViewById<CheckBox>(R.id.cbPaymentConfirmed)
        val btnCancelReview = dialogView.findViewById<Button>(R.id.btnCancelReview)
        val btnSubmitReview = dialogView.findViewById<Button>(R.id.btnSubmitReview)

        // Handle payment method selection
        rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCash -> {
                    cbPaymentConfirmed.isEnabled = true
                    cbPaymentConfirmed.isChecked = false
                }
                R.id.rbGCash -> {
                    cbPaymentConfirmed.isEnabled = false
                    cbPaymentConfirmed.isChecked = true // Auto-check for GCash
                }
            }
        }

        // Create dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelReview.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmitReview.setOnClickListener {
            submitReview(
                application,
                completion,
                cbConfirmCompletion.isChecked,
                ratingBar.rating,
                etEmployerFeedback.text.toString(),
                if (rbCash.isChecked) "Cash" else if (rbGCash.isChecked) "GCash" else "",
                cbPaymentConfirmed.isChecked,
                dialog
            )
        }

        dialog.show()
    }

    private fun submitReview(
        application: Application,
        completion: JobCompletion,
        isConfirmed: Boolean,
        rating: Float,
        feedback: String,
        paymentMethod: String,
        paymentConfirmed: Boolean,
        dialog: AlertDialog
    ) {
        // Validation
        if (!isConfirmed) {
            Toast.makeText(context, "Please confirm job completion", Toast.LENGTH_SHORT).show()
            return
        }

        if (rating == 0f) {
            Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
            return
        }

        if (paymentMethod.isEmpty()) {
            Toast.makeText(context, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        if (!paymentConfirmed) {
            Toast.makeText(context, "Please confirm payment", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress
        val progressDialog = AlertDialog.Builder(requireContext())
            .setMessage("Submitting review...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        // Update completion with employer review
        val updates = hashMapOf<String, Any>(
            "reviewedByEmployer" to true,
            "employerFeedback" to feedback,
            "employerRating" to rating,
            "paymentMethod" to paymentMethod,
            "paymentReleased" to paymentConfirmed
        )

        firestore.collection("jobCompletions")
            .whereEqualTo("applicationId", application.id)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val docId = snapshot.documents[0].id

                    firestore.collection("jobCompletions").document(docId)
                        .update(updates)
                        .addOnSuccessListener {
                            // Update application status to "Reviewed"
                            firestore.collection("applications").document(application.id)
                                .update("status", "Reviewed")
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    dialog.dismiss()

                                    Toast.makeText(
                                        context,
                                        "Review submitted successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    showReviewConfirmation(rating, feedback)
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        context,
                                        "Error updating application: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                context,
                                "Error submitting review: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReviewConfirmation(rating: Float, feedback: String) {
        val stars = "★".repeat(rating.toInt()) + "☆".repeat(5 - rating.toInt())

        AlertDialog.Builder(requireContext())
            .setTitle("Review Submitted ✓")
            .setMessage(
                "Your review has been submitted!\n\n" +
                "Rating: $stars (${rating}/5)\n\n" +
                "The job seeker has been notified and payment has been confirmed."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadApplicants()
    }
}

