package com.example.questboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questboard.adapters.ConversationAdapter
import com.example.questboard.models.Conversation
import com.example.questboard.repository.MessagingRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

/**
 * Employer Messages Fragment
 * Displays: Conversations with job seekers, chat history
 */
class EmployerMessagesFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_employer_messages, container, false)
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
        conversationAdapter = ConversationAdapter { conversation ->
            openChat(conversation)
        }

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

        // FAB click listener
        fabAddMessage.setOnClickListener {
            Toast.makeText(
                context,
                "Conversations are created when job seekers apply for your jobs",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadConversations() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            showEmptyState()
            return
        }

        // Listen for conversations in real-time
        conversationsListener = messagingRepository.getUserConversations(currentUserId) { conversations ->
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
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, otherParticipant?.name ?: "Job Seeker")
            putExtra(ChatActivity.EXTRA_JOB_TITLE, conversation.jobTitle)
        }
        startActivity(intent)
    }

    private fun showEmptyState() {
        rvConversations.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }

    private fun showConversations() {
        rvConversations.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        conversationsListener?.remove()
    }
}
