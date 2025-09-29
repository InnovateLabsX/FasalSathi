package com.fasalsaathi.app.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.card.MaterialCardView

class KnowledgeAdapter(
    private val onArticleClick: (KnowledgeArticle) -> Unit
) : RecyclerView.Adapter<KnowledgeAdapter.KnowledgeViewHolder>() {

    private var articles = listOf<KnowledgeArticle>()

    fun updateArticles(newArticles: List<KnowledgeArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KnowledgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_knowledge_article, parent, false)
        return KnowledgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: KnowledgeViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    inner class KnowledgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardKnowledgeArticle)
        private val titleText: TextView = itemView.findViewById(R.id.textArticleTitle)
        private val summaryText: TextView = itemView.findViewById(R.id.textArticleSummary)
        private val authorText: TextView = itemView.findViewById(R.id.textArticleAuthor)
        private val categoryText: TextView = itemView.findViewById(R.id.textArticleCategory)
        private val readTimeText: TextView = itemView.findViewById(R.id.textReadTime)
        private val difficultyText: TextView = itemView.findViewById(R.id.textDifficulty)
        private val likesText: TextView = itemView.findViewById(R.id.textLikes)

        fun bind(article: KnowledgeArticle) {
            titleText.text = article.title
            summaryText.text = article.summary
            authorText.text = "by ${article.author}"
            categoryText.text = article.category
            readTimeText.text = article.readTime
            difficultyText.text = article.difficulty
            likesText.text = "${article.likes} likes"

            card.setOnClickListener {
                onArticleClick(article)
            }
        }
    }
}