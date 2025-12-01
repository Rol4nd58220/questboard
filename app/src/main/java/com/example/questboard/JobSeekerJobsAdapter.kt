package com.example.questboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class JobSeekerJobsAdapter(
    private var jobs: MutableList<Job>,
    private val onApplyClick: (Job) -> Unit,
    private val onViewDetailsClick: (Job) -> Unit
) : RecyclerView.Adapter<JobSeekerJobsAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jobseeker_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount(): Int = jobs.size

    fun updateJobs(newJobs: List<Job>) {
        jobs.clear()
        jobs.addAll(newJobs)
        notifyDataSetChanged()
    }

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvEmployerName: TextView = itemView.findViewById(R.id.tvEmployerName)
        private val tvJobCategory: TextView = itemView.findViewById(R.id.tvJobCategory)
        private val tvJobLocation: TextView = itemView.findViewById(R.id.tvJobLocation)
        private val tvJobAmount: TextView = itemView.findViewById(R.id.tvJobAmount)
        private val tvJobDescription: TextView = itemView.findViewById(R.id.tvJobDescription)
        private val tvPostedTime: TextView = itemView.findViewById(R.id.tvPostedTime)
        private val btnApply: Button = itemView.findViewById(R.id.btnApply)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)

        fun bind(job: Job) {
            tvJobTitle.text = job.title
            tvEmployerName.text = job.employerName
            tvJobCategory.text = job.category
            tvJobLocation.text = job.location
            tvJobAmount.text = "₱${String.format(Locale.US, "%.2f", job.amount)} / ${job.paymentType}"
            tvJobDescription.text = job.description

            // Format posted time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            tvPostedTime.text = "Posted: ${dateFormat.format(job.createdAt.toDate())}"

            // Apply button
            btnApply.setOnClickListener {
                onApplyClick(job)
            }

            // View details button
            btnViewDetails.setOnClickListener {
                onViewDetailsClick(job)
            }
        }
    }
}

