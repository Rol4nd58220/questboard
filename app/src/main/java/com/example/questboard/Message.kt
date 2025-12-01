package com.example.questboard

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Message data model for in-app messaging
 */
data class Message(
    @DocumentId
    var id: String = "",
    var conversationId: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var receiverId: String = "",
    var receiverName: String = "",
    var messageText: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    var isRead: Boolean = false,
    var messageType: String = "text" // text, image, etc.
) {
    constructor() : this(
        id = "",
        conversationId = "",
        senderId = "",
        senderName = "",
        receiverId = "",
        receiverName = "",
        messageText = "",
        timestamp = Timestamp.now(),
        isRead = false,
        messageType = "text"
    )
}

/**
 * Conversation data model
 * Represents a chat between jobseeker and employer
 */
data class Conversation(
    @DocumentId
    var id: String = "",
    var jobseekerId: String = "",
    var jobseekerName: String = "",
    var employerId: String = "",
    var employerName: String = "",
    var lastMessage: String = "",
    var lastMessageTime: Timestamp = Timestamp.now(),
    var unreadCount: Int = 0,
    var jobId: String = "", // Optional: reference to related job
    var jobTitle: String = "" // Optional: for context
) {
    constructor() : this(
        id = "",
        jobseekerId = "",
        jobseekerName = "",
        employerId = "",
        employerName = "",
        lastMessage = "",
        lastMessageTime = Timestamp.now(),
        unreadCount = 0,
        jobId = "",
        jobTitle = ""
    )
}

