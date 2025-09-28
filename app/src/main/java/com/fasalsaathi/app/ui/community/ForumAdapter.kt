package com.fasalsaathi.app.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.card.MaterialCardView

class ForumAdapter(
    private val onTopicClick: (ForumTopic) -> Unit
) : RecyclerView.Adapter<ForumAdapter.ForumViewHolder>() {

    private var topics = listOf<ForumTopic>()

    fun updateTopics(newTopics: List<ForumTopic>) {
        topics = newTopics
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forum_topic, parent, false)
        return ForumViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
        holder.bind(topics[position])
    }

    override fun getItemCount(): Int = topics.size

    inner class ForumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardForumTopic)
        private val titleText: TextView = itemView.findViewById(R.id.textTopicTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.textTopicDescription)
        private val authorText: TextView = itemView.findViewById(R.id.textAuthor)
        private val categoryText: TextView = itemView.findViewById(R.id.textCategory)
        private val repliesText: TextView = itemView.findViewById(R.id.textReplies)
        private val timeText: TextView = itemView.findViewById(R.id.textLastActivity)
        private val popularBadge: TextView = itemView.findViewById(R.id.textPopularBadge)

        fun bind(topic: ForumTopic) {
            titleText.text = topic.title
            descriptionText.text = topic.description
            authorText.text = "by ${topic.author}"
            categoryText.text = topic.category
            repliesText.text = "${topic.replies} replies"
            timeText.text = topic.lastActivity
            
            popularBadge.visibility = if (topic.isPopular) View.VISIBLE else View.GONE

            card.setOnClickListener {
                onTopicClick(topic)
            }
        }
    }
}