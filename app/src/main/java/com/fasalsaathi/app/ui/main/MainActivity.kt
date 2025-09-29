package com.fasalsaathi.app.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fasalsaathi.app.FasalSaathiApplication
import com.fasalsaathi.app.ui.auth.LoginActivity
import com.fasalsaathi.app.ui.dashboard.DashboardActivity
import com.fasalsaathi.app.ui.language.LanguageSelectionActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as FasalSaathiApplication
        val isFirstTime = app.sharedPreferences.getBoolean("is_first_time", true)
        val isLoggedIn = app.sharedPreferences.getBoolean("is_logged_in", false)
        val languageSelected = app.sharedPreferences.getBoolean("language_selected", false)
        
        when {
            isFirstTime -> {
                // First time user - go to language selection first
                app.sharedPreferences.edit().putBoolean("is_first_time", false).apply()
                navigateToLanguageSelection()
            }
            !languageSelected -> {
                // Language not selected - go to language selection
                navigateToLanguageSelection()
            }
            !isLoggedIn -> {
                // User not logged in - go to login
                navigateToLogin()
            }
            else -> {
                // User logged in - go to dashboard
                navigateToDashboard()
            }
        }
    }
    
    private fun navigateToLanguageSelection() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}