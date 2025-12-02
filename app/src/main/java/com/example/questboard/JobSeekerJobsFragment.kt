package com.example.questboard

// import android.app.Activity // Disabled - not needed without image upload
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
// import android.provider.MediaStore // Disabled for now - image upload
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
// import androidx.activity.result.contract.ActivityResultContracts // Disabled for now - image upload
// import androidx.cardview.widget.CardView // Disabled - not needed without image upload
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questboard.models.JobCompletion
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// import com.google.firebase.storage.FirebaseStorage // Disabled for now - image upload
// import java.util.UUID // Disabled for now - image upload

/**
 * Job Seeker Jobs Fragment
 * Displays: Applied jobs and Active jobs with status tracking
 */
class JobSeekerJobsFragment : Fragment() {

    private var isAppliedTabSelected = true
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    // private lateinit var storage: FirebaseStorage // Disabled for now - image upload

    private var recyclerViewApplied: RecyclerView? = null
    private var recyclerViewActive: RecyclerView? = null
    private var appliedJobsAdapter: AppliedJobsAdapter? = null
    private var activeJobsAdapter: ActiveJobsAdapter? = null
    private var progressBar: ProgressBar? = null
    private var tvNoJobs: TextView? = null

    // For job completion - image upload disabled for now
    // private var selectedImageUri: Uri? = null
    // private var currentCompletionDialog: AlertDialog? = null

    /* IMAGE PICKER DISABLED - TO RE-ENABLE LATER, UNCOMMENT THIS
    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateImagePreview(uri)
            }
        }
    }
    */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobseeker_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // storage = FirebaseStorage.getInstance() // Disabled for now - image upload

        val tabAppliedContainer = view.findViewById<LinearLayout>(R.id.tabAppliedContainer)
        val tabActiveContainer = view.findViewById<LinearLayout>(R.id.tabActiveContainer)
        val tabApplied = view.findViewById<TextView>(R.id.tabApplied)
        val tabActive = view.findViewById<TextView>(R.id.tabActive)
        val underlineApplied = view.findViewById<View>(R.id.underlineApplied)
        val underlineActive = view.findViewById<View>(R.id.underlineActive)

        recyclerViewApplied = view.findViewById(R.id.recyclerViewApplied)
        recyclerViewActive = view.findViewById(R.id.recyclerViewActive)
        progressBar = view.findViewById(R.id.progressBarJobs)
        tvNoJobs = view.findViewById(R.id.tvNoJobs)

        // Setup RecyclerViews
        setupRecyclerViews()

        // Set up tab click listeners
        tabAppliedContainer?.setOnClickListener {
            if (!isAppliedTabSelected) {
                selectAppliedTab(tabApplied, tabActive, underlineApplied, underlineActive)
            }
        }

        tabActiveContainer?.setOnClickListener {
            if (isAppliedTabSelected) {
                selectActiveTab(tabApplied, tabActive, underlineApplied, underlineActive)
            }
        }

