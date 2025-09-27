package com.fasalsaathi.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.fasalsaathi.app.data.local.FasalSaathiDatabase
import com.fasalsaathi.app.data.repository.UserRepository
import com.fasalsaathi.app.utils.LanguageManager

class FasalSaathiApplication : Application() {
    
    companion object {
        lateinit var instance: FasalSaathiApplication
            private set
    }
    
    // Database
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            FasalSaathiDatabase::class.java,
            "fasalsaathi_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    // Shared Preferences
    val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("fasalsaathi_prefs", Context.MODE_PRIVATE)
    }
    
    // Repositories
    val userRepository by lazy {
        UserRepository(database.userDao(), sharedPreferences)
    }
    
    // Language Manager
    val languageManager by lazy {
        LanguageManager(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize language
        languageManager.setLocale(languageManager.getCurrentLanguage())
        
        // Initialize other components
        initializeApp()
    }
    
    private fun initializeApp() {
        // Initialize Firebase
        // Initialize notification channels
        // Initialize work manager for background sync
        // Initialize crash reporting
    }
    
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    fun getCurrentUserId(): String? {
        return sharedPreferences.getString("current_user_id", null)
    }
    
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("is_first_launch", true)
    }
    
    fun setFirstLaunchComplete() {
        sharedPreferences.edit()
            .putBoolean("is_first_launch", false)
            .apply()
    }
}