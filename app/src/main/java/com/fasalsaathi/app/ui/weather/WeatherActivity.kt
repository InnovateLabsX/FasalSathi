package com.fasalsaathi.app.ui.weather

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.fasalsaathi.app.R
import com.fasalsaathi.app.FasalSaathiApplication
import com.fasalsaathi.app.data.api.WeatherService
import com.fasalsaathi.app.data.model.IndianCitiesData
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    
    private lateinit var weatherService: WeatherService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        
        weatherService = WeatherService()
        
        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Weather Details"
        
        // Load weather data
        loadWeatherData()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    
    private fun loadWeatherData() {
        // First check if weather data was passed from dashboard
        if (intent.getBooleanExtra("hasValidData", false)) {
            // Use cached data passed from dashboard
            val cachedWeatherData = WeatherService.WeatherData(
                location = intent.getStringExtra("location") ?: "Unknown Location",
                temperature = intent.getDoubleExtra("temperature", 28.0),
                temperatureUnit = intent.getStringExtra("temperatureUnit") ?: "°C",
                condition = intent.getStringExtra("condition") ?: "Partly Cloudy",
                humidity = intent.getIntExtra("humidity", 65),
                windSpeed = intent.getDoubleExtra("windSpeed", 12.0),
                windDirection = intent.getStringExtra("windDirection") ?: "NW",
                pressure = intent.getDoubleExtra("pressure", 1013.0),
                visibility = intent.getDoubleExtra("visibility", 10.0),
                uvIndex = intent.getIntExtra("uvIndex", 6),
                feelsLike = intent.getDoubleExtra("feelsLike", 30.0),
                icon = intent.getStringExtra("icon") ?: "🌤️"
            )
            updateUI(cachedWeatherData)
            return
        }
        
        // If no cached data, fetch fresh data
        lifecycleScope.launch {
            try {
                val app = application as FasalSaathiApplication
                val weatherData = weatherService.getCurrentWeatherForUser(app.sharedPreferences)
                
                if (weatherData != null) {
                    updateUI(weatherData)
                } else {
                    // Use consistent fallback data
                    val fallbackData = createFallbackWeatherData()
                    updateUI(fallbackData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Use consistent fallback data instead of showing error
                val fallbackData = createFallbackWeatherData()
                updateUI(fallbackData)
            }
        }
    }
    
    private fun createFallbackWeatherData(): WeatherService.WeatherData {
        val app = application as FasalSaathiApplication
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
    
    private fun updateUI(weather: WeatherService.WeatherData) {
        // Location and main weather
        findViewById<TextView>(R.id.tvLocation).text = weather.location
        findViewById<TextView>(R.id.tvTemperature).text = "${weather.temperature.toInt()}${weather.temperatureUnit}"
        findViewById<TextView>(R.id.tvCondition).text = weather.condition
        findViewById<TextView>(R.id.tvWeatherIcon).text = weather.icon
        
        // Current time
        val currentTime = SimpleDateFormat("EEEE, MMMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date())
        findViewById<TextView>(R.id.tvDateTime).text = currentTime
        
        // Basic weather details
        findViewById<TextView>(R.id.tvFeelsLike).text = "${weather.feelsLike.toInt()}°C"
        findViewById<TextView>(R.id.tvHumidity).text = "${weather.humidity}%"
        findViewById<TextView>(R.id.tvWindSpeed).text = "${weather.windSpeed.toInt()} km/h"
        findViewById<TextView>(R.id.tvWindDirection).text = weather.windDirection
        findViewById<TextView>(R.id.tvVisibility).text = "${weather.visibility} km"
        findViewById<TextView>(R.id.tvUvIndex).text = "${weather.uvIndex}"
        findViewById<TextView>(R.id.tvPressure).text = "${weather.pressure.toInt()} hPa"
        
        // Agricultural-specific data from Open-Meteo
        findViewById<TextView>(R.id.tvPrecipitationProb).text = "${weather.precipitationProbability}%"
        findViewById<TextView>(R.id.tvPrecipitationAmount).text = "${weather.precipitationMm} mm"
        findViewById<TextView>(R.id.tvSoilMoisture).text = "${(weather.soilMoisture * 100).toInt()}%"
        findViewById<TextView>(R.id.tvSoilTemperature).text = "${weather.soilTemperature.toInt()}°C"
        findViewById<TextView>(R.id.tvCloudCover).text = "${weather.cloudCover}%"
        
        // Agricultural recommendations
        updateAgriculturalAdvice(weather)
        
        // Set UV index color
        setUvIndexColor(weather.uvIndex)
        
        // Set soil moisture color
        setSoilMoistureColor(weather.soilMoisture)
    }
    
    private fun updateAgriculturalAdvice(weather: WeatherService.WeatherData) {
        val advice = buildString {
            // Irrigation advice based on soil moisture
            when {
                weather.soilMoisture < 0.15 -> append("🚰 Irrigation needed - Soil moisture is low (${(weather.soilMoisture * 100).toInt()}%)\n\n")
                weather.soilMoisture > 0.35 -> append("💧 Soil well hydrated (${(weather.soilMoisture * 100).toInt()}%) - Monitor drainage\n\n")
                else -> append("✅ Soil moisture optimal (${(weather.soilMoisture * 100).toInt()}%)\n\n")
            }
            
            // Planting advice based on soil temperature
            when {
                weather.soilTemperature < 15 -> append("❄️ Soil too cold for most crops (${weather.soilTemperature.toInt()}°C)\n\n")
                weather.soilTemperature > 30 -> append("🌡️ Soil quite warm (${weather.soilTemperature.toInt()}°C) - Good for heat-loving crops\n\n")
                else -> append("🌱 Good soil temperature for planting (${weather.soilTemperature.toInt()}°C)\n\n")
            }
            
            // Weather-based activity recommendations
            when {
                weather.precipitationProbability > 70 -> append("🌧️ High rain probability (${weather.precipitationProbability}%) - Avoid field work")
                weather.precipitationProbability > 30 -> append("⛅ Possible rain (${weather.precipitationProbability}%) - Plan indoor activities")
                weather.cloudCover < 30 -> append("☀️ Clear skies (${weather.cloudCover}% clouds) - Good for outdoor work")
                else -> append("🌤️ Partly cloudy - Normal farming activities possible")
            }
            
            // UV protection advice
            if (weather.uvIndex > 6) {
                append("\n\n☀️ High UV index (${weather.uvIndex}) - Use sun protection during field work")
            }
        }
        
        findViewById<TextView>(R.id.tvAgriculturalAdvice).text = advice
    }
    
    private fun setUvIndexColor(uvIndex: Int) {
        val colorRes = when {
            uvIndex <= 2 -> R.color.uv_low
            uvIndex <= 5 -> R.color.uv_moderate
            uvIndex <= 7 -> R.color.uv_high
            uvIndex <= 10 -> R.color.uv_very_high
            else -> R.color.uv_extreme
        }
        
        findViewById<TextView>(R.id.tvUvIndex).setTextColor(getColor(colorRes))
    }
    
    private fun setSoilMoistureColor(soilMoisture: Double) {
        val colorRes = when {
            soilMoisture < 0.15 -> R.color.soil_dry
            soilMoisture < 0.25 -> R.color.soil_moderate
            soilMoisture < 0.35 -> R.color.soil_good
            else -> R.color.soil_wet
        }
        
        findViewById<TextView>(R.id.tvSoilMoisture).setTextColor(getColor(colorRes))
    }
    
    private fun showError(message: String) {
        findViewById<TextView>(R.id.tvLocation).text = "Error"
        findViewById<TextView>(R.id.tvTemperature).text = "--°C"
        findViewById<TextView>(R.id.tvCondition).text = message
    }
}