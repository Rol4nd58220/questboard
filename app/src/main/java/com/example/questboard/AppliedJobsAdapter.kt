package com.example.questboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AppliedJobsAdapter(
    private var applications: MutableList<Application>,
    private val onViewJobClick: (Application) -> Unit,
    private val onCancelApplicationClick: (Application) -> Unit
) : RecyclerView.Adapter<AppliedJobsAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applied_job, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(applications[position])
    }

    override fun getItemCount(): Int = applications.size

    fun updateApplications(newApplications: List<Application>) {
        applications.clear()
        applications.addAll(newApplications)
        notifyDataSetChanged()
    }

    fun removeApplication(application: Application) {
        val position = applications.indexOf(application)
        if (position != -1) {
            applications.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardApplication)
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvEmployerName: TextView = itemView.findViewById(R.id.tvEmployerName)
        private val tvApplicationStatus: TextView = itemView.findViewById(R.id.tvApplicationStatus)
        private val tvAppliedDate: TextView = itemView.findViewById(R.id.tvAppliedDate)
        private val tvViewJob: TextView = itemView.findViewById(R.id.tvViewJob)
        private val tvCancelApplication: TextView? = itemView.findViewById(R.id.tvCancelApplication)

        fun bind(application: Application) {
            tvJobTitle.text = application.jobTitle
            tvEmployerName.text = application.employerName

            // Set status text and color
            when (application.status) {
                "Pending" -> {
                    tvApplicationStatus.text = "Pending"
                    tvApplicationStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_light))
                }
                "Accepted" -> {
                    tvApplicationStatus.text = "Accepted"
                    tvApplicationStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                "Rejected" -> {
                    tvApplicationStatus.text = "Rejected"
                    tvApplicationStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_light))
                }
            }

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            tvAppliedDate.text = "Applied: ${dateFormat.format(application.appliedAt.toDate())}"

            // View job button
            tvViewJob.setOnClickListener {
                onViewJobClick(application)
            }

            // Cancel application (only if pending)
            if (application.status == "Pending") {
                tvCancelApplication?.visibility = View.VISIBLE
                tvCancelApplication?.setOnClickListener {
                    onCancelApplicationClick(application)
                }
            } else {
                tvCancelApplication?.visibility = View.GONE
            }
        }
    }
}

