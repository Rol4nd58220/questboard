package com.example.questboard

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Employer Applicants Fragment
 * Displays: All applicants across jobs, manage applications, shortlist candidates
 */
class EmployerApplicantsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
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

        val dialog = AlertDialog.Builder(requireContext())
            .setView(detailsView)
            .setPositiveButton("Close", null)
            .setNeutralButton("Contact") { _, _ ->
                contactApplicant(application)
            }

        if (application.status == "Pending") {
            dialog.setNegativeButton("Accept") { _, _ ->
                acceptApplication(application)
            }
        }

        dialog.show()
    }

    private fun contactApplicant(application: Application) {
        val options = arrayOf(
            "Send Email",
            "Call Applicant",
            "Send Message (Coming Soon)",
            "Cancel"
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
                        Toast.makeText(context, "Messaging feature coming soon", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadApplicants()
    }
}

