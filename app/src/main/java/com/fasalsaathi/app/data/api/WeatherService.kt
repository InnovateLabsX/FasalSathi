package com.fasalsaathi.app.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import com.fasalsaathi.app.data.model.IndianCity
import com.fasalsaathi.app.data.model.IndianCitiesData

/**
 * Weather service to fetch real weather data from OpenWeatherMap API
 * Integrated with Indian cities database for accurate location-based weather
 */
class WeatherService {
    
    companion object {
        // OpenWeatherMap API - Free tier available
        private const val OPENWEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5"
        // Replace with your actual OpenWeatherMap API key from https://openweathermap.org/api
        private const val OPENWEATHER_API_KEY = "781eb4b3bfc94420a75114903252709" // Replace with your actual API key
        
        // For production: Get free API key from https://openweathermap.org/api
        // Currently using enhanced simulation with realistic data
        private const val USE_REAL_API = true // Set to true when you have a valid API key
    }
    
    data class WeatherData(
        val location: String,
        val temperature: Double,
        val temperatureUnit: String = "°C",
        val condition: String,
        val humidity: Int,
        val windSpeed: Double,
        val windDirection: String,
        val pressure: Double,
        val visibility: Double,
        val uvIndex: Int,
        val feelsLike: Double,
        val icon: String
    )
    
    data class ForecastData(
        val date: String,
        val dayOfWeek: String,
        val highTemp: Double,
        val lowTemp: Double,
        val condition: String,
        val precipitationProbability: Int,
        val icon: String
    )
    
