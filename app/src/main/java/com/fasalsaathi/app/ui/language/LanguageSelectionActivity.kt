package com.fasalsaathi.app.ui.language

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.dashboard.DashboardActivity
import com.fasalsaathi.app.utils.LanguageManager
import com.fasalsaathi.app.FasalSaathiApplication
import com.google.android.material.button.MaterialButton

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var languageManager: LanguageManager
    private lateinit var app: FasalSaathiApplication
    private var selectedLanguageCode: String = ""
    private var selectedLanguageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)
        
        app = application as FasalSaathiApplication
        languageManager = LanguageManager(this)
        
        setupLanguageCards()
        setupAcceptButton()
    }

    private fun setupLanguageCards() {
        findViewById<CardView>(R.id.cardEnglish).setOnClickListener {
            highlightSelectedLanguage("en", "English", R.id.cardEnglish)
        }
        
        findViewById<CardView>(R.id.cardHindi).setOnClickListener {
            highlightSelectedLanguage("hi", "Hindi", R.id.cardHindi)
        }
        
        findViewById<CardView>(R.id.cardMarathi).setOnClickListener {
            highlightSelectedLanguage("mr", "Marathi", R.id.cardMarathi)
        }
        
        findViewById<CardView>(R.id.cardGujarati).setOnClickListener {
            highlightSelectedLanguage("gu", "Gujarati", R.id.cardGujarati)
        }
        
        findViewById<CardView>(R.id.cardUrdu).setOnClickListener {
            highlightSelectedLanguage("ur", "Urdu", R.id.cardUrdu)
        }
    }
    
    private fun setupAcceptButton() {
        val acceptButton = findViewById<MaterialButton>(R.id.btnAcceptLanguage)
        acceptButton.isEnabled = false // Initially disabled
        acceptButton.alpha = 0.5f
        
        acceptButton.setOnClickListener {
            if (selectedLanguageCode.isNotEmpty()) {
                applySelectedLanguage()
            } else {
                Toast.makeText(this, "Please select a language first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun highlightSelectedLanguage(languageCode: String, languageName: String, selectedCardId: Int) {
        // Store selected language
        selectedLanguageCode = languageCode
        selectedLanguageName = languageName
        
        // Reset all cards to default appearance
        val cardIds = listOf(R.id.cardEnglish, R.id.cardHindi, R.id.cardMarathi, R.id.cardGujarati, R.id.cardUrdu)
        cardIds.forEach { cardId ->
            findViewById<CardView>(cardId).apply {
                cardElevation = 8f
                alpha = if (cardId == selectedCardId) 1.0f else 0.7f
                scaleX = if (cardId == selectedCardId) 1.05f else 1.0f
                scaleY = if (cardId == selectedCardId) 1.05f else 1.0f
            }
        }
        
        // Enable accept button
        val acceptButton = findViewById<MaterialButton>(R.id.btnAcceptLanguage)
        acceptButton.isEnabled = true
        acceptButton.alpha = 1.0f
    }
    
    private fun applySelectedLanguage() {
        // Set the language using LanguageManager
        languageManager.setLanguage(selectedLanguageCode)
        
        // Save language preference
        app.sharedPreferences.edit()
            .putString("selected_language", selectedLanguageCode)
            .putString("selected_language_name", selectedLanguageName)
            .putBoolean("language_selected", true)
            .apply()
        
        // Show confirmation
        Toast.makeText(this, "Language set to $selectedLanguageName", Toast.LENGTH_SHORT).show()
        
        // Navigate to Dashboard
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        // Apply transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    override fun onBackPressed() {
        // Prevent going back to prevent user from skipping language selection
        // Do nothing or show exit confirmation
    }
}