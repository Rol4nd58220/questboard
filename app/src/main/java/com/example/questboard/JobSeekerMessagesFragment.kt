package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questboard.adapters.ConversationAdapter
import com.example.questboard.models.Conversation
import com.example.questboard.repository.MessagingRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Job Seeker Messages Fragment
 * Displays: Conversations with employers, chat history, notifications
 */
class JobSeekerMessagesFragment : Fragment() {

    private lateinit var rvConversations: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var etSearchMessages: EditText
    private lateinit var fabAddMessage: FloatingActionButton

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var messagingRepository: MessagingRepository
    private var conversationsListener: ListenerRegistration? = null

    private var allConversations = listOf<Conversation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobseeker_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()

        messagingRepository = MessagingRepository()

        loadConversations()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        rvConversations = view.findViewById(R.id.rvConversations)
        emptyState = view.findViewById(R.id.emptyState)
        etSearchMessages = view.findViewById(R.id.etSearchMessages)
        fabAddMessage = view.findViewById(R.id.fabAddMessage)
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            onConversationClick = { conversation ->
                openChat(conversation)
            },
            onConversationLongClick = { conversation ->
                showDeleteDialog(conversation)
            }
        )

        rvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }

    private fun setupListeners() {
        // Search functionality
        etSearchMessages.setOnEditorActionListener { _, _, _ ->
            val query = etSearchMessages.text.toString()
            searchConversations(query)
            false
        }

        // FAB click listener - Show info that conversations are created when applying
        fabAddMessage.setOnClickListener {
            Toast.makeText(
                context,
                "Conversations are created when you apply for jobs and message employers",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadConversations() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            android.util.Log.e("JobSeekerMessages", "No current user")
            showEmptyState()
            return
        }

        android.util.Log.d("JobSeekerMessages", "Loading conversations for user: $currentUserId")

        // Listen for conversations in real-time
        conversationsListener = messagingRepository.getUserConversations(currentUserId) { conversations ->
            android.util.Log.d("JobSeekerMessages", "Received ${conversations.size} conversations")
            conversations.forEachIndexed { index, conv ->
                android.util.Log.d("JobSeekerMessages", "Conversation $index: ${conv.conversationId}, participants: ${conv.participants}, jobTitle: ${conv.jobTitle}")
            }

            allConversations = conversations

            if (conversations.isEmpty()) {
                showEmptyState()
            } else {
                showConversations()
                conversationAdapter.updateConversations(conversations)
            }
        }
    }

    private fun searchConversations(query: String) {
        if (query.isEmpty()) {
            conversationAdapter.updateConversations(allConversations)
            return
        }

        val filtered = allConversations.filter { conv ->
            conv.jobTitle.contains(query, ignoreCase = true) ||
            conv.participantDetails.values.any { it.name.contains(query, ignoreCase = true) } ||
            conv.lastMessage.contains(query, ignoreCase = true)
        }

        conversationAdapter.updateConversations(filtered)

        if (filtered.isEmpty()) {
            Toast.makeText(context, "No conversations found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openChat(conversation: Conversation) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Get other participant info
        val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId }
        val otherParticipant = otherParticipantId?.let { conversation.participantDetails[it] }

        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.conversationId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, otherParticipant?.name ?: "Employer")
            putExtra(ChatActivity.EXTRA_JOB_TITLE, conversation.jobTitle)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(conversation: Conversation) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId }
        val otherParticipant = otherParticipantId?.let { conversation.participantDetails[it] }
        val otherName = otherParticipant?.name ?: "this employer"

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Conversation")
            .setMessage("Are you sure you want to delete this conversation with $otherName?\n\nThis will permanently delete all messages for both you and $otherName.")
            .setPositiveButton("Delete") { _, _ ->
                deleteConversation(conversation)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteConversation(conversation: Conversation) {
        android.util.Log.d("JobSeekerMessages", "Deleting conversation: ${conversation.conversationId}")

        CoroutineScope(Dispatchers.IO).launch {
            val result = messagingRepository.deleteConversation(conversation.conversationId)

            withContext(Dispatchers.Main) {
                result.onSuccess {
                    Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
                    android.util.Log.d("JobSeekerMessages", "Conversation deleted successfully")
                }.onFailure { error ->
                    Toast.makeText(context, "Failed to delete: ${error.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("JobSeekerMessages", "Error deleting conversation", error)
                }
            }
        }
    }

    private fun showEmptyState() {
        rvConversations.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }

    private fun showConversations() {
        rvConversations.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Reload conversations when fragment becomes visible
        // This ensures conversations appear even after navigating away and back
        android.util.Log.d("JobSeekerMessages", "onResume called - fragment is now visible")
        if (conversationsListener == null) {
            android.util.Log.d("JobSeekerMessages", "Listener was null, reloading conversations")
            loadConversations()
        }
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d("JobSeekerMessages", "onPause called - fragment is pausing")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        android.util.Log.d("JobSeekerMessages", "onDestroyView called - removing listener")
        conversationsListener?.remove()
        conversationsListener = null
    }
}
