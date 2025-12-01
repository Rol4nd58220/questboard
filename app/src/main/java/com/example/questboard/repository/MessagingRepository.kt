package com.example.questboard.repository

import android.util.Log
import com.example.questboard.models.Conversation
import com.example.questboard.models.Message
import com.example.questboard.models.ParticipantInfo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MessagingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val conversationsRef = db.collection("conversations")

    companion object {
        private const val TAG = "MessagingRepository"
    }

    /**
     * Get or create a conversation between two users for a specific job
     */
    suspend fun getOrCreateConversation(
        jobSeekerId: String,
        employerId: String,
        jobId: String,
        jobTitle: String,
        applicationId: String,
        jobSeekerName: String,
        employerName: String
    ): Result<String> {
        return try {
            // Create conversation ID (format: jobSeekerId_employerId_jobId)
            val conversationId = "${jobSeekerId}_${employerId}_${jobId}"
            Log.d(TAG, "Attempting to get or create conversation: $conversationId")
            Log.d(TAG, "JobSeeker: $jobSeekerId ($jobSeekerName), Employer: $employerId ($employerName)")

            // Check if conversation already exists
            val existingConv = conversationsRef.document(conversationId).get().await()

            if (existingConv.exists()) {
                Log.d(TAG, "Conversation already exists: $conversationId")
                Result.success(conversationId)
            } else {
                // Create new conversation
                val conversation = Conversation(
                    conversationId = conversationId,
                    participants = listOf(jobSeekerId, employerId),
                    participantDetails = mapOf(
                        jobSeekerId to ParticipantInfo(jobSeekerName, "jobseeker"),
                        employerId to ParticipantInfo(employerName, "employer")
                    ),
                    jobId = jobId,
                    jobTitle = jobTitle,
                    applicationId = applicationId,
                    lastMessage = "Application submitted",
                    lastMessageTime = Timestamp.now(),
                    lastMessageSenderId = jobSeekerId,
                    unreadCount = mapOf(
                        jobSeekerId to 0,
                        employerId to 1
                    ),
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )

                Log.d(TAG, "Creating new conversation with participants: ${conversation.participants}")
                conversationsRef.document(conversationId).set(conversation).await()
                Log.d(TAG, "Conversation document created successfully")

                // Add system message
                sendSystemMessage(conversationId, "$jobSeekerName applied for $jobTitle")

                Log.d(TAG, "Created new conversation: $conversationId")
                Result.success(conversationId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating conversation", e)
            Result.failure(e)
        }
    }

    /**
     * Get all conversations for current user with real-time updates
     */
    fun getUserConversations(
        userId: String,
        onConversationsChanged: (List<Conversation>) -> Unit
    ): ListenerRegistration {
        Log.d(TAG, "Setting up conversation listener for user: $userId")

        return conversationsRef
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to conversations", error)
                    onConversationsChanged(emptyList())
                    return@addSnapshotListener
                }

                Log.d(TAG, "Snapshot received: ${snapshot?.documents?.size ?: 0} documents")

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val conv = doc.toObject(Conversation::class.java)
                        Log.d(TAG, "Parsed conversation: ${doc.id}, participants: ${conv?.participants}")
                        conv
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing conversation: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "Loaded ${conversations.size} conversations for user $userId (sorted by Firestore)")
                onConversationsChanged(conversations)
            }
    }

    /**
     * Get messages for a conversation with real-time updates
     */
    fun getMessages(
        conversationId: String,
        onMessagesChanged: (List<Message>) -> Unit
    ): ListenerRegistration {
        return conversationsRef
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to messages", error)
                    onMessagesChanged(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Message::class.java)?.copy(messageId = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "Loaded ${messages.size} messages for conversation $conversationId")
                onMessagesChanged(messages)
            }
    }

    /**
     * Send a text message
     */
    suspend fun sendMessage(
        conversationId: String,
        text: String,
        senderName: String
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

            val message = Message(
                senderId = currentUserId,
                senderName = senderName,
                text = text,
                timestamp = Timestamp.now(),
                read = false,
                type = "text"
            )

            // Add message to subcollection
            conversationsRef
                .document(conversationId)
                .collection("messages")
                .add(message)
                .await()

            // Update conversation's last message
            updateLastMessage(conversationId, text, currentUserId)

            Log.d(TAG, "Message sent successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            Result.failure(e)
        }
    }

    /**
     * Send a system message (like "Application accepted")
     */
    private suspend fun sendSystemMessage(conversationId: String, text: String) {
        try {
            val message = Message(
                senderId = "system",
                senderName = "System",
                text = text,
                timestamp = Timestamp.now(),
                read = false,
                type = "system"
            )

            conversationsRef
                .document(conversationId)
                .collection("messages")
                .add(message)
                .await()

            Log.d(TAG, "System message sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending system message", e)
        }
    }

    /**
     * Update conversation's last message info
     */
    private suspend fun updateLastMessage(conversationId: String, text: String, senderId: String) {
        try {
            // Get current conversation to update unread counts
            val convDoc = conversationsRef.document(conversationId).get().await()
            val conversation = convDoc.toObject(Conversation::class.java)

            if (conversation != null) {
                // Increment unread count for other participant
                val unreadCount = conversation.unreadCount.toMutableMap()
                conversation.participants.forEach { participantId ->
                    if (participantId != senderId) {
                        unreadCount[participantId] = (unreadCount[participantId] ?: 0) + 1
                    }
                }

                // Update conversation
                conversationsRef.document(conversationId).update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to Timestamp.now(),
                        "lastMessageSenderId" to senderId,
                        "unreadCount" to unreadCount,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()

                Log.d(TAG, "Last message updated")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last message", e)
        }
    }

    /**
     * Mark all messages in a conversation as read for current user
     */
    suspend fun markMessagesAsRead(conversationId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

            // Reset unread count for current user
            conversationsRef.document(conversationId).update(
                "unreadCount.$currentUserId", 0
            ).await()

            Log.d(TAG, "Messages marked as read")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
            Result.failure(e)
        }
    }

    /**
     * Get conversation by ID
     */
    suspend fun getConversation(conversationId: String): Result<Conversation> {
        return try {
            val doc = conversationsRef.document(conversationId).get().await()
            val conversation = doc.toObject(Conversation::class.java)

            if (conversation != null) {
                Result.success(conversation)
            } else {
                Result.failure(Exception("Conversation not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conversation", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a conversation and all its messages
     * Note: This deletes for BOTH participants (complete deletion)
     */
    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

            // Verify current user is a participant
            val convDoc = conversationsRef.document(conversationId).get().await()
            val conversation = convDoc.toObject(Conversation::class.java)

            if (conversation == null) {
                return Result.failure(Exception("Conversation not found"))
            }

            if (!conversation.participants.contains(currentUserId)) {
                return Result.failure(Exception("Unauthorized to delete this conversation"))
            }

            // Delete all messages in the conversation
            val messagesSnapshot = conversationsRef
                .document(conversationId)
                .collection("messages")
                .get()
                .await()

            messagesSnapshot.documents.forEach { messageDoc ->
                messageDoc.reference.delete().await()
            }

            // Delete the conversation document
            conversationsRef.document(conversationId).delete().await()

            Log.d(TAG, "Deleted conversation: $conversationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting conversation", e)
            Result.failure(e)
        }
    }

    /**
     * Search conversations by participant name or job title
     */
    fun searchConversations(
        userId: String,
        query: String,
        onResults: (List<Conversation>) -> Unit
    ): ListenerRegistration {
        return conversationsRef
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error searching conversations", error)
                    onResults(emptyList())
                    return@addSnapshotListener
                }

                val allConversations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Conversation::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                // Filter by search query (job title or participant name)
                val filtered = allConversations.filter { conv ->
                    conv.jobTitle.contains(query, ignoreCase = true) ||
                    conv.participantDetails.values.any { it.name.contains(query, ignoreCase = true) }
                }

                onResults(filtered)
            }
    }
}

