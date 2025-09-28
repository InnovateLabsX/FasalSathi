package com.fasalsaathi.app.ui.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class ForumTopic(
    val id: Int,
    val title: String,
    val description: String,
    val author: String,
    val category: String,
    val replies: Int,
    val lastActivity: String,
    val isPopular: Boolean = false
)

class DiscussionForumsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var forumAdapter: ForumAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discussion_forums)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        loadForumTopics()
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
            title = "Discussion Forums"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewForums)
        forumAdapter = ForumAdapter { topic ->
            // Handle topic click
            Toast.makeText(this, "Opening topic: ${topic.title}", Toast.LENGTH_SHORT).show()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscussionForumsActivity)
            adapter = forumAdapter
        }
    }

    private fun setupFab() {
        fab = findViewById(R.id.fabNewTopic)
        fab.setOnClickListener {
            // Handle new topic creation
            Toast.makeText(this, "Create new topic - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadForumTopics() {
        val sampleTopics = listOf(
            ForumTopic(
                1,
                "Best practices for organic wheat farming",
                "Looking for advice on transitioning from conventional to organic wheat farming. What are the key considerations?",
                "FarmerRaj",
                "Organic Farming",
                24,
                "2 hours ago",
                true
            ),
            ForumTopic(
                2,
                "Dealing with pest control in tomato crops",
                "My tomato plants are being affected by whiteflies. Any natural remedies that work?",
                "TomatoGrower",
                "Pest Management",
                18,
                "5 hours ago"
            ),
            ForumTopic(
                3,
                "Water management during drought season",
                "Sharing tips on how to conserve water and maintain crop yield during dry spells.",
                "WaterWise",
                "Water Management",
                31,
                "1 day ago",
                true
            ),
            ForumTopic(
                4,
                "Soil testing and nutrient management",
                "When should we test soil and what parameters are most important for different crops?",
                "SoilExpert",
                "Soil Management",
                15,
                "2 days ago"
            ),
            ForumTopic(
                5,
                "Successful crop rotation strategies",
                "Discussion on 3-4 year crop rotation cycles that have worked well in different regions.",
                "CropRotator",
                "Crop Planning",
                22,
                "3 days ago"
            )
        )
        
        forumAdapter.updateTopics(sampleTopics)
    }
}