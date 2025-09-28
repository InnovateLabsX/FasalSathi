package com.fasalsaathi.app.ui.profile

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.FasalSaathiApplication
import com.fasalsaathi.app.R
import com.fasalsaathi.app.utils.LanguageManager
import com.fasalsaathi.app.utils.ThemeManager
import com.google.android.material.card.MaterialCardView

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchWeatherAlerts: SwitchCompat
    private lateinit var switchPriceAlerts: SwitchCompat
    private lateinit var switchOfflineMode: SwitchCompat
    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var tvLanguage: TextView
    private lateinit var tvDataSync: TextView
    private lateinit var tvCacheSize: TextView
    
    private lateinit var cardLanguage: MaterialCardView
    private lateinit var cardDataSync: MaterialCardView
    private lateinit var cardClearCache: MaterialCardView
    private lateinit var cardAbout: MaterialCardView
    private lateinit var cardPrivacy: MaterialCardView
    
    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val app = newBase.applicationContext as FasalSaathiApplication
            val languageManager = app.languageManager
            val currentLanguage = languageManager.getCurrentLanguage()
            languageManager.setLocale(currentLanguage)
            super.attachBaseContext(newBase)
        } else {
            super.attachBaseContext(newBase)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initializeViews()
        setupToolbar()
        loadSettings()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        switchNotifications = findViewById(R.id.switchNotifications)
        switchWeatherAlerts = findViewById(R.id.switchWeatherAlerts)
        switchPriceAlerts = findViewById(R.id.switchPriceAlerts)
        switchOfflineMode = findViewById(R.id.switchOfflineMode)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        tvLanguage = findViewById(R.id.tvLanguage)
        tvDataSync = findViewById(R.id.tvDataSync)
        tvCacheSize = findViewById(R.id.tvCacheSize)
        
        cardLanguage = findViewById(R.id.cardLanguage)
        cardDataSync = findViewById(R.id.cardDataSync)
        cardClearCache = findViewById(R.id.cardClearCache)
        cardAbout = findViewById(R.id.cardAbout)
        cardPrivacy = findViewById(R.id.cardPrivacy)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Settings"
    }
    
    private fun loadSettings() {
        val app = application as FasalSaathiApplication
        val sharedPrefs = app.sharedPreferences
        val themeManager = app.themeManager
        
        // Load notification settings
        switchNotifications.isChecked = sharedPrefs.getBoolean("notifications_enabled", true)
        switchWeatherAlerts.isChecked = sharedPrefs.getBoolean("weather_alerts_enabled", true)
        switchPriceAlerts.isChecked = sharedPrefs.getBoolean("price_alerts_enabled", true)
        switchOfflineMode.isChecked = sharedPrefs.getBoolean("offline_mode_enabled", false)
        
        // Load dark mode setting from ThemeManager
        switchDarkMode.isChecked = themeManager.isDarkModeEnabled()
        
        // Load language preference
        val currentLanguage = sharedPrefs.getString("app_language", "english") ?: "english"
        tvLanguage.text = when (currentLanguage) {
            "english" -> "English"
            "hindi" -> "हिंदी (Hindi)"
            "hinglish" -> "Hinglish"
            else -> "English"
        }
        
        // Load sync status
        val lastSyncTime = sharedPrefs.getString("last_sync_time", "Never")
        tvDataSync.text = "Last synced: $lastSyncTime"
        
        // Calculate cache size (simulated)
        tvCacheSize.text = "Cache size: ${(Math.random() * 50 + 10).toInt()} MB"
    }
    
    private fun setupClickListeners() {
        val app = application as FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        
        // Notification switches
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("notifications_enabled", isChecked)
            editor.apply()
            
            // Enable/disable other notification switches based on main switch
            switchWeatherAlerts.isEnabled = isChecked
            switchPriceAlerts.isEnabled = isChecked
            
            if (!isChecked) {
                switchWeatherAlerts.isChecked = false
                switchPriceAlerts.isChecked = false
                editor.putBoolean("weather_alerts_enabled", false)
                editor.putBoolean("price_alerts_enabled", false)
                editor.apply()
            }
        }
        
        switchWeatherAlerts.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("weather_alerts_enabled", isChecked)
            editor.apply()
        }
        
        switchPriceAlerts.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("price_alerts_enabled", isChecked)
            editor.apply()
        }
        
        switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("offline_mode_enabled", isChecked)
            editor.apply()
            
            Toast.makeText(this, 
                if (isChecked) "Offline mode enabled" else "Offline mode disabled", 
                Toast.LENGTH_SHORT).show()
        }
        
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val app = application as FasalSaathiApplication
            val themeManager = app.themeManager
            
            // Set theme immediately
            val newTheme = if (isChecked) ThemeManager.THEME_DARK else ThemeManager.THEME_LIGHT
            themeManager.setTheme(newTheme)
            
            // Save preference for consistency
            editor.putBoolean("dark_mode_enabled", isChecked)
            editor.apply()
            
            // Show confirmation
            Toast.makeText(this, 
                if (isChecked) "Dark mode enabled" else "Light mode enabled", 
                Toast.LENGTH_SHORT).show()
        }
        
        // Language selection
        cardLanguage.setOnClickListener {
            showLanguageSelectionDialog()
        }
        
        // Data sync
        cardDataSync.setOnClickListener {
            performDataSync()
        }
        
        // Clear cache
        cardClearCache.setOnClickListener {
            showClearCacheDialog()
        }
        
        // About
        cardAbout.setOnClickListener {
            showAboutDialog()
        }
        
        // Privacy
        cardPrivacy.setOnClickListener {
            showPrivacyDialog()
        }
    }
    
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "हिंदी (Hindi)", "Hinglish")
        val languageCodes = arrayOf("english", "hindi", "hinglish")
        
        val app = application as FasalSaathiApplication
        val currentLanguage = app.sharedPreferences.getString("app_language", "english")
        val currentIndex = languageCodes.indexOf(currentLanguage)
        
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]
                val editor = app.sharedPreferences.edit()
                editor.putString("app_language", selectedLanguage)
                editor.apply()
                
                tvLanguage.text = languages[which]
                
                // Apply language change
                val languageManager = LanguageManager(this)
                languageManager.setLocale(selectedLanguage)
                
                Toast.makeText(this, "Language changed to ${languages[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                
                // Recreate activity to apply language changes immediately
                recreate()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performDataSync() {
        tvDataSync.text = "Syncing..."
        
        // Simulate sync process
        Handler(Looper.getMainLooper()).postDelayed({
            val app = application as FasalSaathiApplication
            val editor = app.sharedPreferences.edit()
            val currentTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            editor.putString("last_sync_time", currentTime)
            editor.apply()
            
            tvDataSync.text = "Last synced: $currentTime"
            Toast.makeText(this, "Data synced successfully", Toast.LENGTH_SHORT).show()
        }, 2000)
    }
    
    private fun showClearCacheDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will clear all cached data including offline crops data and images. Are you sure?")
            .setPositiveButton("Clear") { _, _ ->
                clearCache()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun clearCache() {
        // Simulate cache clearing
        Handler(Looper.getMainLooper()).postDelayed({
            tvCacheSize.text = "Cache size: 2 MB"
            Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
        }, 1000)
    }
    
    private fun showAboutDialog() {
        val message = """
            FasalSaathi v1.0
            
            Your Smart Farming Companion
            
            Developed for Indian farmers to provide:
            • AI-powered crop recommendations
            • Disease detection and treatment
            • Real-time weather updates
            • Market price information
            
            © 2025 FasalSaathi Team
            Made with ❤️ for Indian Agriculture
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("About FasalSaathi")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showPrivacyDialog() {
        val message = """
            Privacy & Data Policy
            
            Your privacy is important to us:
            
            • Personal data is stored locally on your device
            • We don't share your information with third parties
            • Location data is used only for weather and crop recommendations
            • Images are processed locally for disease detection
            
            For detailed privacy policy, visit:
            www.fasalsaathi.com/privacy
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}