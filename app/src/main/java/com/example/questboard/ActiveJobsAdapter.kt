package com.example.questboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ActiveJobsAdapter(
    private var applications: MutableList<Application>,
    private val onViewJobClick: (Application) -> Unit,
    private val onContactEmployerClick: (Application) -> Unit
) : RecyclerView.Adapter<ActiveJobsAdapter.ActiveJobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveJobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_job, parent, false)
        return ActiveJobViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActiveJobViewHolder, position: Int) {
        holder.bind(applications[position])
    }

    override fun getItemCount(): Int = applications.size

    fun updateApplications(newApplications: List<Application>) {
        applications.clear()
        applications.addAll(newApplications)
        notifyDataSetChanged()
    }

    inner class ActiveJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardActiveJob)
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvEmployerName: TextView = itemView.findViewById(R.id.tvEmployerName)
        private val tvAcceptedDate: TextView = itemView.findViewById(R.id.tvAcceptedDate)
        private val tvViewJob: TextView = itemView.findViewById(R.id.tvViewJob)
        private val tvContactEmployer: TextView = itemView.findViewById(R.id.tvContactEmployer)

        fun bind(application: Application) {
            tvJobTitle.text = application.jobTitle
            tvEmployerName.text = application.employerName

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val acceptedDate = application.respondedAt?.toDate() ?: application.appliedAt.toDate()
            tvAcceptedDate.text = "Accepted: ${dateFormat.format(acceptedDate)}"

            // View job button
            tvViewJob.setOnClickListener {
                onViewJobClick(application)
            }

            // Contact employer button
            tvContactEmployer.setOnClickListener {
                onContactEmployerClick(application)
            }
        }
    }
}

