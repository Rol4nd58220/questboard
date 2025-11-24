package com.example.questboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JobPostsAdapter(
    private val jobPosts: List<JobPost>,
    private val onViewApplicantsClick: (JobPost) -> Unit
) : RecyclerView.Adapter<JobPostsAdapter.JobPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_applicants_post, parent, false)
        return JobPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobPostViewHolder, position: Int) {
        holder.bind(jobPosts[position])
    }

    override fun getItemCount(): Int = jobPosts.size

    inner class JobPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPostedTime: TextView = itemView.findViewById(R.id.tvPostedTime)
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvStatusApplicants: TextView = itemView.findViewById(R.id.tvStatusApplicants)
        private val btnViewApplicants: Button = itemView.findViewById(R.id.btnViewApplicants)
        private val ivJobImage: ImageView = itemView.findViewById(R.id.ivJobImage)

        fun bind(jobPost: JobPost) {
            tvPostedTime.text = itemView.context.getString(R.string.posted_time, jobPost.postedTime)
            tvJobTitle.text = jobPost.title
            tvStatusApplicants.text = itemView.context.getString(
                R.string.job_status,
                jobPost.status,
                jobPost.applicantsCount
            )

            // Set job image - use placeholder for now
            // In real implementation, load from URL using Glide/Coil
            ivJobImage.setImageResource(R.drawable.employer)

            btnViewApplicants.setOnClickListener {
                onViewApplicantsClick(jobPost)
            }
        }
    }
}

