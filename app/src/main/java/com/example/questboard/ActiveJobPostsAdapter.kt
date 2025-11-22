package com.example.questboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class JobPost(
    val id: String,
    val title: String,
    val postedTime: String,
    val status: String,
    val applicantsCount: Int,
    val imageUrl: String? = null
)

class ActiveJobPostsAdapter(
    private val jobPosts: List<JobPost>,
    private val onViewApplicantsClick: (JobPost) -> Unit
) : RecyclerView.Adapter<ActiveJobPostsAdapter.JobPostViewHolder>() {

    class JobPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPostedTime: TextView = itemView.findViewById(R.id.tvPostedTime)
        val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val tvJobStatus: TextView = itemView.findViewById(R.id.tvJobStatus)
        val btnViewApplicants: Button = itemView.findViewById(R.id.btnViewApplicants)
        val ivJobImage: ImageView = itemView.findViewById(R.id.ivJobImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_job_post, parent, false)
        return JobPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobPostViewHolder, position: Int) {
        val jobPost = jobPosts[position]
        val context = holder.itemView.context

        holder.tvPostedTime.text = context.getString(R.string.posted_time, jobPost.postedTime)
        holder.tvJobTitle.text = jobPost.title
        holder.tvJobStatus.text = context.getString(R.string.job_status, jobPost.status, jobPost.applicantsCount)

        // Load image using your preferred image loading library (Glide, Picasso, Coil, etc.)
        // Example with placeholder:
        // Glide.with(holder.itemView.context)
        //     .load(jobPost.imageUrl)
        //     .placeholder(R.drawable.placeholder_job)
        //     .into(holder.ivJobImage)

        holder.btnViewApplicants.setOnClickListener {
            onViewApplicantsClick(jobPost)
        }
    }

    override fun getItemCount(): Int = jobPosts.size
}

