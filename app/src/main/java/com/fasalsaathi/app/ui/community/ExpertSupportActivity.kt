package com.fasalsaathi.app.ui.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.base.BaseBottomNavigationActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpertSupportActivity : BaseBottomNavigationActivity() {

    override fun getCurrentNavItemId(): Int {
        return R.id.nav_community
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_support)

        setupToolbar()
        setupBottomNavigation()
        setupServiceCards()
        setupExpertButtons()
        setupFab()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Expert Support"
    }

    private fun setupServiceCards() {
        // Basic setup - UI elements will be added later
        Toast.makeText(this, "Expert Support loaded", Toast.LENGTH_SHORT).show()
    }

    private fun setupExpertButtons() {
        // Expert buttons will be added later
    }

    private fun setupFab() {
        // FAB will be added later
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}