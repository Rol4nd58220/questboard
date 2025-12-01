package com.example.questboard

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questboard.adapters.MessageAdapter
import com.example.questboard.models.Conversation
import com.example.questboard.repository.MessagingRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var tvChatName: TextView
    private lateinit var tvJobTitle: TextView
    private lateinit var emptyState: LinearLayout

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagingRepository: MessagingRepository
    private var messagesListener: ListenerRegistration? = null

    private var conversationId: String = ""
    private var currentUserName: String = ""

    companion object {
        private const val TAG = "ChatActivity"
        const val EXTRA_CONVERSATION_ID = "conversation_id"
        const val EXTRA_OTHER_USER_ID = "other_user_id"
        const val EXTRA_OTHER_USER_NAME = "other_user_name"
        const val EXTRA_JOB_ID = "job_id"
        const val EXTRA_JOB_TITLE = "job_title"
        const val EXTRA_APPLICATION_ID = "application_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initializeViews()
        setupRecyclerView()

        messagingRepository = MessagingRepository()

        // Get conversation info from intent
        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: ""
        val otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: ""
        val otherUserName = intent.getStringExtra(EXTRA_OTHER_USER_NAME) ?: "User"
        val jobId = intent.getStringExtra(EXTRA_JOB_ID) ?: ""
        val jobTitle = intent.getStringExtra(EXTRA_JOB_TITLE) ?: ""
        val applicationId = intent.getStringExtra(EXTRA_APPLICATION_ID) ?: ""

        // Set header info
        tvChatName.text = otherUserName
        tvJobTitle.text = jobTitle

        // Get current user info
        loadCurrentUserInfo()

        // If conversation doesn't exist, create it
        if (conversationId.isEmpty() && otherUserId.isNotEmpty() && jobId.isNotEmpty()) {
            createConversation(otherUserId, jobId, jobTitle, applicationId, otherUserName)
        } else if (conversationId.isNotEmpty()) {
            loadConversation()
        }

        setupListeners()
    }

    private fun initializeViews() {
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnBack = findViewById(R.id.btnBack)
        tvChatName = findViewById(R.id.tvChatName)
        tvJobTitle = findViewById(R.id.tvJobTitle)
        emptyState = findViewById(R.id.emptyState)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun loadCurrentUserInfo() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .get()
                    .await()

                currentUserName = buildString {
                    append(userDoc.getString("firstName") ?: "")
                    val lastName = userDoc.getString("lastName") ?: ""
                    if (lastName.isNotEmpty()) {
                        append(" $lastName")
                    }
                }.ifEmpty { "User" }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading user info", e)
                currentUserName = "User"
            }
        }
    }

    private fun createConversation(
        otherUserId: String,
        jobId: String,
        jobTitle: String,
        applicationId: String,
        otherUserName: String
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            // Determine who is job seeker and who is employer
            val currentUserDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .get()
                .await()

            val currentAccountType = currentUserDoc.getString("accountType") ?: ""

            val (jobSeekerId, employerId, jobSeekerName, employerName) = if (currentAccountType == "jobseeker") {
                listOf(currentUserId, otherUserId, currentUserName, otherUserName)
            } else {
                listOf(otherUserId, currentUserId, otherUserName, currentUserName)
            }

            val result = messagingRepository.getOrCreateConversation(
                jobSeekerId = jobSeekerId,
                employerId = employerId,
                jobId = jobId,
                jobTitle = jobTitle,
                applicationId = applicationId,
                jobSeekerName = jobSeekerName,
                employerName = employerName
            )

            withContext(Dispatchers.Main) {
                result.onSuccess { convId ->
                    conversationId = convId
                    loadConversation()
                }.onFailure { error ->
                    Toast.makeText(
                        this@ChatActivity,
                        "Error creating conversation: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error creating conversation", error)
                }
            }
        }
    }

    private fun loadConversation() {
        if (conversationId.isEmpty()) return

        // Listen for messages in real-time
        messagesListener = messagingRepository.getMessages(conversationId) { messages ->
            if (messages.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                rvMessages.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                rvMessages.visibility = View.VISIBLE
                messageAdapter.updateMessages(messages)

                // Scroll to bottom
                rvMessages.post {
                    rvMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        // Mark messages as read
        CoroutineScope(Dispatchers.IO).launch {
            messagingRepository.markMessagesAsRead(conversationId)
        }
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        if (conversationId.isEmpty()) {
            Toast.makeText(this, "Conversation not ready", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear input immediately
        etMessage.text.clear()

        // Send message
        CoroutineScope(Dispatchers.IO).launch {
            val result = messagingRepository.sendMessage(
                conversationId = conversationId,
                text = messageText,
                senderName = currentUserName
            )

            withContext(Dispatchers.Main) {
                result.onFailure { error ->
                    Toast.makeText(
                        this@ChatActivity,
                        "Failed to send message: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error sending message", error)
                    // Restore the message text
                    etMessage.setText(messageText)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
    }
}

