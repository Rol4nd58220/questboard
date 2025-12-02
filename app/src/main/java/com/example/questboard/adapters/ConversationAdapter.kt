package com.example.questboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.questboard.R
import com.example.questboard.models.Conversation
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit,
    private val onConversationLongClick: (Conversation) -> Unit = {}
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    private val conversations = mutableListOf<Conversation>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvUnreadBadge: TextView = view.findViewById(R.id.tvUnreadBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]

        // Get other participant's info
        val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId }
        val otherParticipant = otherParticipantId?.let { conversation.participantDetails[it] }

        // Set avatar (first letter of name)
        val name = otherParticipant?.name ?: "Unknown"
        holder.tvAvatar.text = name.firstOrNull()?.toString()?.uppercase() ?: "?"

        // Set name
        holder.tvName.text = name

        // Set job title
        holder.tvJobTitle.text = conversation.jobTitle

        // Set last message
        holder.tvLastMessage.text = conversation.lastMessage

        // Set time
        holder.tvTime.text = formatTime(conversation.lastMessageTime.toDate())

        // Set unread badge
        val unreadCount = conversation.unreadCount[currentUserId] ?: 0
        if (unreadCount > 0) {
            holder.tvUnreadBadge.visibility = View.VISIBLE
            holder.tvUnreadBadge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
        } else {
            holder.tvUnreadBadge.visibility = View.GONE
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onConversationClick(conversation)
        }

        // Long click listener for delete
        holder.itemView.setOnLongClickListener {
            onConversationLongClick(conversation)
            true
        }
    }

    override fun getItemCount() = conversations.size

    fun updateConversations(newConversations: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newConversations)
        notifyDataSetChanged()
    }

    fun getConversationAt(position: Int): Conversation {
        return conversations[position]
    }

    private fun formatTime(date: java.util.Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        }
    }
}

