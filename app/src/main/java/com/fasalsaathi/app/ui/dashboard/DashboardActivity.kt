package com.fasalsaathi.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.R
import com.google.android.material.navigation.NavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.cardview.widget.CardView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.fasalsaathi.app.data.api.WeatherService
import com.fasalsaathi.app.utils.LanguageManager
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var weatherService: WeatherService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        
        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        weatherService = WeatherService()
        
        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()
        loadUserData()
        loadWeatherData()
    }
    
    private fun setupToolbar() {
        try {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        } catch (e: Exception) {
            // Fallback: use toolbar without setting as action bar
            toolbar.setNavigationOnClickListener {
                drawerLayout.openDrawer(navigationView)
            }
        }
    }
    
    private fun setupNavigationDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.app_name, // Using app_name as placeholder
            R.string.app_name
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
    }
    
    private fun setupClickListeners() {
        // Quick action cards
        findViewById<CardView>(R.id.cardCropRecommendation).setOnClickListener {
            // TODO: Navigate to Crop Recommendation
        }
        
        findViewById<CardView>(R.id.cardDiseaseDetection).setOnClickListener {
            // TODO: Navigate to Disease Detection
        }
        
        findViewById<CardView>(R.id.cardFaq).setOnClickListener {
            startActivity(Intent(this, com.fasalsaathi.app.ui.faq.FaqActivity::class.java))
        }
        
        findViewById<CardView>(R.id.cardWeather).setOnClickListener {
            showDetailedWeather()
        }
        
        // FAB for AI chat
        findViewById<FloatingActionButton>(R.id.fabChat).setOnClickListener {
            startActivity(Intent(this, com.fasalsaathi.app.ui.faq.FaqActivity::class.java))
        }
        
        // Language button click
        findViewById<MaterialButton>(R.id.btnLanguage).setOnClickListener {
            showLanguageSelectionDialog()
        }
    }
    
    private fun loadUserData() {
        // Load user data from SharedPreferences
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val userName = app.sharedPreferences.getString("user_name", "Farmer")
        val userEmail = app.sharedPreferences.getString("user_email", "")
        
        findViewById<TextView>(R.id.tvUserName).text = "Hello, $userName!"
        
        // Update language button
        updateLanguageButton()
        
        // Update navigation header with user info
        val headerView = navigationView.getHeaderView(0)
        val navUserName = headerView.findViewById<TextView>(R.id.tvNavUserName)
        val navUserEmail = headerView.findViewById<TextView>(R.id.tvNavUserEmail)
        navUserName?.text = userName
        navUserEmail?.text = userEmail
    }
    
    private fun loadWeatherData() {
        // Show loading state
        findViewById<TextView>(R.id.tvTemperature).text = "Loading..."
        findViewById<TextView>(R.id.tvWeatherDesc).text = "Fetching weather..."
        
        // Get user's location from preferences
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val userCity = app.sharedPreferences.getString("user_city", null)
        val userState = app.sharedPreferences.getString("user_state", "Delhi")
        
        lifecycleScope.launch {
            try {
                // Debug logging
                val hasCity = userCity != null && userCity != "Select City"
                val cityLat = app.sharedPreferences.getFloat("user_city_lat", 0f)
                val cityLon = app.sharedPreferences.getFloat("user_city_lon", 0f)
                println("Weather Debug - City: $userCity, State: $userState, Lat: $cityLat, Lon: $cityLon")
                
                // Use the new weather method that utilizes saved city coordinates
                val weatherData = weatherService.getCurrentWeatherForUser(app.sharedPreferences)
                
                if (weatherData != null) {
                    // Update UI with weather data - show city name instead of district
                    findViewById<TextView>(R.id.tvTemperature).text = "${weatherData.temperature.toInt()}${weatherData.temperatureUnit}"
                    findViewById<TextView>(R.id.tvWeatherDesc).text = "${weatherData.icon} ${weatherData.condition}"
                    
                    // Update weather card with more details
                    updateWeatherCard(weatherData)
                    
                    println("Weather loaded successfully for ${weatherData.location}")
                } else {
                    println("Weather data returned null, using fallback")
                    // Fallback weather data
                    findViewById<TextView>(R.id.tvTemperature).text = "28°C"
                    findViewById<TextView>(R.id.tvWeatherDesc).text = "🌤️ Partly Cloudy"
                }
            } catch (e: Exception) {
                println("Weather loading error: ${e.message}")
                e.printStackTrace()
                // Handle error - show default weather
                findViewById<TextView>(R.id.tvTemperature).text = "28°C"
                findViewById<TextView>(R.id.tvWeatherDesc).text = "🌤️ Weather Unavailable"
            }
        }
    }
    
    private fun updateWeatherCard(weather: WeatherService.WeatherData) {
        // Update additional weather details if TextViews exist in the layout
        updateWeatherDetailsIfExists(weather)
        
        // Update farming recommendations based on weather
        updateFarmingRecommendations(weather)
    }
    
    private fun updateWeatherDetailsIfExists(weather: WeatherService.WeatherData) {
        // For now, we'll just update the main weather display
        // Additional weather details can be added to the layout later
        
        // Update the weather description to include more details
        val weatherDesc = "${weather.icon} ${weather.condition} • ${weather.humidity}% humidity"
        findViewById<TextView>(R.id.tvWeatherDesc).text = weatherDesc
    }
    
    private fun updateFarmingRecommendations(weather: WeatherService.WeatherData) {
        // Generate farming recommendation based on weather
        val recommendationText = when {
            weather.condition.contains("rain", ignoreCase = true) -> 
                "🌧️ Good time for planting rice and other water-loving crops"
            weather.temperature > 35 -> 
                "🌡️ Very hot weather - ensure adequate irrigation for crops"
            weather.temperature < 15 -> 
                "❄️ Cool weather - good for wheat and winter crops"
            weather.humidity > 80 -> 
                "💧 High humidity - watch for fungal diseases in crops"
            weather.condition.contains("clear", ignoreCase = true) -> 
                "☀️ Perfect weather for harvesting and field work"
            else -> 
                "🌾 Monitor your crops and adjust watering as needed"
        }
        
        // For now, we'll just log this recommendation or display it as a toast
        // Later we can add a dedicated TextView for farming tips
        android.util.Log.d("FarmingTip", recommendationText)
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                // Already on dashboard
            }
            R.id.nav_crop_recommendation -> {
                // TODO: Navigate to crop recommendation
            }
            R.id.nav_disease_detection -> {
                // TODO: Navigate to disease detection
            }
            R.id.nav_market_prices -> {
                // TODO: Navigate to market prices
            }
            R.id.nav_weather -> {
                showDetailedWeather()
            }
            R.id.nav_profile -> {
                startActivity(Intent(this, com.fasalsaathi.app.ui.profile.ProfileActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, com.fasalsaathi.app.ui.profile.SettingsActivity::class.java))
            }
            R.id.nav_faq -> {
                startActivity(Intent(this, com.fasalsaathi.app.ui.faq.FaqActivity::class.java))
            }
            R.id.nav_logout -> {
                showLogoutConfirmation()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        
        // Update language menu item text based on current language
        val languageMenuItem = menu.findItem(R.id.action_language)
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val currentLanguage = app.sharedPreferences.getString("app_language", "english") ?: "english"
        
        val languageText = when (currentLanguage) {
            "english" -> "🇺🇸 EN"
            "hindi" -> "🇮🇳 हिं"
            "hinglish" -> "🇮🇳 Hi"
            else -> "🇺🇸 EN"
        }
        languageMenuItem?.title = languageText
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language -> {
                showLanguageSelectionDialog()
                true
            }
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "हिंदी (Hindi)", "Hinglish")
        val languageCodes = arrayOf("english", "hindi", "hinglish")
        val languageFlags = arrayOf("🇺🇸", "🇮🇳", "🇮🇳")
        
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val currentLanguage = app.sharedPreferences.getString("app_language", "english")
        val currentIndex = languageCodes.indexOf(currentLanguage)
        
        // Create display options with flags
        val displayOptions = languages.mapIndexed { index, language ->
            "${languageFlags[index]} $language"
        }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Select Language / भाषा चुनें")
            .setSingleChoiceItems(displayOptions, currentIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]
                changeAppLanguage(selectedLanguage, languages[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun changeAppLanguage(languageCode: String, languageName: String) {
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        editor.putString("app_language", languageCode)
        editor.apply()
        
        // Apply language change using LanguageManager
        val languageManager = LanguageManager(this)
        languageManager.setLocale(languageCode)
        
        // Show confirmation toast
        Toast.makeText(this, "Language changed to $languageName", Toast.LENGTH_SHORT).show()
        
        // Refresh the toolbar menu to show new language
        invalidateOptionsMenu()
        
        // Update language button
        updateLanguageButton()
        
        // Update dashboard content with new language
        updateDashboardContent()
    }
    
    private fun updateLanguageButton() {
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val currentLanguage = app.sharedPreferences.getString("app_language", "english") ?: "english"
        
        val languageButton = findViewById<MaterialButton>(R.id.btnLanguage)
        val buttonText = when (currentLanguage) {
            "english" -> "🇺🇸 EN"
            "hindi" -> "🇮🇳 हिं"
            "hinglish" -> "🇮🇳 Hi"
            else -> "🇺🇸 EN"
        }
        languageButton?.text = buttonText
    }
    
    private fun updateDashboardContent() {
        // Refresh user data and weather info with new language
        loadUserData()
        
        // Update navigation header
        val headerView = navigationView.getHeaderView(0)
        val navUserName = headerView.findViewById<TextView>(R.id.tvNavUserName)
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val userName = app.sharedPreferences.getString("user_name", "Farmer") ?: "Farmer"
        navUserName?.text = userName
        
        // Update toolbar title if needed
        supportActionBar?.title = getString(R.string.dashboard)
    }

    private fun showDetailedWeather() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_weather_details, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        // Set dialog background to be transparent for rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Get weather data and populate dialog
        loadDetailedWeatherData(dialogView, dialog)
        
        // Set up button listeners
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRefreshWeather).setOnClickListener {
            loadDetailedWeatherData(dialogView, dialog)
        }
        
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCloseWeather).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun loadDetailedWeatherData(dialogView: android.view.View, dialog: AlertDialog? = null) {
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        
        // Show loading state
        dialogView.findViewById<TextView>(R.id.tvWeatherLocation).text = "Loading..."
        dialogView.findViewById<TextView>(R.id.tvWeatherCondition).text = "Fetching weather data..."
        
        lifecycleScope.launch {
            try {
                val weatherData = weatherService.getCurrentWeatherForUser(app.sharedPreferences)
                
                if (weatherData != null) {
                    // Update all UI elements
                    dialogView.findViewById<TextView>(R.id.tvWeatherIcon).text = weatherData.icon
                    dialogView.findViewById<TextView>(R.id.tvWeatherLocation).text = weatherData.location
                    dialogView.findViewById<TextView>(R.id.tvWeatherCondition).text = weatherData.condition
                    dialogView.findViewById<TextView>(R.id.tvWeatherTemp).text = "${weatherData.temperature.toInt()}${weatherData.temperatureUnit}"
                    
                    dialogView.findViewById<TextView>(R.id.tvHumidity).text = "${weatherData.humidity}%"
                    dialogView.findViewById<TextView>(R.id.tvWindSpeed).text = "${weatherData.windSpeed.toInt()} km/h ${weatherData.windDirection}"
                    dialogView.findViewById<TextView>(R.id.tvFeelsLike).text = "${weatherData.feelsLike.toInt()}°C"
                    
                    val uvLevel = when (weatherData.uvIndex) {
                        in 0..2 -> "Low"
                        in 3..5 -> "Moderate"
                        in 6..7 -> "High"
                        in 8..10 -> "Very High"
                        else -> "Extreme"
                    }
                    dialogView.findViewById<TextView>(R.id.tvUvIndex).text = "${weatherData.uvIndex} ($uvLevel)"
                    
                    dialogView.findViewById<TextView>(R.id.tvPressure).text = "${weatherData.pressure.toInt()} hPa"
                    dialogView.findViewById<TextView>(R.id.tvVisibility).text = "${weatherData.visibility.toInt()} km"
                    
                    // Generate farming recommendation
                    val recommendation = generateFarmingRecommendation(weatherData)
                    dialogView.findViewById<TextView>(R.id.tvFarmingRecommendation).text = recommendation
                } else {
                    // This should never happen now, but as a safety net
                    println("WeatherData was null - this shouldn't happen")
                    // Try to get a simple fallback directly
                    val fallbackWeather = generateFallbackWeatherData()
                    dialogView.findViewById<TextView>(R.id.tvWeatherIcon).text = "🌤️"
                    dialogView.findViewById<TextView>(R.id.tvWeatherLocation).text = fallbackWeather.location
                    dialogView.findViewById<TextView>(R.id.tvWeatherCondition).text = fallbackWeather.condition
                    dialogView.findViewById<TextView>(R.id.tvWeatherTemp).text = "${fallbackWeather.temperature.toInt()}°C"
                    dialogView.findViewById<TextView>(R.id.tvHumidity).text = "${fallbackWeather.humidity}%"
                    dialogView.findViewById<TextView>(R.id.tvWindSpeed).text = "12 km/h NW"
                    dialogView.findViewById<TextView>(R.id.tvFeelsLike).text = "28°C"
                    dialogView.findViewById<TextView>(R.id.tvUvIndex).text = "5 (Moderate)"
                    dialogView.findViewById<TextView>(R.id.tvPressure).text = "1013 hPa"
                    dialogView.findViewById<TextView>(R.id.tvVisibility).text = "10 km"
                    dialogView.findViewById<TextView>(R.id.tvFarmingRecommendation).text = "Good weather for general farming activities."
                }
            } catch (e: Exception) {
                println("Dashboard weather error: ${e.message}")
                e.printStackTrace()
                // Emergency fallback
                dialogView.findViewById<TextView>(R.id.tvWeatherIcon).text = "🌤️"
                dialogView.findViewById<TextView>(R.id.tvWeatherLocation).text = "Delhi, India"
                dialogView.findViewById<TextView>(R.id.tvWeatherCondition).text = "Partly Cloudy"
                dialogView.findViewById<TextView>(R.id.tvWeatherTemp).text = "25°C"
                dialogView.findViewById<TextView>(R.id.tvHumidity).text = "65%"
                dialogView.findViewById<TextView>(R.id.tvWindSpeed).text = "12 km/h NW"
                dialogView.findViewById<TextView>(R.id.tvFeelsLike).text = "27°C"
                dialogView.findViewById<TextView>(R.id.tvUvIndex).text = "5 (Moderate)"
                dialogView.findViewById<TextView>(R.id.tvPressure).text = "1013 hPa"
                dialogView.findViewById<TextView>(R.id.tvVisibility).text = "10 km"
                dialogView.findViewById<TextView>(R.id.tvFarmingRecommendation).text = "Moderate conditions suitable for farming activities."
            }
        }
    }
    
    private fun generateFallbackWeatherData(): WeatherService.WeatherData {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val temp = when (hour) {
            in 6..9 -> 22 // Morning
            in 10..15 -> 28 // Day
            in 16..18 -> 25 // Evening
            else -> 20 // Night
        }
        
        return WeatherService.WeatherData(
            location = "Delhi, India",
            temperature = temp.toDouble(),
            temperatureUnit = "°C",
            condition = "Partly Cloudy",
            humidity = 65,
            windSpeed = 12.0,
            windDirection = "NW",
            pressure = 1013.25,
            visibility = 10.0,
            uvIndex = 5,
            feelsLike = temp + 2.0,
            icon = "🌤️"
        )
    }
    
    private fun generateFarmingRecommendation(weather: WeatherService.WeatherData): String {
        val temp = weather.temperature
        val humidity = weather.humidity
        val windSpeed = weather.windSpeed
        val uvIndex = weather.uvIndex
        
        return when {
            temp > 35 -> "🌡️ Very hot weather. Ensure adequate irrigation and avoid field work during peak hours (11 AM - 3 PM)."
            temp < 10 -> "❄️ Cold weather. Protect sensitive crops from frost. Good time for harvesting winter crops."
            humidity > 80 -> "💧 High humidity. Monitor crops for fungal diseases. Ensure good ventilation in greenhouses."
            humidity < 30 -> "🌵 Low humidity. Increase irrigation frequency. Mulch around plants to retain moisture."
            windSpeed > 20 -> "💨 Strong winds. Secure tall crops and structures. Delay spraying operations."
            uvIndex > 7 -> "☀️ High UV levels. Good for crop growth but ensure workers have protection during midday."
            temp in 20.0..30.0 && humidity in 40..70 -> "✅ Ideal conditions for most farming activities. Good time for planting, harvesting, and field maintenance."
            else -> "🌾 Moderate conditions. Monitor crop needs and adjust irrigation based on soil moisture levels."
        }
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout from FasalSaathi?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        // Clear user session and navigate to login
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        editor.putBoolean("is_logged_in", false)
        editor.remove("user_email")
        // Keep user data but mark as logged out
        editor.apply()
        
        val intent = Intent(this, com.fasalsaathi.app.ui.auth.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}