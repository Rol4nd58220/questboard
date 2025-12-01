package com.example.questboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ApplicantsAdapter(
    private val applicants: List<Applicant>,
    private val onViewClick: (Applicant) -> Unit,
    private val onRejectClick: (Applicant) -> Unit,
    private val onShortlistClick: (Applicant) -> Unit
) : RecyclerView.Adapter<ApplicantsAdapter.ApplicantViewHolder>() {

    class ApplicantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAppliedTime: TextView = itemView.findViewById(R.id.tvAppliedTime)
        val tvApplicantName: TextView = itemView.findViewById(R.id.tvApplicantName)
        val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val btnShortlist: Button = itemView.findViewById(R.id.btnShortlist)
        val ivApplicantPhoto: ImageView = itemView.findViewById(R.id.ivApplicantPhoto)
        val chipGroupSkills: ChipGroup = itemView.findViewById(R.id.chipGroupSkills)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvReviewCount: TextView = itemView.findViewById(R.id.tvReviewCount)
        val tvViewRates: TextView = itemView.findViewById(R.id.tvViewRates)
        val progressBar5: ProgressBar = itemView.findViewById(R.id.progressBar5)
        val progressBar4: ProgressBar = itemView.findViewById(R.id.progressBar4)
        val progressBar3: ProgressBar = itemView.findViewById(R.id.progressBar3)
        val progressBar2: ProgressBar = itemView.findViewById(R.id.progressBar2)
        val progressBar1: ProgressBar = itemView.findViewById(R.id.progressBar1)
        val tvPercent5: TextView = itemView.findViewById(R.id.tvPercent5)
        val tvPercent4: TextView = itemView.findViewById(R.id.tvPercent4)
        val tvPercent3: TextView = itemView.findViewById(R.id.tvPercent3)
        val tvPercent2: TextView = itemView.findViewById(R.id.tvPercent2)
        val tvPercent1: TextView = itemView.findViewById(R.id.tvPercent1)
        val tvBio: TextView = itemView.findViewById(R.id.tvBio)
        val btnView: Button = itemView.findViewById(R.id.btnView)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applicant, parent, false)
        return ApplicantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        val applicant = applicants[position]

        holder.tvAppliedTime.text = applicant.appliedTime
        holder.tvApplicantName.text = applicant.name
        holder.tvJobTitle.text = applicant.jobTitle
        holder.tvDistance.text = applicant.distance
        holder.tvRating.text = String.format("%.1f", applicant.rating)
        holder.tvReviewCount.text = "${applicant.reviewCount} reviews"
        holder.tvBio.text = applicant.bio

        // Load skills
        holder.chipGroupSkills.removeAllViews()
        applicant.skills.forEach { skill ->
            val chip = Chip(holder.itemView.context)
            chip.text = skill
            chip.setTextColor(holder.itemView.context.getColor(R.color.text_primary))
            chip.chipBackgroundColor = holder.itemView.context.getColorStateList(R.color.card_background)
            chip.isClickable = false
            chip.textSize = 11f
            holder.chipGroupSkills.addView(chip)
        }

        // Set rating breakdown
        applicant.ratingBreakdown[5]?.let {
            holder.progressBar5.progress = it
            holder.tvPercent5.text = "$it%"
        }
        applicant.ratingBreakdown[4]?.let {
            holder.progressBar4.progress = it
            holder.tvPercent4.text = "$it%"
        }
        applicant.ratingBreakdown[3]?.let {
            holder.progressBar3.progress = it
            holder.tvPercent3.text = "$it%"
        }
        applicant.ratingBreakdown[2]?.let {
            holder.progressBar2.progress = it
            holder.tvPercent2.text = "$it%"
        }
        applicant.ratingBreakdown[1]?.let {
            holder.progressBar1.progress = it
            holder.tvPercent1.text = "$it%"
        }

        // Load photo (use image loading library like Glide/Coil)
        // Glide.with(holder.itemView.context)
        //     .load(applicant.photoUrl)
        //     .placeholder(R.drawable.placeholder_avatar)
        //     .into(holder.ivApplicantPhoto)

        // Click listeners
        holder.btnShortlist.setOnClickListener {
            onShortlistClick(applicant)
        }

        holder.btnView.setOnClickListener {
            onViewClick(applicant)
        }

        holder.btnReject.setOnClickListener {
            onRejectClick(applicant)
        }

        holder.tvViewRates.setOnClickListener {
            // Show detailed rates
        }
    }

    override fun getItemCount(): Int = applicants.size
}

// Data class for Applicant
data class Applicant(
    val id: String,
    val name: String,
    val jobTitle: String,
    val appliedTime: String,
    val photoUrl: String?,
    val skills: List<String>,
    val distance: String,
    val rating: Float,
    val reviewCount: Int,
    val ratingBreakdown: Map<Int, Int>, // star rating to percentage
    val bio: String,
    var isShortlisted: Boolean = false
)

