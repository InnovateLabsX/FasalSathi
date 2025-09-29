package com.fasalsaathi.app.ui.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class ExpertConsultation(
    val id: Int,
    val expertName: String,
    val specialization: String,
    val experience: String,
    val rating: Float,
    val isAvailable: Boolean,
    val consultationFee: String,
    val languages: List<String>,
    val description: String
)

class ExpertSupportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var expertAdapter: ExpertAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_support)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        loadExperts()
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
            title = "Expert Support"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewExperts)
        expertAdapter = ExpertAdapter { expert ->
            Toast.makeText(this, "Booking consultation with ${expert.expertName}", Toast.LENGTH_SHORT).show()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpertSupportActivity)
            adapter = expertAdapter
        }
    }

    private fun setupFab() {
        fab = findViewById(R.id.fabAskQuestion)
        fab.setOnClickListener {
            Toast.makeText(this, "Ask a quick question - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExperts() {
        val sampleExperts = listOf(
            ExpertConsultation(
                1,
                "Dr. Ramesh Kumar",
                "Crop Diseases & Plant Pathology",
                "15+ years",
                4.8f,
                true,
                "₹500/hour",
                listOf("Hindi", "English"),
                "Specialist in crop disease diagnosis and organic treatment methods"
            ),
            ExpertConsultation(
                2,
                "Prof. Anjali Singh",
                "Soil Science & Nutrition",
                "20+ years",
                4.9f,
                false,
                "₹750/hour",
                listOf("Hindi", "English", "Marathi"),
                "Expert in soil health management and precision agriculture"
            ),
            ExpertConsultation(
                3,
                "Er. Suresh Patel",
                "Irrigation & Water Management",
                "12+ years",
                4.7f,
                true,
                "₹600/hour",
                listOf("Hindi", "Gujarati", "English"),
                "Specializes in drip irrigation and water conservation techniques"
            ),
            ExpertConsultation(
                4,
                "Dr. Meera Joshi",
                "Organic Farming & Sustainability",
                "18+ years",
                4.9f,
                true,
                "₹550/hour",
                listOf("Hindi", "English"),
                "Leading expert in organic farming and sustainable agriculture practices"
            )
        )
        
        expertAdapter.updateExperts(sampleExperts)
    }
}