package com.fasalsaathi.app.ui.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.base.BaseBottomNavigationActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class KnowledgeSharingActivity : BaseBottomNavigationActivity() {

    override fun getCurrentNavItemId(): Int {
        return R.id.nav_community
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge_sharing)

        setupToolbar()
        setupBottomNavigation()
        setupFeatureCards()
        setupFab()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Knowledge Base"
    }

    private fun setupFeatureCards() {
        // Basic setup - UI elements will be added later
        Toast.makeText(this, "Knowledge Sharing loaded", Toast.LENGTH_SHORT).show()
    }

    private fun setupFab() {
        // FAB will be added later
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}