        // Load initial data
        loadAppliedJobs()
    }

    private fun setupRecyclerViews() {
        // Setup Applied Jobs RecyclerView with new card adapter
        recyclerViewApplied?.layoutManager = LinearLayoutManager(context)
        appliedJobsAdapter = AppliedJobsAdapter(
            mutableListOf(),
            onViewJobClick = { application ->
                viewJobDetails(application)
            },
            onCancelApplicationClick = { application ->
                showCancelConfirmation(application)
            }
        )
        recyclerViewApplied?.adapter = appliedJobsAdapter

        // Setup Active Jobs RecyclerView with new card adapter
        recyclerViewActive?.layoutManager = LinearLayoutManager(context)
        activeJobsAdapter = ActiveJobsAdapter(
            mutableListOf(),
            onViewJobClick = { application ->
                viewActiveJobDetails(application)
            },
            onContactEmployerClick = { application ->
                messageEmployer(application)
            },
            onConfirmCompletionClick = { application ->
                showJobCompletionForm(application)
            }
        )
        recyclerViewActive?.adapter = activeJobsAdapter
    }

    private fun selectAppliedTab(tabApplied: TextView?, tabActive: TextView?,
                                  underlineApplied: View?, underlineActive: View?) {
        isAppliedTabSelected = true

        tabApplied?.setTextColor(requireContext().getColor(android.R.color.white))
        tabActive?.setTextColor(requireContext().getColor(android.R.color.darker_gray))
        underlineApplied?.visibility = View.VISIBLE
        underlineActive?.visibility = View.INVISIBLE

        recyclerViewApplied?.visibility = View.VISIBLE
        recyclerViewActive?.visibility = View.GONE

        loadAppliedJobs()
    }

    private fun selectActiveTab(tabApplied: TextView?, tabActive: TextView?,
                                 underlineApplied: View?, underlineActive: View?) {
        isAppliedTabSelected = false

        tabApplied?.setTextColor(requireContext().getColor(android.R.color.darker_gray))
        tabActive?.setTextColor(requireContext().getColor(android.R.color.white))
        underlineApplied?.visibility = View.INVISIBLE
        underlineActive?.visibility = View.VISIBLE

        recyclerViewApplied?.visibility = View.GONE
        recyclerViewActive?.visibility = View.VISIBLE

        loadActiveJobs()
    }

    private fun loadAppliedJobs() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar?.visibility = View.VISIBLE
        recyclerViewApplied?.visibility = View.GONE
        tvNoJobs?.visibility = View.GONE

        firestore.collection("applications")
            .whereEqualTo("applicantId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                progressBar?.visibility = View.GONE

                if (error != null) {
                    android.util.Log.e("JobSeekerJobsFragment", "Error loading applications: ${error.message}", error)
                    // Don't show toast repeatedly - just log
                    tvNoJobs?.visibility = View.VISIBLE
                    tvNoJobs?.text = "Error loading applications"
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val applications = snapshot.toObjects(Application::class.java)

                    // Filter out accepted applications (they go to Active tab)
                    val appliedOnly = applications.filter { it.status != "Accepted" }

                    appliedJobsAdapter?.updateApplications(appliedOnly)
                    recyclerViewApplied?.visibility = View.VISIBLE
                    tvNoJobs?.visibility = View.GONE

                    // Check for new notifications
                    checkForNewNotifications(applications)
                } else {
                    recyclerViewApplied?.visibility = View.GONE
                    tvNoJobs?.visibility = View.VISIBLE
                    tvNoJobs?.text = "No applications yet.\nApply to jobs to see them here."
                }
            }
    }

    private fun loadActiveJobs() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar?.visibility = View.VISIBLE
        recyclerViewActive?.visibility = View.GONE
        tvNoJobs?.visibility = View.GONE

        firestore.collection("applications")
            .whereEqualTo("applicantId", currentUser.uid)
            .whereEqualTo("status", "Accepted")
            .addSnapshotListener { snapshot, error ->
                progressBar?.visibility = View.GONE

                if (error != null) {
                    android.util.Log.e("JobSeekerJobsFragment", "Error loading active jobs: ${error.message}", error)
                    // Don't show toast repeatedly - just log
                    tvNoJobs?.visibility = View.VISIBLE
                    tvNoJobs?.text = "Error loading active jobs"
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val applications = snapshot.toObjects(Application::class.java)
                    activeJobsAdapter?.updateApplications(applications)
                    recyclerViewActive?.visibility = View.VISIBLE
                    tvNoJobs?.visibility = View.GONE
                } else {
                    recyclerViewActive?.visibility = View.GONE
                    tvNoJobs?.visibility = View.VISIBLE
                    tvNoJobs?.text = "No active jobs yet.\nAccepted applications will appear here."
                }
            }
    }

    private fun checkForNewNotifications(applications: List<Application>) {
        applications.forEach { application ->
            if ((application.status == "Accepted" || application.status == "Rejected")
                && !application.notificationSent) {
                // Show notification
                showApplicationStatusNotification(application)

                // Mark notification as sent
                firestore.collection("applications").document(application.id)
                    .update("notificationSent", true)
            }
        }
    }

    private fun showApplicationStatusNotification(application: Application) {
        val title = if (application.status == "Accepted") {
            "Application Accepted! ✓"
        } else {
            "Application Update"
        }

        val message = if (application.status == "Accepted") {
            "Congratulations! Your application for '${application.jobTitle}' has been accepted by ${application.employerName}."
        } else {
            "Your application for '${application.jobTitle}' was not selected this time."
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("View Job") { _, _ ->
                viewJobDetails(application)
            }
            .setNegativeButton("OK", null)
            .show()

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun viewJobDetails(application: Application) {
        // Load the job and show details
        firestore.collection("jobs").document(application.jobId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val job = document.toObject(Job::class.java)
                    job?.let {
                        // Show job details dialog (reuse from JobSeekerHomeFragment)
                        showJobDetailsDialog(it, application)
                    }
                } else {
                    Toast.makeText(context, "Job no longer available", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading job: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showJobDetailsDialog(job: Job, application: Application) {
        val detailsView = LayoutInflater.from(context).inflate(R.layout.dialog_job_details, null)

        detailsView.findViewById<TextView>(R.id.tvDialogJobTitle).text = job.title
        detailsView.findViewById<TextView>(R.id.tvDialogEmployerName).text = "Employer: ${job.employerName}"
        detailsView.findViewById<TextView>(R.id.tvDialogCategory).text = "Category: ${job.category}"
        detailsView.findViewById<TextView>(R.id.tvDialogLocation).text = "Location: ${job.location}"
        detailsView.findViewById<TextView>(R.id.tvDialogAmount).text = "Pay: ₱${String.format(java.util.Locale.US, "%.2f", job.amount)} / ${job.paymentType}"
        detailsView.findViewById<TextView>(R.id.tvDialogDateTime).text = "Date/Time: ${job.dateTime}"
        detailsView.findViewById<TextView>(R.id.tvDialogDescription).text = job.description
        detailsView.findViewById<TextView>(R.id.tvDialogRequirements).text = if (job.requirements.isNotEmpty()) job.requirements else "No specific requirements"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(detailsView)
            .setPositiveButton("Close", null)

        if (application.status == "Accepted") {
            dialog.setNeutralButton("Contact Employer") { _, _ ->
                contactEmployer(application)
            }
        }

        dialog.show()
    }

    private fun showCancelConfirmation(application: Application) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Application")
            .setMessage("Are you sure you want to cancel your application for '${application.jobTitle}'?\n\nThis action cannot be undone.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelApplication(application)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelApplication(application: Application) {
        firestore.collection("applications").document(application.id)
            .delete()
            .addOnSuccessListener {
                // Update job applicants count
                firestore.collection("jobs").document(application.jobId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val currentCount = doc.getLong("applicantsCount")?.toInt() ?: 0
                        if (currentCount > 0) {
                            firestore.collection("jobs").document(application.jobId)
                                .update("applicantsCount", currentCount - 1)
                        }
                    }

                Toast.makeText(context, "Application cancelled successfully", Toast.LENGTH_SHORT).show()

                // Reload applications
                loadAppliedJobs()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("JobSeekerJobsFragment", "Error cancelling: ${e.message}", e)
                Toast.makeText(context, "Failed to cancel application", Toast.LENGTH_SHORT).show()
            }
    }

    private fun viewActiveJobDetails(application: Application) {
        // Load the job and employer details
        firestore.collection("jobs").document(application.jobId)
            .get()
            .addOnSuccessListener { jobDoc ->
                if (!jobDoc.exists()) {
                    Toast.makeText(context, "Job not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val job = jobDoc.toObject(Job::class.java)
                if (job == null) {
                    Toast.makeText(context, "Error loading job details", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Load employer details
                firestore.collection("users").document(application.employerId)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val employerPhone = userDoc.getString("phone") ?: "Not available"
                        val employerEmail = userDoc.getString("email") ?: application.employerEmail

                        showActiveJobDetailsDialog(job, application, employerPhone, employerEmail)
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("JobSeekerJobsFragment", "Error loading employer: ${e.message}", e)
                        // Show dialog without phone
                        showActiveJobDetailsDialog(job, application, "Not available", application.employerEmail)
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("JobSeekerJobsFragment", "Error loading job: ${e.message}", e)
                Toast.makeText(context, "Error loading job details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showActiveJobDetailsDialog(
        job: Job,
        application: Application,
        employerPhone: String,
        employerEmail: String
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_active_job_details, null)

        // Set job details
        dialogView.findViewById<TextView>(R.id.tvDialogJobTitle).text = job.title
        dialogView.findViewById<TextView>(R.id.tvDialogEmployerName).text = application.employerName
        dialogView.findViewById<TextView>(R.id.tvDialogEmployerPhone).text = employerPhone
        dialogView.findViewById<TextView>(R.id.tvDialogEmployerEmail).text = employerEmail
        dialogView.findViewById<TextView>(R.id.tvDialogCategory).text = job.category
        dialogView.findViewById<TextView>(R.id.tvDialogLocation).text = job.location
        dialogView.findViewById<TextView>(R.id.tvDialogAmount).text = "₱${String.format(java.util.Locale.US, "%.2f", job.amount)} / ${job.paymentType}"
        dialogView.findViewById<TextView>(R.id.tvDialogDateTime).text = job.dateTime
        dialogView.findViewById<TextView>(R.id.tvDialogDescription).text = job.description
        dialogView.findViewById<TextView>(R.id.tvDialogRequirements).text =
            if (job.requirements.isNotEmpty()) job.requirements else "No specific requirements"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .setNeutralButton("Message Employer") { _, _ ->
                messageEmployer(application)
            }
            .create()

        dialog.show()
    }

    private fun messageEmployer(application: Application) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login to send messages", Toast.LENGTH_SHORT).show()
            return
        }

        // Open ChatActivity with the application details
        // Conversation will be created automatically if it doesn't exist
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            // Don't pass conversationId - let ChatActivity create it properly
            // This ensures the conversation is created with all required fields in Firestore
            putExtra(ChatActivity.EXTRA_OTHER_USER_ID, application.employerId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, application.employerName)
            putExtra(ChatActivity.EXTRA_JOB_ID, application.jobId)
            putExtra(ChatActivity.EXTRA_JOB_TITLE, application.jobTitle)
            putExtra(ChatActivity.EXTRA_APPLICATION_ID, application.id)
        }
        startActivity(intent)
    }


    private fun contactEmployer(application: Application) {
        val options = arrayOf(
            "Call Employer",
            "Send Email",
            "Send Message (Coming Soon)",
            "Cancel"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Contact ${application.employerName}")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Call employer (would need phone number in database)
                        Toast.makeText(context, "Phone number will be available soon", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // Send email
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${application.employerEmail}")
                            putExtra(Intent.EXTRA_SUBJECT, "Regarding: ${application.jobTitle}")
                        }
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    2 -> {
                        Toast.makeText(context, "Messaging feature coming soon", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (isAppliedTabSelected) {
            loadAppliedJobs()
        } else {
            loadActiveJobs()
        }
    }

    private fun selectAppliedTab(
        tabApplied: TextView,
        tabActive: TextView,
        underlineApplied: View,
        underlineActive: View,
        tvJobsContent: TextView
    ) {
        isAppliedTabSelected = true

        // Applied tab - 100% opacity
        tabApplied.alpha = 1.0f
        underlineApplied.alpha = 1.0f
        tabApplied.setTextAppearance(android.R.style.TextAppearance_Medium)

        // Active tab - 50% opacity
        tabActive.alpha = 0.5f
        underlineActive.alpha = 0.5f

        tvJobsContent.text = "Applied Jobs"
        Toast.makeText(context, "Showing Applied Jobs", Toast.LENGTH_SHORT).show()
        // TODO: Load applied jobs data
    }

    private fun selectActiveTab(
        tabApplied: TextView,
        tabActive: TextView,
        underlineApplied: View,
        underlineActive: View,
        tvJobsContent: TextView
    ) {
        isAppliedTabSelected = false

        // Applied tab - 50% opacity
        tabApplied.alpha = 0.5f
        underlineApplied.alpha = 0.5f

        // Active tab - 100% opacity
        tabActive.alpha = 1.0f
        underlineActive.alpha = 1.0f
        tabActive.setTextAppearance(android.R.style.TextAppearance_Medium)

        tvJobsContent.text = "Active Jobs"
        Toast.makeText(context, "Showing Active Jobs", Toast.LENGTH_SHORT).show()
        // TODO: Load active jobs data
    }

    private fun showJobCompletionForm(application: Application) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_job_completion, null)

        // Reset state - image upload disabled for now
        // selectedImageUri = null

        // Load job details first
        firestore.collection("jobs").document(application.jobId)
            .get()
            .addOnSuccessListener { document ->
                val job = document.toObject(Job::class.java)
                
                // Populate job details
                dialogView.findViewById<TextView>(R.id.tvCompletionJobTitle).text = application.jobTitle
                dialogView.findViewById<TextView>(R.id.tvCompletionEmployerName).text = "Employer: ${application.employerName}"
                dialogView.findViewById<TextView>(R.id.tvCompletionPayment).text = 
                    "Payment: ₱${String.format(java.util.Locale.US, "%.2f", job?.amount ?: 0.0)} / ${job?.paymentType ?: "Per Job"}"
                dialogView.findViewById<TextView>(R.id.tvCompletionLocation).text = "Location: ${job?.location ?: "N/A"}"
                dialogView.findViewById<TextView>(R.id.tvCompletionDateTime).text = "Scheduled: ${job?.dateTime ?: "N/A"}"

                // Get UI elements
                val cbJobCompleted = dialogView.findViewById<CheckBox>(R.id.cbJobCompleted)
                val cbHasIssues = dialogView.findViewById<CheckBox>(R.id.cbHasIssues)
                val tilConcerns = dialogView.findViewById<TextInputLayout>(R.id.tilConcerns)
                val etConcerns = dialogView.findViewById<TextInputEditText>(R.id.etConcerns)
                // Image upload disabled for now - will be implemented later
                // val btnUploadPhoto = dialogView.findViewById<Button>(R.id.btnUploadPhoto)
                // val cvImagePreview = dialogView.findViewById<CardView>(R.id.cvImagePreview)
                // val ivWorkPhoto = dialogView.findViewById<ImageView>(R.id.ivWorkPhoto)
                // val btnRemovePhoto = dialogView.findViewById<ImageButton>(R.id.btnRemovePhoto)
                val etAdditionalNotes = dialogView.findViewById<TextInputEditText>(R.id.etAdditionalNotes)
                val btnCancelCompletion = dialogView.findViewById<Button>(R.id.btnCancelCompletion)
                val btnSubmitCompletion = dialogView.findViewById<Button>(R.id.btnSubmitCompletion)

                // Handle checkbox interactions
                cbJobCompleted.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        cbHasIssues.isChecked = false
                        tilConcerns.visibility = View.GONE
                    }
                }

                cbHasIssues.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        cbJobCompleted.isChecked = false
                        tilConcerns.visibility = View.VISIBLE
                    } else {
                        tilConcerns.visibility = View.GONE
                    }
                }

                // Image upload functionality disabled for now - will be implemented later
                /*
                // Handle image upload
                btnUploadPhoto.setOnClickListener {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    imagePickerLauncher.launch(intent)
                }

                btnRemovePhoto.setOnClickListener {
                    selectedImageUri = null
                    cvImagePreview.visibility = View.GONE
                    ivWorkPhoto.setImageURI(null)
                }
                */

                // Create and show dialog
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()


                btnCancelCompletion.setOnClickListener {
                    dialog.dismiss()
                }

                btnSubmitCompletion.setOnClickListener {
                    submitJobCompletion(
                        application, 
                        job,
                        cbJobCompleted.isChecked,
                        cbHasIssues.isChecked,
                        etConcerns.text.toString(),
                        etAdditionalNotes.text.toString(),
                        dialog
                    )
                }

                dialog.show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading job details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /* IMAGE UPLOAD DISABLED - TO RE-ENABLE LATER, UNCOMMENT THIS FUNCTION
    private fun updateImagePreview(uri: Uri) {
        currentCompletionDialog?.let { dialog ->
            val dialogView = dialog.findViewById<View>(android.R.id.content)
            dialogView?.let {
                val cvImagePreview = it.findViewById<CardView>(R.id.cvImagePreview)
                val ivWorkPhoto = it.findViewById<ImageView>(R.id.ivWorkPhoto)
                
                cvImagePreview?.visibility = View.VISIBLE
                ivWorkPhoto?.setImageURI(uri)
            }
        }
    }
    */

    private fun submitJobCompletion(
        application: Application,
        job: Job?,
        isCompleted: Boolean,
        hasIssues: Boolean,
        concerns: String,
        additionalNotes: String,
        dialog: AlertDialog
    ) {
        // Validation
        if (!isCompleted && !hasIssues) {
            Toast.makeText(context, "Please select completion status or report issues", Toast.LENGTH_SHORT).show()
            return
        }

        if (hasIssues && concerns.trim().isEmpty()) {
            Toast.makeText(context, "Please describe your concerns", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to submit completion", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress
        val progressDialog = AlertDialog.Builder(requireContext())
            .setMessage("Submitting completion form...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        // Image upload disabled for now - directly save completion data without photo
        // When re-enabled, uncomment the uploadWorkPhoto logic below
        saveCompletionData(
            application, job, currentUser.uid, isCompleted, hasIssues,
            concerns, "", additionalNotes, progressDialog, dialog
        )

        /*
        // TO RE-ENABLE IMAGE UPLOAD LATER:
        // Upload image if selected, then save completion data
        if (selectedImageUri != null) {
            uploadWorkPhoto(selectedImageUri!!) { imageUrl ->
                saveCompletionData(
                    application, job, currentUser.uid, isCompleted, hasIssues, 
                    concerns, imageUrl, additionalNotes, progressDialog, dialog
                )
            }
        } else {
            saveCompletionData(
                application, job, currentUser.uid, isCompleted, hasIssues, 
                concerns, "", additionalNotes, progressDialog, dialog
            )
        }
        */
    }

    /* IMAGE UPLOAD DISABLED - TO RE-ENABLE LATER, UNCOMMENT THIS FUNCTION
    private fun uploadWorkPhoto(imageUri: Uri, onSuccess: (String) -> Unit) {
        val fileName = "work_photos/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
                // Continue without photo
                onSuccess("")
            }
    }
    */

    private fun saveCompletionData(
        application: Application,
        job: Job?,
        userId: String,
        isCompleted: Boolean,
        hasIssues: Boolean,
        concerns: String,
        photoUrl: String,
        additionalNotes: String,
        progressDialog: AlertDialog,
        completionDialog: AlertDialog
    ) {
        // Get user name
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val userName = userDoc.getString("name") ?: "Job Seeker"

                val completion = JobCompletion(
                    applicationId = application.id,
                    jobId = application.jobId,
                    jobTitle = application.jobTitle,
                    jobSeekerId = userId,
                    jobSeekerName = userName,
                    employerId = application.employerId,
                    employerName = application.employerName,
                    isCompleted = isCompleted,
                    hasIssues = hasIssues,
                    concerns = concerns,
                    workPhotoUrl = photoUrl,
                    additionalNotes = additionalNotes,
                    submittedAt = Timestamp.now(),
                    reviewedByEmployer = false,
                    paymentReleased = false
                )

                firestore.collection("jobCompletions")
                    .add(completion)
                    .addOnSuccessListener { docRef ->
                        // Update application status to "Completed"
                        firestore.collection("applications").document(application.id)
                            .update(
                                mapOf(
                                    "status" to "Completed",
                                    "completedAt" to Timestamp.now()
                                )
                            )
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                completionDialog.dismiss()
                                
                                Toast.makeText(
                                    context,
                                    "Completion submitted successfully! The employer will review it.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Show confirmation
                                showCompletionConfirmation(isCompleted, hasIssues)
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
                            "Error submitting completion: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCompletionConfirmation(isCompleted: Boolean, hasIssues: Boolean) {
        val title = if (isCompleted) "Job Completed! ✓" else "Concerns Reported"
        val message = if (isCompleted) {
            "Your job completion has been submitted to the employer for review. " +
            "You will be notified once the employer confirms and releases payment."
        } else {
            "Your concerns have been submitted to the employer. " +
            "They will review your report and contact you to resolve the issues."
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}


