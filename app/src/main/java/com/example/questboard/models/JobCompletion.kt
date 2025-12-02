package com.example.questboard.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Job Completion data model
 * Stores completion information submitted by job seekers
 */
data class JobCompletion(
    @DocumentId
    var id: String = "",
    var applicationId: String = "",
    var jobId: String = "",
    var jobTitle: String = "",
    var jobSeekerId: String = "",
    var jobSeekerName: String = "",
    var employerId: String = "",
    var employerName: String = "",
    var isCompleted: Boolean = false,
    var hasIssues: Boolean = false,
    var concerns: String = "",
    var workPhotoUrl: String = "",
    var additionalNotes: String = "",
    var submittedAt: Timestamp = Timestamp.now(),
    var reviewedByEmployer: Boolean = false,
    var employerFeedback: String = "",
    var employerRating: Float = 0f,
    var paymentMethod: String = "", // "Cash" or "GCash"
    var paymentReleased: Boolean = false
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        applicationId = "",
        jobId = "",
        jobTitle = "",
        jobSeekerId = "",
        jobSeekerName = "",
        employerId = "",
        employerName = "",
        isCompleted = false,
        hasIssues = false,
        concerns = "",
        workPhotoUrl = "",
        additionalNotes = "",
        submittedAt = Timestamp.now(),
        reviewedByEmployer = false,
        employerFeedback = "",
        employerRating = 0f,
        paymentMethod = "",
        paymentReleased = false
    )
}

