package com.example.questboard.models

import com.google.firebase.Timestamp

data class Conversation(
    val conversationId: String = "",
    val participants: List<String> = emptyList(),
    val participantDetails: Map<String, ParticipantInfo> = emptyMap(),
    val jobId: String = "",
    val jobTitle: String = "",
    val applicationId: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // No-arg constructor for Firestore
    constructor() : this(
        "",
        emptyList(),
        emptyMap(),
        "",
        "",
        "",
        "",
        Timestamp.now(),
        "",
        emptyMap(),
        Timestamp.now(),
        Timestamp.now()
    )
}

data class ParticipantInfo(
    val name: String = "",
    val accountType: String = "" // "jobseeker" or "employer"
) {
    // No-arg constructor for Firestore
    constructor() : this("", "")
}

