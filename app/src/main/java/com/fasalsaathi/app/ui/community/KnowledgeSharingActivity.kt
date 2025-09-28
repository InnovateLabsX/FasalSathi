package com.fasalsaathi.app.ui.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class KnowledgeArticle(
    val id: Int,
    val title: String,
    val summary: String,
    val author: String,
    val category: String,
    val readTime: String,
    val difficulty: String,
    val likes: Int,
    val imageUrl: String? = null
)

class KnowledgeSharingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var knowledgeAdapter: KnowledgeAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge_sharing)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        loadKnowledgeArticles()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Knowledge Sharing"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewKnowledge)
        knowledgeAdapter = KnowledgeAdapter { article ->
            // Handle article click
            Toast.makeText(this, "Opening article: ${article.title}", Toast.LENGTH_SHORT).show()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@KnowledgeSharingActivity)
            adapter = knowledgeAdapter
        }
    }

    private fun setupFab() {
        fab = findViewById(R.id.fabNewArticle)
        fab.setOnClickListener {
            // Handle new article creation
            Toast.makeText(this, "Share your knowledge - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadKnowledgeArticles() {
        val sampleArticles = listOf(
            KnowledgeArticle(
                1,
                "Complete Guide to Soil Preparation",
                "Learn the essential steps for preparing your soil before planting. Covers testing, amendments, and optimal timing.",
                "Dr. Priya Sharma",
                "Soil Management",
                "8 min read",
                "Beginner",
                45
            ),
            KnowledgeArticle(
                2,
                "Integrated Pest Management Strategies",
                "Effective ways to control pests using natural methods combined with minimal chemical intervention.",
                "Farmer Mukesh",
                "Pest Control",
                "12 min read",
                "Intermediate",
                67
            ),
            KnowledgeArticle(
                3,
                "Water Conservation Techniques for Dry Regions",
                "Proven methods to maximize water efficiency and maintain crop yields even during drought conditions.",
                "Water Expert Ravi",
                "Water Management",
                "10 min read",
                "Advanced",
                89
            ),
            KnowledgeArticle(
                4,
                "Organic Fertilizer Preparation at Home",
                "Step-by-step guide to making nutrient-rich organic fertilizers using kitchen waste and farm materials.",
                "Organic Guru Sunita",
                "Organic Farming",
                "6 min read",
                "Beginner",
                34
            ),
            KnowledgeArticle(
                5,
                "Crop Rotation for Maximum Yield",
                "Understanding the science behind crop rotation and designing effective rotation schedules.",
                "Agri Scientist Rajesh",
                "Crop Planning",
                "15 min read",
                "Intermediate",
                56
            )
        )
        
        knowledgeAdapter.updateArticles(sampleArticles)
    }
}