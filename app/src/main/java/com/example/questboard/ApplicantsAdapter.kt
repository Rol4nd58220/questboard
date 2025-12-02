package com.example.questboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ApplicantsAdapter(
    private var applications: MutableList<Application>,
    private val onAcceptClick: (Application) -> Unit,
    private val onRejectClick: (Application) -> Unit,
    private val onViewApplicantClick: (Application) -> Unit
) : RecyclerView.Adapter<ApplicantsAdapter.ApplicantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applicant, parent, false)
        return ApplicantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
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

    inner class ApplicantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardApplicant)
        private val tvApplicantName: TextView = itemView.findViewById(R.id.tvApplicantName)
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvApplicationDate: TextView = itemView.findViewById(R.id.tvApplicationDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        private val btnReject: Button = itemView.findViewById(R.id.btnReject)
        private val btnViewApplicant: Button = itemView.findViewById(R.id.btnViewApplicant)

        fun bind(application: Application) {
            tvApplicantName.text = application.applicantName
            tvJobTitle.text = "Applied for: ${application.jobTitle}"

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            tvApplicationDate.text = "Applied: ${dateFormat.format(application.appliedAt.toDate())}"

            // Set status
            when (application.status) {
                "Pending" -> {
                    tvStatus.text = "Pending Review"
                    tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_light))
                    btnAccept.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE
                }
                "Accepted" -> {
                    tvStatus.text = "Accepted ✓"
                    tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_light))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }
                "Rejected" -> {
                    tvStatus.text = "Rejected"
                    tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_light))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }
                "Completed" -> {
                    tvStatus.text = "⏳ Awaiting Review"
                    tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_blue_light))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }
                "Reviewed" -> {
                    tvStatus.text = "Reviewed ✓"
                    tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }
                else -> {
                    tvStatus.text = application.status
                    tvStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }
            }

            // Show message if available
            if (application.message.isNotEmpty()) {
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = "\"${application.message}\""
            } else {
                tvMessage.visibility = View.GONE
            }

            // Button click listeners
            btnAccept.setOnClickListener {
                onAcceptClick(application)
            }

            btnReject.setOnClickListener {
                onRejectClick(application)
            }

            btnViewApplicant.setOnClickListener {
                onViewApplicantClick(application)
            }
        }
    }
}

