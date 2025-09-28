package com.fasalsaathi.app.ui.community

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.base.BaseBottomNavigationActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DiscussionForumsActivity : BaseBottomNavigationActivity() {

    override fun getCurrentNavItemId(): Int {
        return R.id.nav_community
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discussion_forums)

        setupToolbar()
        setupBottomNavigation()
        setupFab()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Discussion Forums"
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fab_new_discussion)?.setOnClickListener {
            // TODO: Implement new discussion creation
            android.widget.Toast.makeText(this, "Create new discussion", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}