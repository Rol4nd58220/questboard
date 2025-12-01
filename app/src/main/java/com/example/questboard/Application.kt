package com.example.questboard

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Application data model for Firestore
 */
data class Application(
    @DocumentId
    var id: String = "",
    var jobId: String = "",
    var jobTitle: String = "",
    var employerId: String = "",
    var employerName: String = "",
    var employerEmail: String = "",
    var applicantId: String = "",
    var applicantName: String = "",
    var applicantEmail: String = "",
    var applicantPhone: String = "",
    var status: String = "Pending", // Pending, Accepted, Rejected
    var appliedAt: Timestamp = Timestamp.now(),
    var respondedAt: Timestamp? = null,
    var message: String = "",
    var coverLetter: String = "",
    var isRead: Boolean = false,
    var notificationSent: Boolean = false
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        jobId = "",
        jobTitle = "",
        employerId = "",
        employerName = "",
        employerEmail = "",
        applicantId = "",
        applicantName = "",
        applicantEmail = "",
        applicantPhone = "",
        status = "Pending",
        appliedAt = Timestamp.now(),
        respondedAt = null,
        message = "",
        coverLetter = "",
        isRead = false,
        notificationSent = false
    )
}

