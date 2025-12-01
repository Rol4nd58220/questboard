package com.example.questboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EmployerJobsAdapter(
    private var jobs: MutableList<Job>,
    private val onEditClick: (Job) -> Unit,
    private val onDeleteClick: (Job) -> Unit,
    private val onViewApplicantsClick: (Job) -> Unit
) : RecyclerView.Adapter<EmployerJobsAdapter.JobViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employer_job, parent, false)
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

    fun removeJob(job: Job) {
        val position = jobs.indexOf(job)
        if (position != -1) {
            jobs.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvJobCategory: TextView = itemView.findViewById(R.id.tvJobCategory)
        private val tvJobLocation: TextView = itemView.findViewById(R.id.tvJobLocation)
        private val tvJobAmount: TextView = itemView.findViewById(R.id.tvJobAmount)
        private val tvJobStatus: TextView = itemView.findViewById(R.id.tvJobStatus)
        private val tvApplicantsCount: TextView = itemView.findViewById(R.id.tvApplicantsCount)
        private val tvPostedDate: TextView = itemView.findViewById(R.id.tvPostedDate)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditJob)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDeleteJob)
        private val btnViewApplicants: Button = itemView.findViewById(R.id.btnViewApplicants)
        private val btnToggleStatus: Button = itemView.findViewById(R.id.btnToggleStatus)

        fun bind(job: Job) {
            tvJobTitle.text = job.title
            tvJobCategory.text = job.category
            tvJobLocation.text = job.location
            tvJobAmount.text = "₱${String.format(Locale.US, "%.2f", job.amount)} ${job.paymentType}"
            tvJobStatus.text = job.status
            tvApplicantsCount.text = "${job.applicantsCount} Applicants"

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            tvPostedDate.text = dateFormat.format(job.createdAt.toDate())

            // Set status color
            when (job.status) {
                "Open" -> tvJobStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_light))
                "Closed" -> tvJobStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_light))
                "In Progress" -> tvJobStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_light))
                "Completed" -> tvJobStatus.setTextColor(itemView.context.getColor(android.R.color.holo_blue_light))
            }

            // Edit button
            btnEdit.setOnClickListener {
                onEditClick(job)
            }

            // Delete button
            btnDelete.setOnClickListener {
                showDeleteConfirmation(job)
            }

            // View applicants button
            btnViewApplicants.setOnClickListener {
                onViewApplicantsClick(job)
            }

            // Toggle status button
            btnToggleStatus.text = if (job.status == "Open") "Close Job" else "Reopen Job"
            btnToggleStatus.setOnClickListener {
                toggleJobStatus(job)
            }
        }

        private fun showDeleteConfirmation(job: Job) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Job")
                .setMessage("Are you sure you want to delete '${job.title}'? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteClick(job)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun toggleJobStatus(job: Job) {
            val newStatus = if (job.status == "Open") "Closed" else "Open"

            firestore.collection("jobs").document(job.id)
                .update("status", newStatus)
                .addOnSuccessListener {
                    job.status = newStatus
                    notifyItemChanged(adapterPosition)
                    Toast.makeText(itemView.context, "Job status updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(itemView.context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

