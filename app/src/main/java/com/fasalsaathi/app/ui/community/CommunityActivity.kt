package com.fasalsaathi.app.ui.community

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.base.BaseBottomNavigationActivity
import com.google.android.material.card.MaterialCardView

class CommunityActivity : BaseBottomNavigationActivity() {

    override fun getCurrentNavItemId(): Int {
        return R.id.nav_community
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        setupToolbar()
        setupBottomNavigation()
        setupFeatureCards()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Community Hub"
        }
    }

    private fun setupFeatureCards() {
        // Discussion Forums Card
        findViewById<MaterialCardView>(R.id.card_discussion_forums)?.setOnClickListener {
            startActivity(Intent(this, DiscussionForumsActivity::class.java))
        }

        // Knowledge Sharing Card  
        findViewById<MaterialCardView>(R.id.card_knowledge_base)?.setOnClickListener {
            startActivity(Intent(this, KnowledgeSharingActivity::class.java))
        }

        // Expert Support Card
        findViewById<MaterialCardView>(R.id.card_expert_support)?.setOnClickListener {
            startActivity(Intent(this, ExpertSupportActivity::class.java))
        }

        // Market Updates Card
        findViewById<MaterialCardView>(R.id.card_market_updates)?.setOnClickListener {
            startActivity(Intent(this, MarketUpdatesActivity::class.java))
        }
    }
}
