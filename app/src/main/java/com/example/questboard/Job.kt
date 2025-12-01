package com.example.questboard

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Job data model for Firestore
 */
data class Job(
    @DocumentId
    var id: String = "",
    var employerId: String = "",
    var employerName: String = "",
    var employerEmail: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "",
    var paymentType: String = "",
    var amount: Double = 0.0,
    var location: String = "",
    var dateTime: String = "",
    var requirements: String = "",
    var imageUrl: String = "",
    var status: String = "Open", // Open, Closed, In Progress, Completed
    var applicantsCount: Int = 0,
    var createdAt: Timestamp = Timestamp.now(),
    var updatedAt: Timestamp = Timestamp.now(),
    var isActive: Boolean = true
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        employerId = "",
        employerName = "",
        employerEmail = "",
        title = "",
        description = "",
        category = "",
        paymentType = "",
        amount = 0.0,
        location = "",
        dateTime = "",
        requirements = "",
        imageUrl = "",
        status = "Open",
        applicantsCount = 0,
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now(),
        isActive = true
    )
}

