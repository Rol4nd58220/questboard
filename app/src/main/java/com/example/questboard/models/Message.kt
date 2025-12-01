package com.example.questboard.models

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false,
    val type: String = "text" // "text", "system", "application_status"
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", "", Timestamp.now(), false, "text")
}

