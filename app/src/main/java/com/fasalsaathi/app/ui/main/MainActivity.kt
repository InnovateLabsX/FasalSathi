package com.fasalsaathi.app.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fasalsaathi.app.FasalSaathiApplication
import com.fasalsaathi.app.ui.auth.LoginActivity
import com.fasalsaathi.app.ui.dashboard.DashboardActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as FasalSaathiApplication
        val isFirstTime = app.sharedPreferences.getBoolean("is_first_time", true)
        val isLoggedIn = app.sharedPreferences.getBoolean("is_logged_in", false)
        
        when {
            isFirstTime -> {
                // First time user - go to login/signup
                app.sharedPreferences.edit().putBoolean("is_first_time", false).apply()
                navigateToLogin()
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