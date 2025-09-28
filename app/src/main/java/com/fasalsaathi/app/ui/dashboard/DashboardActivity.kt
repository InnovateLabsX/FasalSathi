package com.fasalsaathi.app.ui.dashboard

import android.content.Context
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
import androidx.cardview.widget.CardView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.fasalsaathi.app.data.api.WeatherService
import com.fasalsaathi.app.utils.LanguageManager
import com.fasalsaathi.app.FasalSaathiApplication
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.fasalsaathi.app.ui.crops.CropRecommendationActivity
import com.fasalsaathi.app.ui.base.BaseBottomNavigationActivity
import com.fasalsaathi.app.utils.UXUtils
import com.fasalsaathi.app.utils.AccessibilityUtils

class DashboardActivity : BaseBottomNavigationActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var weatherService: WeatherService
    private var currentWeatherData: WeatherService.WeatherData? = null
    
    companion object {
        const val WEATHER_DATA_KEY = "cached_weather_data"
        const val WEATHER_TIMESTAMP_KEY = "weather_timestamp"
        const val WEATHER_CACHE_DURATION = 30 * 60 * 1000L // 30 minutes in milliseconds
    }
    
    override fun getCurrentNavItemId(): Int = R.id.nav_home
    
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
        setContentView(R.layout.activity_dashboard)
        
        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        weatherService = WeatherService()
        
        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()
        setupBottomNavigation()
        loadUserData()
        loadWeatherData()
        
        // Setup accessibility for better user experience
        AccessibilityUtils.setupViewGroupAccessibility(findViewById(R.id.drawerLayout), this)
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
        // Quick action cards with improved UX
        UXUtils.setUpInteractiveView(findViewById(R.id.cardCropRecommendation)) {
            UXUtils.showInfoMessage(this, "Opening Crop Recommendations...")
            startActivity(Intent(this, CropRecommendationActivity::class.java))
        }
        
        UXUtils.setUpInteractiveView(findViewById(R.id.cardDiseaseDetection)) {
            UXUtils.showInfoMessage(this, "Opening Disease Detection...")
            startActivity(Intent(this, com.fasalsaathi.app.ui.disease.DiseaseDetectionActivity::class.java))
        }
        
        UXUtils.setUpInteractiveView(findViewById(R.id.cardAIAssistant)) {
            UXUtils.showInfoMessage(this, "Connecting to AI Assistant...")
            startActivity(Intent(this, com.fasalsaathi.app.ui.ai.AIAssistantActivity::class.java))
        }
        
        UXUtils.setUpInteractiveView(findViewById(R.id.cardWeather)) {
            UXUtils.showInfoMessage(this, "Loading detailed weather...")
            showDetailedWeather()
        }
        
        // Today's Focus chips with enhanced feedback
        UXUtils.setUpInteractiveView(findViewById(R.id.chipSoilHealth)) {
            showSoilHealthInfo()
        }
        
        UXUtils.setUpInteractiveView(findViewById(R.id.chipPestWatch)) {
            showPestWatchInfo()
        }
        
        findViewById<Chip>(R.id.chipMarketRates).setOnClickListener {
            showMarketRatesInfo()
        }
        
        findViewById<Chip>(R.id.chipWaterUsage).setOnClickListener {
            showWaterUsageInfo()
        }
    }
    
    private fun loadUserData() {
        // Load user data from SharedPreferences
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val userName = app.sharedPreferences.getString("user_name", "Farmer")
        val userEmail = app.sharedPreferences.getString("user_email", "")
        
        findViewById<TextView>(R.id.tvUserName).text = "Hello, $userName!"
        
        // Update navigation header with user info
        val headerView = navigationView.getHeaderView(0)
        val navUserName = headerView.findViewById<TextView>(R.id.tvNavUserName)
        val navUserEmail = headerView.findViewById<TextView>(R.id.tvNavUserEmail)
        navUserName?.text = userName
        navUserEmail?.text = userEmail
    }
    
    private fun loadWeatherData() {
        // Show loading state with better UX
        findViewById<TextView>(R.id.tvTemperature).text = "..."
        findViewById<TextView>(R.id.tvWeatherDesc).text = "🔄 Loading weather..."
        
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
                    // Cache the weather data and timestamp
                    currentWeatherData = weatherData
                    app.sharedPreferences.edit()
                        .putLong(WEATHER_TIMESTAMP_KEY, System.currentTimeMillis())
                        .apply()
                    
                    // Update UI with weather data - show city name instead of district
                    findViewById<TextView>(R.id.tvTemperature).text = "${weatherData.temperature.toInt()}${weatherData.temperatureUnit}"
                    findViewById<TextView>(R.id.tvWeatherDesc).text = "${weatherData.icon} ${weatherData.condition}"
                    
                    // Update weather card with more details
                    updateWeatherCard(weatherData)
                    
                    println("Weather loaded successfully for ${weatherData.location}")
                    UXUtils.showSuccessMessage(this@DashboardActivity, "Weather updated successfully")
                } else {
                    println("Weather data returned null, using fallback")
                    // Create consistent fallback weather data
                    currentWeatherData = createFallbackWeatherData()
                    findViewById<TextView>(R.id.tvTemperature).text = "28°C"
                    findViewById<TextView>(R.id.tvWeatherDesc).text = "🌤️ Partly Cloudy"
                    UXUtils.showWarningMessage(this@DashboardActivity, "Using offline weather data")
                }
            } catch (e: Exception) {
                println("Weather loading error: ${e.message}")
                e.printStackTrace()
                // Handle error - create consistent fallback data
                currentWeatherData = createFallbackWeatherData()
                findViewById<TextView>(R.id.tvTemperature).text = "28°C"
                findViewById<TextView>(R.id.tvWeatherDesc).text = "🌤️ Weather Unavailable"
                UXUtils.showErrorMessage(this@DashboardActivity, "Failed to load weather data")
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
                startActivity(Intent(this, CropRecommendationActivity::class.java))
            }
            R.id.nav_disease_detection -> {
                startActivity(Intent(this, com.fasalsaathi.app.ui.disease.DiseaseDetectionActivity::class.java))
            }
            R.id.nav_market_prices -> {
                startActivity(Intent(this, com.fasalsaathi.app.ui.market.MarketActivity::class.java))
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
                startActivity(Intent(this, com.fasalsaathi.app.ui.ai.AIAssistantActivity::class.java))
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        // Navigate to the comprehensive weather activity with cached data
        val intent = Intent(this, com.fasalsaathi.app.ui.weather.WeatherActivity::class.java)
        
        // Pass current weather data if available and recent
        currentWeatherData?.let { weatherData ->
            val app = application as com.fasalsaathi.app.FasalSaathiApplication
            val lastUpdate = app.sharedPreferences.getLong(WEATHER_TIMESTAMP_KEY, 0L)
            val currentTime = System.currentTimeMillis()
            
            // Only pass data if it's less than 30 minutes old
            if (currentTime - lastUpdate < WEATHER_CACHE_DURATION) {
                intent.putExtra("temperature", weatherData.temperature)
                intent.putExtra("temperatureUnit", weatherData.temperatureUnit)
                intent.putExtra("condition", weatherData.condition)
                intent.putExtra("icon", weatherData.icon)
                intent.putExtra("location", weatherData.location)
                intent.putExtra("humidity", weatherData.humidity)
                intent.putExtra("windSpeed", weatherData.windSpeed)
                intent.putExtra("windDirection", weatherData.windDirection)
                intent.putExtra("pressure", weatherData.pressure)
                intent.putExtra("feelsLike", weatherData.feelsLike)
                intent.putExtra("uvIndex", weatherData.uvIndex)
                intent.putExtra("visibility", weatherData.visibility)
                intent.putExtra("hasValidData", true)
            }
        }
        
        startActivity(intent)
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
    
    // Today's Focus chip handlers
    private fun showSoilHealthInfo() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Soil Health Focus")
            .setMessage("🌱 Today's soil health tips:\n\n" +
                "• Check soil moisture levels\n" +
                "• Test pH levels (ideal: 6.0-7.0)\n" +
                "• Look for signs of nutrient deficiency\n" +
                "• Consider organic composting\n" +
                "• Monitor soil temperature\n\n" +
                "💡 Healthy soil leads to better crop yields!")
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Learn More") { _, _ -> 
                // Could open a detailed soil health activity
                Toast.makeText(this, "Opening soil health guide...", Toast.LENGTH_SHORT).show()
            }
            .create()
        dialog.show()
    }
    
    private fun showPestWatchInfo() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Pest Watch Focus")
            .setMessage("🛡️ Today's pest monitoring:\n\n" +
                "• Inspect leaves for unusual spots\n" +
                "• Check for insect damage\n" +
                "• Look for pest eggs on plant stems\n" +
                "• Monitor crop growth patterns\n" +
                "• Consider natural pest control\n\n" +
                "⚠️ Early detection prevents major crop loss!")
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Report Pest") { _, _ -> 
                Toast.makeText(this, "Opening pest reporting...", Toast.LENGTH_SHORT).show()
            }
            .create()
        dialog.show()
    }
    
    private fun showMarketRatesInfo() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Market Rates Focus")
            .setMessage("📈 Today's market insights:\n\n" +
                "• Rice: ₹2,800/quintal (+2.5%)\n" +
                "• Wheat: ₹2,200/quintal (-1.2%)\n" +
                "• Cotton: ₹6,500/quintal (+3.8%)\n" +
                "• Sugarcane: ₹350/quintal (stable)\n" +
                "• Tomato: ₹35/kg (+15.2%)\n\n" +
                "💰 Plan your harvest timing wisely!")
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("View Markets") { _, _ -> 
                // Could open market activity
                Toast.makeText(this, "Opening market rates...", Toast.LENGTH_SHORT).show()
            }
            .create()
        dialog.show()
    }
    
    private fun showWaterUsageInfo() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Water Usage Focus")
            .setMessage("💧 Today's water management:\n\n" +
                "• Optimal irrigation: Early morning\n" +
                "• Check drip irrigation systems\n" +
                "• Monitor soil moisture depth\n" +
                "• Consider rainwater harvesting\n" +
                "• Adjust based on weather forecast\n\n" +
                "🌊 Efficient water use saves costs and helps crops!")
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Water Calculator") { _, _ -> 
                Toast.makeText(this, "Opening water usage calculator...", Toast.LENGTH_SHORT).show()
            }
            .create()
        dialog.show()
    }
    
    private fun createFallbackWeatherData(): WeatherService.WeatherData {
        val app = application as com.fasalsaathi.app.FasalSaathiApplication
        val userCity = app.sharedPreferences.getString("user_city", "Unknown City")
        val userState = app.sharedPreferences.getString("user_state", "India")
        val location = if (userCity != "Select City" && userCity != "Unknown City") {
            "$userCity, $userState"
        } else {
            userState ?: "India"
        }
        
        return WeatherService.WeatherData(
            location = location,
            temperature = 28.0,
            temperatureUnit = "°C",
            condition = "Partly Cloudy",
            humidity = 65,
            windSpeed = 12.0,
            windDirection = "NW",
            pressure = 1013.0,
            visibility = 10.0,
            uvIndex = 6,
            feelsLike = 30.0,
            icon = "🌤️"
        )
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}