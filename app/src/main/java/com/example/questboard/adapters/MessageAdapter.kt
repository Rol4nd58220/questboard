package com.example.questboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.questboard.R
import com.example.questboard.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<Message>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_SYSTEM = 3

        private fun formatTime(date: java.util.Date): String {
            val now = System.currentTimeMillis()
            val diff = now - date.time

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
                else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.type == "system" -> VIEW_TYPE_SYSTEM
            message.senderId == currentUserId -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_system, parent, false)
                SystemMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // ViewHolder for sent messages
    inner class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessageText: TextView = view.findViewById(R.id.tvMessageText)
        private val tvMessageTime: TextView = view.findViewById(R.id.tvMessageTime)

        fun bind(message: Message) {
            tvMessageText.text = message.text
            tvMessageTime.text = formatTime(message.timestamp.toDate())
        }
    }

    // ViewHolder for received messages
    inner class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSenderName: TextView = view.findViewById(R.id.tvSenderName)
        private val tvMessageText: TextView = view.findViewById(R.id.tvMessageText)
        private val tvMessageTime: TextView = view.findViewById(R.id.tvMessageTime)

        fun bind(message: Message) {
            tvSenderName.text = message.senderName
            tvMessageText.text = message.text
            tvMessageTime.text = formatTime(message.timestamp.toDate())
        }
    }

    // ViewHolder for system messages
    inner class SystemMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSystemMessage: TextView = view.findViewById(R.id.tvSystemMessage)

        fun bind(message: Message) {
            tvSystemMessage.text = message.text
        }
    }
}

