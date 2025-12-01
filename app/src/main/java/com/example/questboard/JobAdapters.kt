package com.example.questboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for Recently Viewed Jobs (Horizontal Scroll)
 */
class RecentlyViewedJobsAdapter(
    private val jobs: List<Job>,
    private val onViewClick: (Job) -> Unit
) : RecyclerView.Adapter<RecentlyViewedJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgJobPhoto: ImageView = view.findViewById(R.id.imgJobPhoto)
        val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
        val tvJobDescription: TextView = view.findViewById(R.id.tvJobDescription)
        val btnView: Button = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recently_viewed_job, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobs[position]

        holder.tvJobTitle.text = job.title
        holder.tvJobDescription.text = job.description

        // TODO: Load image from Cloudinary when implemented
        // For now, placeholder is set in XML
        // holder.imgJobPhoto.setImageURI(Uri.parse(job.imageUrl))

        holder.btnView.setOnClickListener {
            // Navigate to JobDetailsActivity
            val intent = Intent(holder.itemView.context, JobDetailsActivity::class.java)
            intent.putExtra("JOB_ID", job.id)
            holder.itemView.context.startActivity(intent)

            // Also call the callback for tracking
            onViewClick(job)
        }
    }

    override fun getItemCount() = jobs.size
}

/**
 * Adapter for Pending Applications (Horizontal Scroll)
 */
class PendingApplicationsAdapter(
    private val applications: List<Application>,
    private val onViewClick: (Application) -> Unit
) : RecyclerView.Adapter<PendingApplicationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
        val tvJobDescription: TextView = view.findViewById(R.id.tvJobDescription)
        val tvAppliedDate: TextView = view.findViewById(R.id.tvAppliedDate)
        val btnView: Button = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val application = applications[position]

        holder.tvStatus.text = application.status.replaceFirstChar { it.uppercase() }
        holder.tvJobTitle.text = application.jobTitle
        holder.tvJobDescription.text = "Applied to: ${application.employerName}"

        // Format applied date
        val daysAgo = getDaysAgo(application.appliedAt?.toDate())
        holder.tvAppliedDate.text = "Applied: $daysAgo"

        // Set status color
        when (application.status.lowercase()) {
            "pending" -> holder.tvStatus.setTextColor(0xFFFFC107.toInt())
            "accepted" -> holder.tvStatus.setTextColor(0xFF4CAF50.toInt())
            "rejected" -> holder.tvStatus.setTextColor(0xFFF44336.toInt())
        }

        holder.btnView.setOnClickListener {
            onViewClick(application)
        }
    }

    override fun getItemCount() = applications.size

    private fun getDaysAgo(date: java.util.Date?): String {
        if (date == null) return "Recently"

        val now = System.currentTimeMillis()
        val diff = now - date.time
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} weeks ago"
            else -> "${days / 30} months ago"
        }
    }
}

/**
 * Adapter for Available Jobs (Vertical Scroll)
 */
class AvailableJobsAdapter(
    private val jobs: List<Job>,
    private val onViewClick: (Job) -> Unit
) : RecyclerView.Adapter<AvailableJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
        val tvJobDescription: TextView = view.findViewById(R.id.tvJobDescription)
        val tvPayment: TextView = view.findViewById(R.id.tvPayment)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val imgJobPhoto: ImageView = view.findViewById(R.id.imgJobPhoto)
        val btnView: Button = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_available_job, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobs[position]

        holder.tvJobTitle.text = job.title
        holder.tvJobDescription.text = job.description
        holder.tvPayment.text = "₱${job.amount}/${job.paymentType.lowercase()}"
        holder.tvLocation.text = job.location

        // TODO: Load image from Cloudinary when implemented
        // For now, placeholder is set in XML

        holder.btnView.setOnClickListener {
            // Navigate to JobDetailsActivity
            val intent = Intent(holder.itemView.context, JobDetailsActivity::class.java)
            intent.putExtra("JOB_ID", job.id)
            holder.itemView.context.startActivity(intent)

            // Also call the callback for tracking
            onViewClick(job)
        }
    }

    override fun getItemCount() = jobs.size
}

