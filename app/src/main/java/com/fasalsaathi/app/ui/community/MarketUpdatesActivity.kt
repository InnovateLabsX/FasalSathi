package com.fasalsaathi.app.ui.community

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.base.BaseBottomNavigationActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MarketUpdatesActivity : BaseBottomNavigationActivity() {

    override fun getCurrentNavItemId(): Int {
        return R.id.nav_community
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_updates)

        setupToolbar()
        setupBottomNavigation()
        setupFab()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Market Updates"
    }

    private fun setupFab() {
        // FAB will be added later
        android.widget.Toast.makeText(this, "Market Updates loaded", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}