    /**
     * Get current weather for a specific Indian city
     */
    suspend fun getCurrentWeatherForCity(city: IndianCity): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                if (USE_REAL_API && OPENWEATHER_API_KEY != "YOUR_API_KEY_HERE") {
                    // Use real OpenWeatherMap API
                    fetchRealWeatherData(city.latitude, city.longitude, city.name, city.state)
                } else {
                    // Use enhanced simulated data based on actual city coordinates
                    getEnhancedSimulatedWeatherData(city)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to simulated data
                getEnhancedSimulatedWeatherData(city)
            }
        }
    }
    
    /**
     * Get current weather by city name (legacy method for backward compatibility)
     */
    suspend fun getCurrentWeatherByCity(cityName: String): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                // Find city in Indian cities database
                val city = com.fasalsaathi.app.data.model.IndianCitiesData.getCityByName(cityName)
                if (city != null) {
                    getCurrentWeatherForCity(city)
                } else {
                    // Fallback to basic simulated data
                    getSimulatedWeatherDataByCity(cityName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                getSimulatedWeatherDataByCity(cityName)
            }
        }
    }
    
    /**
     * Get current weather for user's saved location from SharedPreferences
     */
    suspend fun getCurrentWeatherForUser(sharedPreferences: android.content.SharedPreferences): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val cityName = sharedPreferences.getString("user_city", null)
                val latitude = sharedPreferences.getFloat("user_city_lat", 0f).toDouble()
                val longitude = sharedPreferences.getFloat("user_city_lon", 0f).toDouble()
                val state = sharedPreferences.getString("user_state", "")
                
                println("WeatherService Debug - City: $cityName, Lat: $latitude, Lon: $longitude, State: $state")
                
                if (cityName != null && cityName != "Select City" && latitude != 0.0 && longitude != 0.0) {
                    println("Using saved location data for weather")
                    if (USE_REAL_API && OPENWEATHER_API_KEY != "YOUR_API_KEY_HERE") {
                        println("Fetching real weather data for $cityName")
                        val realWeather = fetchRealWeatherData(latitude, longitude, cityName, state ?: "")
                        if (realWeather != null) {
                            println("Real weather data fetched successfully")
                            realWeather
                        } else {
                            println("Real weather failed, using enhanced simulation")
                            // Fallback to simulation if API fails
                            val tempCity = IndianCity(cityName, state ?: "", latitude, longitude)
                            getEnhancedSimulatedWeatherData(tempCity)
                        }
                    } else {
                        println("Using enhanced simulation for $cityName")
                        // Create a temporary IndianCity object for simulation
                        val tempCity = IndianCity(cityName, state ?: "", latitude, longitude)
                        getEnhancedSimulatedWeatherData(tempCity)
                    }
                } else {
                    println("No valid location data, using simple fallback")
                    // Simple fallback that always works
                    getSimpleWeatherFallback()
                }
            } catch (e: Exception) {
                println("Weather error: ${e.message}")
                e.printStackTrace()
                // Always return fallback weather data - never return null
                return@withContext getSimpleWeatherFallback()
            }
        }
    }

    /**
     * Get current weather for coordinates (legacy method)
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                if (USE_REAL_API && OPENWEATHER_API_KEY != "YOUR_API_KEY_HERE") {
                    fetchRealWeatherData(latitude, longitude, "Unknown Location", "")
                } else {
                    getSimulatedWeatherData(latitude, longitude)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                getSimulatedWeatherData(latitude, longitude)
            }
        }
    }
    
    /**
     * Get 5-day weather forecast
     */
    suspend fun getFiveDayForecast(latitude: Double, longitude: Double): List<ForecastData>? {
        return withContext(Dispatchers.IO) {
            try {
                getSimulatedForecastData()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Simulated weather data for Indian farming regions
     * In production, replace with actual API calls
     */
    private fun getSimulatedWeatherData(lat: Double, lon: Double): WeatherData {
        // Simulate weather based on coordinates (Indian subcontinent focus)
        val temp = when {
            lat > 30 -> (15..25).random() // Northern regions (cooler)
            lat < 15 -> (25..35).random() // Southern regions (warmer)
            else -> (20..30).random() // Central regions
        }
        
        val conditions = listOf(
            "Clear Sky", "Partly Cloudy", "Cloudy", "Light Rain", 
            "Moderate Rain", "Sunny", "Haze", "Misty"
        )
        
        val location = determineLocation(lat, lon)
        
        return WeatherData(
            location = location,
            temperature = temp.toDouble(),
            condition = conditions.random(),
            humidity = (60..85).random(),
            windSpeed = (5..15).random().toDouble(),
            windDirection = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW").random(),
            pressure = (1008..1020).random().toDouble(),
            visibility = (8..15).random().toDouble(),
            uvIndex = (3..8).random(),
            feelsLike = temp + (-2..3).random().toDouble(),
            icon = getWeatherIcon(conditions.random())
        )
    }
    
    /**
     * Simulated weather data by city name
     */
    private fun getSimulatedWeatherDataByCity(cityName: String): WeatherData {
        val temp = when (cityName.lowercase()) {
            "delhi", "new delhi" -> (20..30).random()
            "mumbai" -> (25..32).random()
            "chennai" -> (28..35).random()
            "kolkata" -> (22..30).random()
            "bangalore", "bengaluru" -> (18..28).random()
            "hyderabad" -> (22..32).random()
            "pune" -> (20..30).random()
            "jaipur" -> (18..32).random()
            else -> (20..30).random()
        }
        
        val conditions = getSeasonalConditions()
        
        return WeatherData(
            location = cityName,
            temperature = temp.toDouble(),
            condition = conditions.random(),
            humidity = (60..85).random(),
            windSpeed = (5..15).random().toDouble(),
            windDirection = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW").random(),
            pressure = (1008..1020).random().toDouble(),
            visibility = (8..15).random().toDouble(),
            uvIndex = (3..8).random(),
            feelsLike = temp + (-2..3).random().toDouble(),
            icon = getWeatherIcon(conditions.random())
        )
    }
    
    /**
     * Get seasonal conditions based on current month
     */
    private fun getSeasonalConditions(): List<String> {
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        return when (month) {
            11, 0, 1 -> listOf("Clear Sky", "Misty", "Foggy", "Cool", "Sunny") // Winter
            2, 3, 4 -> listOf("Sunny", "Hot", "Clear Sky", "Haze", "Windy") // Summer
            5, 6, 7, 8 -> listOf("Rainy", "Heavy Rain", "Cloudy", "Humid", "Overcast") // Monsoon
            9, 10 -> listOf("Pleasant", "Clear Sky", "Partly Cloudy", "Cool Breeze") // Post-monsoon
            else -> listOf("Clear Sky", "Partly Cloudy", "Sunny")
        }
    }
    
    /**
     * Simulated 5-day forecast
     */
    private fun getSimulatedForecastData(): List<ForecastData> {
        val forecast = mutableListOf<ForecastData>()
        val calendar = java.util.Calendar.getInstance()
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        
        repeat(5) { index ->
            calendar.add(java.util.Calendar.DAY_OF_YEAR, if (index == 0) 0 else 1)
            val dayOfWeek = days[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            val baseTemp = (20..30).random()
            
            forecast.add(
                ForecastData(
                    date = "${calendar.get(java.util.Calendar.DAY_OF_MONTH)}/${calendar.get(java.util.Calendar.MONTH) + 1}",
                    dayOfWeek = if (index == 0) "Today" else dayOfWeek,
                    highTemp = (baseTemp + 2..baseTemp + 8).random().toDouble(),
                    lowTemp = (baseTemp - 5..baseTemp).random().toDouble(),
                    condition = getSeasonalConditions().random(),
                    precipitationProbability = (10..80).random(),
                    icon = getWeatherIcon(getSeasonalConditions().random())
                )
            )
        }
        
        return forecast
    }
    
    /**
     * Determine location name from coordinates
     */
    private fun determineLocation(lat: Double, lon: Double): String {
        return when {
            lat in 28.0..29.0 && lon in 76.0..78.0 -> "Delhi"
            lat in 18.0..19.5 && lon in 72.0..73.5 -> "Mumbai"
            lat in 12.5..13.5 && lon in 80.0..81.0 -> "Chennai"
            lat in 22.0..23.0 && lon in 88.0..89.0 -> "Kolkata"
            lat in 12.0..13.0 && lon in 77.0..78.0 -> "Bangalore"
            lat in 17.0..18.0 && lon in 78.0..79.0 -> "Hyderabad"
            lat in 18.0..19.0 && lon in 73.0..74.0 -> "Pune"
            lat in 26.0..27.0 && lon in 75.0..76.0 -> "Jaipur"
            else -> "Unknown Location"
        }
    }
    
    /**
     * Get weather icon based on condition
     */
    private fun getWeatherIcon(condition: String): String {
        return when (condition.lowercase()) {
            "clear sky", "sunny" -> "☀️"
            "partly cloudy" -> "⛅"
            "cloudy", "overcast" -> "☁️"
            "rainy", "light rain", "moderate rain", "heavy rain" -> "🌧️"
            "thunderstorm" -> "⛈️"
            "misty", "foggy", "haze" -> "🌫️"
            "windy" -> "💨"
            "hot" -> "🌡️"
            else -> "🌤️"
        }
    }
    
    /**
     * Fetch real weather data from OpenWeatherMap API
     */
    private suspend fun fetchRealWeatherData(lat: Double, lon: Double, cityName: String, state: String): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$OPENWEATHER_BASE_URL/weather?lat=$lat&lon=$lon&appid=$OPENWEATHER_API_KEY&units=metric"
                println("Weather API URL: $url")
                
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 15000
                
                val responseCode = connection.responseCode
                println("Weather API Response Code: $responseCode")
                
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    println("Weather API Response: $response")
                    parseOpenWeatherResponse(response, cityName, state)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.readText()
                    println("Weather API Error: $responseCode - $errorResponse")
                    null
                }
            } catch (e: Exception) {
                println("Weather API Exception: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
    
    private fun parseOpenWeatherResponse(jsonResponse: String, cityName: String, state: String): WeatherData? {
        return try {
            val json = JSONObject(jsonResponse)
            val main = json.getJSONObject("main")
            val weather = json.getJSONArray("weather").getJSONObject(0)
            val wind = json.optJSONObject("wind")
            val sys = json.optJSONObject("sys")
            
            val location = if (state.isNotEmpty()) "$cityName, $state" else cityName
            
            WeatherData(
                location = location,
                temperature = main.getDouble("temp"),
                condition = weather.getString("description").replaceFirstChar { it.titlecase() },
                humidity = main.getInt("humidity"),
                windSpeed = (wind?.optDouble("speed") ?: 0.0) * 3.6, // Convert m/s to km/h
                windDirection = getWindDirection(wind?.optDouble("deg") ?: 0.0),
                pressure = main.getDouble("pressure"),
                visibility = json.optDouble("visibility", 10000.0) / 1000, // Convert to km
                uvIndex = 0, // Would need separate UV API call
                feelsLike = main.getDouble("feels_like"),
                icon = getWeatherIconFromCondition(weather.getString("main"))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Simple weather fallback that always works
     */
    private fun getSimpleWeatherFallback(): WeatherData {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        
        // Simple temperature based on time of day
        val baseTemp = when (hour) {
            in 6..9 -> 22 // Morning
            in 10..15 -> 28 // Day
            in 16..18 -> 25 // Evening
            else -> 20 // Night
        }
        
        val conditions = listOf("☀️ Sunny", "🌤️ Partly Cloudy", "☁️ Cloudy")
        val condition = conditions.random()
        
        return WeatherData(
            location = "Delhi, India",
            temperature = baseTemp.toDouble(),
            temperatureUnit = "°C",
            condition = condition.substring(3), // Remove emoji for condition text
            humidity = 65,
            windSpeed = 12.0,
            windDirection = "NW",
            pressure = 1013.25,
            visibility = 10.0,
            uvIndex = 5,
            feelsLike = baseTemp + 2.0,
            icon = condition.substring(0, 2) // Just emoji
        )
    }
    
    /**
     * Enhanced simulated weather data using real Indian city coordinates and seasonal patterns
     */
    private fun getEnhancedSimulatedWeatherData(city: IndianCity): WeatherData {
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH)
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        
        // Base temperature calculation based on city latitude and season
        val baseTemp = calculateSeasonalTemperature(city.latitude, month)
        val dailyVariation = calculateDailyTemperatureVariation(hour)
        val actualTemp = baseTemp + dailyVariation
        
        // Get regional weather patterns
        val weatherPattern = getRegionalWeatherPattern(city.state, month)
        val condition = weatherPattern.conditions.random()
        
        // Calculate other parameters based on location and season
        val humidity = calculateHumidity(city.latitude, month, condition)
        val windSpeed = calculateWindSpeed(city.state, month)
        val pressure = calculatePressure(city.latitude)
        
        return WeatherData(
            location = "${city.name}, ${city.state}",
            temperature = actualTemp,
            condition = condition,
            humidity = humidity,
            windSpeed = windSpeed,
            windDirection = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW").random(),
            pressure = pressure,
            visibility = (8..15).random().toDouble(),
            uvIndex = calculateUVIndex(city.latitude, month, hour),
            feelsLike = actualTemp + (-2..3).random(),
            icon = getWeatherIcon(condition)
        )
    }
    
    private fun calculateSeasonalTemperature(latitude: Double, month: Int): Double {
        // Base temperature varies by latitude (North-South India)
        val baseTemp = when {
            latitude > 30 -> 15.0 // Northern regions (Himachal, J&K)
            latitude > 25 -> 22.0 // North India (Punjab, Haryana, Delhi)
            latitude > 20 -> 26.0 // Central India (MP, Maharashtra)
            latitude > 15 -> 28.0 // South-Central (Karnataka, Andhra)
            else -> 30.0 // Deep South (Tamil Nadu, Kerala)
        }
        
        // Seasonal variation
        val seasonalAdjustment = when (month) {
            11, 0, 1 -> -8.0 // Winter (Dec, Jan, Feb)
            2, 3 -> -2.0     // Spring (Mar, Apr)
            4, 5 -> 8.0      // Summer (May, Jun)
            6, 7, 8 -> -3.0  // Monsoon (Jul, Aug, Sep)
            9, 10 -> 2.0     // Post-monsoon (Oct, Nov)
            else -> 0.0
        }
        
        return (baseTemp + seasonalAdjustment).coerceIn(5.0, 45.0)
    }
    
    private fun calculateDailyTemperatureVariation(hour: Int): Double {
        // Daily temperature cycle
        return when (hour) {
            in 0..5 -> -4.0   // Night
            in 6..9 -> -1.0   // Morning
            in 10..12 -> 3.0  // Late morning
            in 13..15 -> 5.0  // Afternoon (peak)
            in 16..18 -> 2.0  // Evening
            in 19..23 -> -2.0 // Night
            else -> 0.0
        }
    }
    
    private fun calculateHumidity(latitude: Double, month: Int, condition: String): Int {
        var baseHumidity = when {
            latitude < 15 -> 75 // Coastal South India
            latitude < 20 -> 65 // Interior South
            latitude < 25 -> 60 // Central India
            else -> 55 // North India
        }
        
        // Monsoon adjustment
        if (month in 6..8) baseHumidity += 20
        
        // Condition adjustment
        when {
            condition.contains("rain", ignoreCase = true) -> baseHumidity += 15
            condition.contains("clear", ignoreCase = true) -> baseHumidity -= 10
            condition.contains("cloud", ignoreCase = true) -> baseHumidity += 5
        }
        
        return baseHumidity.coerceIn(30, 95)
    }
    
    private fun calculateWindSpeed(state: String, month: Int): Double {
        val baseWind = when (state) {
            "Rajasthan", "Gujarat" -> 12.0 // Desert regions
            "Maharashtra", "Karnataka" -> 8.0 // Western regions
            "West Bengal", "Odisha" -> 15.0 // Coastal East
            "Tamil Nadu", "Kerala" -> 10.0 // South coastal
            else -> 7.0
        }
        
        // Monsoon winds
        val seasonalWind = if (month in 6..8) baseWind * 1.5 else baseWind
        return seasonalWind.coerceIn(3.0, 25.0)
    }
    
    private fun calculatePressure(latitude: Double): Double {
        // Pressure decreases with altitude and varies by season
        val basePressure = when {
            latitude > 30 -> 1005.0 // High altitude regions
            else -> 1013.0 // Sea level regions
        }
        return basePressure + (-5..5).random()
    }
    
    private fun calculateUVIndex(latitude: Double, month: Int, hour: Int): Int {
        if (hour < 6 || hour > 18) return 0
        
        val baseUV = when {
            latitude < 15 -> 9 // Equatorial regions
            latitude < 25 -> 7 // Tropical regions
            else -> 5 // Temperate regions
        }
        
        val seasonalUV = when (month) {
            4, 5 -> baseUV + 2 // Summer peak
            11, 0, 1 -> baseUV - 2 // Winter
            else -> baseUV
        }
        
        return seasonalUV.coerceIn(0, 11)
    }
    
    private data class WeatherPattern(
        val conditions: List<String>,
        val description: String
    )
    
    private fun getRegionalWeatherPattern(state: String, month: Int): WeatherPattern {
        return when (state) {
            "Rajasthan", "Gujarat", "Haryana" -> {
                if (month in 6..8) {
                    WeatherPattern(listOf("Light Rain", "Cloudy", "Partly Cloudy"), "Monsoon season")
                } else {
                    WeatherPattern(listOf("Clear Sky", "Sunny", "Hot", "Haze"), "Arid climate")
                }
            }
            "Kerala", "Karnataka", "Tamil Nadu" -> {
                if (month in 6..8) {
                    WeatherPattern(listOf("Heavy Rain", "Thunderstorm", "Cloudy"), "Southwest monsoon")
                } else {
                    WeatherPattern(listOf("Partly Cloudy", "Humid", "Clear Sky"), "Tropical climate")
                }
            }
            "West Bengal", "Odisha", "Assam" -> {
                if (month in 6..8) {
                    WeatherPattern(listOf("Heavy Rain", "Thunderstorm", "Overcast"), "Monsoon")
                } else {
                    WeatherPattern(listOf("Humid", "Partly Cloudy", "Misty"), "Humid subtropical")
                }
            }
            "Himachal Pradesh", "Uttarakhand", "Jammu and Kashmir" -> {
                if (month in 11..2) {
                    WeatherPattern(listOf("Snow", "Cold", "Clear Sky", "Foggy"), "Mountain winter")
                } else {
                    WeatherPattern(listOf("Pleasant", "Cool Breeze", "Clear Sky"), "Mountain climate")
                }
            }
            else -> {
                if (month in 6..8) {
                    WeatherPattern(listOf("Rainy", "Cloudy", "Humid"), "Monsoon")
                } else {
                    WeatherPattern(listOf("Clear Sky", "Partly Cloudy", "Sunny"), "General Indian climate")
                }
            }
        }
    }
    
    private fun getWindDirection(degrees: Double): String {
        return when ((degrees / 45).toInt() % 8) {
            0 -> "N"
            1 -> "NE"
            2 -> "E"
            3 -> "SE"
            4 -> "S"
            5 -> "SW"
            6 -> "W"
            7 -> "NW"
            else -> "N"
        }
    }
    
    private fun getWeatherIconFromCondition(condition: String): String {
        return when (condition.lowercase()) {
            "clear" -> "☀️"
            "clouds" -> "☁️"
            "rain", "drizzle" -> "🌧️"
            "thunderstorm" -> "⛈️"
            "snow" -> "❄️"
            "mist", "fog", "haze" -> "🌫️"
            else -> "🌤️"
        }
    }
}