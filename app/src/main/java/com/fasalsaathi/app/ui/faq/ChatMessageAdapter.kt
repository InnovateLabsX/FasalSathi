package com.fasalsaathi.app.ui.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R

class ChatMessageAdapter(
    private val messages: List<ChatMessage>,
    private val onMessageClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutUserMessage: LinearLayout = itemView.findViewById(R.id.layoutUserMessage)
        val layoutAiMessage: LinearLayout = itemView.findViewById(R.id.layoutAiMessage)
        val tvUserMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        val tvAiMessage: TextView = itemView.findViewById(R.id.tvAiMessage)
        val tvUserTimestamp: TextView = itemView.findViewById(R.id.tvUserTimestamp)
        val tvAiTimestamp: TextView = itemView.findViewById(R.id.tvAiTimestamp)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        if (message.isFromAi) {
            // Show AI message
            holder.layoutAiMessage.visibility = View.VISIBLE
            holder.layoutUserMessage.visibility = View.GONE
            
            holder.tvAiMessage.text = message.message
            holder.tvAiTimestamp.text = message.timestamp
            
            // Add click listener for AI messages (for text-to-speech)
            holder.layoutAiMessage.setOnClickListener {
                onMessageClick(message)
            }
            
        } else {
            // Show user message
            holder.layoutUserMessage.visibility = View.VISIBLE
            holder.layoutAiMessage.visibility = View.GONE
            
            holder.tvUserMessage.text = message.message
            holder.tvUserTimestamp.text = message.timestamp
            
            // Show image if attached
            if (message.image != null) {
                holder.ivUserImage.visibility = View.VISIBLE
                holder.ivUserImage.setImageBitmap(message.image)
            } else {
                holder.ivUserImage.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = messages.size
}