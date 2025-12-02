package com.example.questboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

/**
 * Adapter for Applied Jobs Cards
 * Shows jobs the user has applied to with status and time tracking
 */
class AppliedJobsCardAdapter(
    private var applications: MutableList<Application>,
    private val onViewJobClick: (Application) -> Unit,
    private val onCancelClick: (Application) -> Unit
) : RecyclerView.Adapter<AppliedJobsCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvApplicationStatus: TextView = view.findViewById(R.id.tvApplicationStatus)
        val tvJobStatus: TextView = view.findViewById(R.id.tvJobStatus)
        val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
        val tvEmployerName: TextView = view.findViewById(R.id.tvEmployerName)
        val tvPayment: TextView = view.findViewById(R.id.tvPayment)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvAppliedTime: TextView = view.findViewById(R.id.tvAppliedTime)
        val btnCancelApplication: Button = view.findViewById(R.id.btnCancelApplication)
        val btnViewJob: Button = view.findViewById(R.id.btnViewJob)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applied_job_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val application = applications[position]

        // Job Title
        holder.tvJobTitle.text = application.jobTitle

        // Employer Name
        holder.tvEmployerName.text = "Posted by: ${application.employerName}"

        // Get job details for payment and location
        // For now, we'll need to load this from Firestore or pass it with application
        // Simplified version - you can enhance this
        holder.tvPayment.text = "View details for payment"
        holder.tvLocation.text = "Location in details"

        // Application Status
        when (application.status) {
            "Pending" -> {
                holder.tvApplicationStatus.text = "Pending"
                holder.tvApplicationStatus.setTextColor(0xFFFFC107.toInt())
                holder.tvApplicationStatus.setBackgroundColor(0x33FFC107.toInt())
                holder.btnCancelApplication.visibility = View.VISIBLE
            }
            "Accepted" -> {
                holder.tvApplicationStatus.text = "Accepted"
                holder.tvApplicationStatus.setTextColor(0xFF4CAF50.toInt())
                holder.tvApplicationStatus.setBackgroundColor(0x334CAF50.toInt())
                holder.btnCancelApplication.visibility = View.GONE
            }
            "Rejected" -> {
                holder.tvApplicationStatus.text = "Not Selected"
                holder.tvApplicationStatus.setTextColor(0xFFF44336.toInt())
                holder.tvApplicationStatus.setBackgroundColor(0x33F44336.toInt())
                holder.btnCancelApplication.visibility = View.GONE
            }
            "JobTaken" -> {
                holder.tvApplicationStatus.text = "Pending"
                holder.tvApplicationStatus.setTextColor(0xFF888888.toInt())
                holder.tvJobStatus.visibility = View.VISIBLE
                holder.tvJobStatus.text = "Job taken by another applicant"
                holder.btnCancelApplication.visibility = View.GONE
            }
            "Completed" -> {
                holder.tvApplicationStatus.text = "Completed"
                holder.tvApplicationStatus.setTextColor(0xFF2196F3.toInt())
                holder.tvApplicationStatus.setBackgroundColor(0x332196F3.toInt())
                holder.tvJobStatus.visibility = View.VISIBLE
                holder.tvJobStatus.text = "Job has been completed"
                holder.btnCancelApplication.visibility = View.GONE
            }
            else -> {
                holder.tvApplicationStatus.text = application.status
            }
        }

        // Applied Time
        val timeAgo = getTimeAgo(application.appliedAt)
        holder.tvAppliedTime.text = "Applied: $timeAgo"

        // View Job Button
        holder.btnViewJob.setOnClickListener {
            onViewJobClick(application)
        }

        // Cancel Button
        holder.btnCancelApplication.setOnClickListener {
            onCancelClick(application)
        }
    }

    override fun getItemCount() = applications.size

    fun updateApplications(newApplications: List<Application>) {
        applications.clear()
        applications.addAll(newApplications)
        notifyDataSetChanged()
    }

    private fun getTimeAgo(timestamp: Timestamp?): String {
        if (timestamp == null) return "Unknown"

        val now = System.currentTimeMillis()
        val then = timestamp.toDate().time
        val diff = now - then

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
            hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
            days < 7 -> "$days day${if (days != 1L) "s" else ""} ago"
            days < 30 -> "${days / 7} week${if (days / 7 != 1L) "s" else ""} ago"
            else -> "${days / 30} month${if (days / 30 != 1L) "s" else ""} ago"
        }
    }
}

/**
 * Adapter for Active Jobs Cards
 * Shows jobs where the employer accepted the user
 */
class ActiveJobsCardAdapter(
    private var applications: MutableList<Application>,
    private val onViewJobClick: (Application) -> Unit,
    private val onMessageClick: (Application) -> Unit,
    private val onConfirmCompletionClick: (Application) -> Unit
) : RecyclerView.Adapter<ActiveJobsCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvActiveStatus: TextView = view.findViewById(R.id.tvActiveStatus)
        val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
        val tvEmployerName: TextView = view.findViewById(R.id.tvEmployerName)
        val tvPayment: TextView = view.findViewById(R.id.tvPayment)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvAcceptedTime: TextView = view.findViewById(R.id.tvAcceptedTime)
        val tvJobDateTime: TextView = view.findViewById(R.id.tvJobDateTime)
        val btnConfirmCompletion: Button = view.findViewById(R.id.btnConfirmCompletion)
        val btnMessageEmployer: Button = view.findViewById(R.id.btnMessageEmployer)
        val btnViewJob: Button = view.findViewById(R.id.btnViewJob)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_job_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val application = applications[position]

        // Job Title
        holder.tvJobTitle.text = application.jobTitle

        // Employer Name
        holder.tvEmployerName.text = "Employer: ${application.employerName}"

        // Payment and Location - simplified
        holder.tvPayment.text = "View for details"
        holder.tvLocation.text = "Location"

        // Accepted Time
        val timeAgo = getTimeAgo(application.respondedAt)
        holder.tvAcceptedTime.text = "Accepted: $timeAgo"

        // Job DateTime - would need to be added to Application model or loaded from job
        holder.tvJobDateTime.text = "Scheduled: Check job details"

        // Confirm Completion Button
        holder.btnConfirmCompletion.setOnClickListener {
            onConfirmCompletionClick(application)
        }

        // View Job Button
        holder.btnViewJob.setOnClickListener {
            onViewJobClick(application)
        }

        // Message Employer Button
        holder.btnMessageEmployer.setOnClickListener {
            onMessageClick(application)
        }
    }

    override fun getItemCount() = applications.size

    fun updateApplications(newApplications: List<Application>) {
        applications.clear()
        applications.addAll(newApplications)
        notifyDataSetChanged()
    }

    private fun getTimeAgo(timestamp: Timestamp?): String {
        if (timestamp == null) return "Recently"

        val now = System.currentTimeMillis()
        val then = timestamp.toDate().time
        val diff = now - then

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
            hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
            days < 7 -> "$days day${if (days != 1L) "s" else ""} ago"
            else -> "${days / 7} week${if (days / 7 != 1L) "s" else ""} ago"
        }
    }
}

