package com.fasalsaathi.app.ui.community

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView
import com.fasalsaathi.app.R

class CommunityActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)
        
        setupToolbar()
        setupFeatureCards()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Community"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupFeatureCards() {
        findViewById<MaterialCardView>(R.id.cardDiscussionForums).setOnClickListener {
            startActivity(Intent(this, DiscussionForumsActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.cardKnowledgeSharing).setOnClickListener {
            startActivity(Intent(this, KnowledgeSharingActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.cardExpertSupport).setOnClickListener {
            startActivity(Intent(this, ExpertSupportActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.cardMarketUpdates).setOnClickListener {
            startActivity(Intent(this, MarketUpdatesActivity::class.java))
        }
    }
}