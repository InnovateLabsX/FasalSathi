package com.fasalsaathi.app.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.crops.CropRecommendationActivity
import com.fasalsaathi.app.ui.dashboard.DashboardActivity
import com.fasalsaathi.app.ui.community.CommunityActivity
import com.fasalsaathi.app.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Base activity that provides common bottom navigation functionality
 * All main activities should extend this to ensure consistent navigation behavior
 */
abstract class BaseBottomNavigationActivity : AppCompatActivity() {
    
    protected abstract fun getCurrentNavItemId(): Int
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    protected fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        // Set the current tab as selected
        bottomNavigation.selectedItemId = getCurrentNavItemId()
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (getCurrentNavItemId() != R.id.nav_home) {
                        navigateToHome()
                    }
                    true
                }
                R.id.nav_crops -> {
                    if (getCurrentNavItemId() != R.id.nav_crops) {
                        navigateToCrops()
                    }
                    true
                }
                R.id.nav_community -> {
                    if (getCurrentNavItemId() != R.id.nav_community) {
                        navigateToCommunity()
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (getCurrentNavItemId() != R.id.nav_profile) {
                        navigateToProfile()
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in_scale, R.anim.fade_out_scale)
        if (this.javaClass.simpleName != "DashboardActivity") {
            finish()
        }
    }
    
    private fun navigateToCrops() {
        val intent = Intent(this, CropRecommendationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    
    private fun navigateToCommunity() {
        val intent = Intent(this, CommunityActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}