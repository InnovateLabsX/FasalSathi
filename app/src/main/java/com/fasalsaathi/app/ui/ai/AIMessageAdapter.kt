package com.fasalsaathi.app.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.card.MaterialCardView

class AIMessageAdapter(
    private val messages: List<AIMessage>,
    private val onMessageClick: (AIMessage) -> Unit
) : RecyclerView.Adapter<AIMessageAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
        private const val VIEW_TYPE_SYSTEM = 3
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isSystemMessage -> VIEW_TYPE_SYSTEM
            message.isFromUser -> VIEW_TYPE_USER
            else -> VIEW_TYPE_AI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = when (viewType) {
            VIEW_TYPE_USER -> R.layout.item_message_user
            VIEW_TYPE_SYSTEM -> R.layout.item_message_system
            else -> R.layout.item_message_ai
        }
        
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val cardMessage: View = itemView.findViewById(R.id.cardMessage)

        fun bind(message: AIMessage) {
            tvMessage.text = message.content
            tvTimestamp.text = message.timestamp
            
            cardMessage.setOnClickListener {
                onMessageClick(message)
            }
            
            // Add subtle animation
            itemView.alpha = 0f
            itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }
}