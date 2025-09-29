package com.fasalsaathi.app.weather

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Weather API Service for Fasal Sathi
 * Integrates with Python weather service to provide real-time weather data
 */
class WeatherApiService(private val context: Context) {
    
    companion object {
        private const val TAG = "WeatherApiService"
        private const val WEATHER_SCRIPT_PATH = "weather_service.py"
        private const val CACHE_EXPIRY_HOURS = 1
    }
    
    data class WeatherData(
        val currentTemperature: Double,
        val currentHumidity: Double,
        val currentPrecipitation: Double,
        val weatherCode: Int,
        val weatherDescription: String,
        val windSpeed: Double,
        val pressure: Double,
        val soilTemperature: Double,
        val soilMoisture: Double,
        val uvIndex: Double,
        val agriculturalMetrics: AgriculturalMetrics
    )
    
    data class AgriculturalMetrics(
        val avgTemperature24h: Double,
        val avgHumidity24h: Double,
        val totalPrecipitation7d: Double,
        val avgSoilMoisture: Double,
        val growingDegreeDays: Double,
        val waterStressIndex: Double,
        val heatStressRisk: String,
        val irrigationNeedIndex: Double,
        val frostRisk: Boolean,
        val plantingConditions: PlantingConditions
    )
    
    data class PlantingConditions(
        val temperatureSuitable: Boolean,
        val moistureAdequate: Boolean,
        val noExtremeWeather: Boolean,
        val overallRating: String
    )
    
    data class HourlyForecast(
        val hour: Int,
        val temperature: Double,
        val humidity: Double,
        val precipitation: Double,
        val precipitationProbability: Double,
        val weatherCode: Int,
        val soilMoisture: Double,
        val uvIndex: Double
    )
    
    data class DailyForecast(
        val day: Int,
        val weatherCode: Int,
        val tempMax: Double,
        val tempMin: Double,
        val precipitationSum: Double,
        val precipitationProbability: Double,
        val windSpeedMax: Double,
        val uvIndexMax: Double
    )
    
    /**
     * Get comprehensive weather data for given coordinates
     */
    suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val weatherJson = callPythonWeatherService(latitude, longitude)
                parseWeatherData(weatherJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting weather data: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Get hourly forecast for next 24 hours
     */
    suspend fun getHourlyForecast(latitude: Double, longitude: Double): List<HourlyForecast> {
        return withContext(Dispatchers.IO) {
            try {
                val weatherJson = callPythonWeatherService(latitude, longitude)
                parseHourlyForecast(weatherJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting hourly forecast: ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * Get daily forecast for next 7 days
     */
    suspend fun getDailyForecast(latitude: Double, longitude: Double): List<DailyForecast> {
        return withContext(Dispatchers.IO) {
            try {
                val weatherJson = callPythonWeatherService(latitude, longitude)
                parseDailyForecast(weatherJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting daily forecast: ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * Call Python weather service script
     */
    private suspend fun callPythonWeatherService(latitude: Double, longitude: Double): JSONObject {
        return withContext(Dispatchers.IO) {
            try {
                // Get app root directory
                val appRootDir = File(context.applicationInfo.dataDir).parent
                val projectDir = File(appRootDir).parent
                
                // Build command to run Python weather service
                val command = arrayOf(
                    "bash", "-c",
                    "cd '$projectDir' && source weather_venv/bin/activate && python weather_service.py $latitude $longitude"
                )
                
                Log.d(TAG, "Running command: ${command.joinToString(" ")}")
                
                val process = ProcessBuilder(*command)
                    .redirectErrorStream(true)
                    .start()
                
                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()
                
                Log.d(TAG, "Weather service exit code: $exitCode")
                Log.d(TAG, "Weather service output: ${output.take(500)}...")
                
                if (exitCode == 0) {
                    JSONObject(output)
                } else {
                    throw Exception("Weather service failed with exit code: $exitCode, output: $output")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calling Python weather service: ${e.message}")
                // Return mock data for development
                createMockWeatherData(latitude, longitude)
            }
        }
    }
    
    /**
     * Parse weather data from JSON response
     */
    private fun parseWeatherData(json: JSONObject): WeatherData {
        val current = json.getJSONObject("current")
        val hourly = json.getJSONArray("hourly")
        val metrics = json.getJSONObject("agricultural_metrics")
        
        // Get first hour soil data
        val firstHour = hourly.getJSONObject(0)
        val avgSoilTemp = (firstHour.getDouble("soil_temp_0cm") + 
                          firstHour.getDouble("soil_temp_6cm") + 
                          firstHour.getDouble("soil_temp_18cm")) / 3
        val avgSoilMoisture = (firstHour.getDouble("soil_moisture_0_1cm") + 
                              firstHour.getDouble("soil_moisture_1_3cm") + 
                              firstHour.getDouble("soil_moisture_3_9cm")) / 3
        
        val plantingConditions = metrics.getJSONObject("optimal_planting_conditions")
        
        return WeatherData(
            currentTemperature = current.getDouble("temperature"),
            currentHumidity = current.getDouble("humidity"),
            currentPrecipitation = current.getDouble("precipitation"),
            weatherCode = current.getInt("weather_code"),
            weatherDescription = getWeatherDescription(current.getInt("weather_code")),
            windSpeed = current.getDouble("wind_speed"),
            pressure = current.getDouble("pressure"),
            soilTemperature = avgSoilTemp,
            soilMoisture = avgSoilMoisture,
            uvIndex = firstHour.getDouble("uv_index"),
            agriculturalMetrics = AgriculturalMetrics(
                avgTemperature24h = metrics.getDouble("avg_temperature_24h"),
                avgHumidity24h = metrics.getDouble("avg_humidity_24h"),
                totalPrecipitation7d = metrics.getDouble("total_precipitation_7d"),
                avgSoilMoisture = metrics.getDouble("avg_soil_moisture"),
                growingDegreeDays = metrics.getDouble("growing_degree_days"),
                waterStressIndex = metrics.getDouble("water_stress_index"),
                heatStressRisk = metrics.getString("heat_stress_risk"),
                irrigationNeedIndex = metrics.getDouble("irrigation_need_index"),
                frostRisk = metrics.getBoolean("frost_risk"),
                plantingConditions = PlantingConditions(
                    temperatureSuitable = plantingConditions.getBoolean("temperature_suitable"),
                    moistureAdequate = plantingConditions.getBoolean("moisture_adequate"),
                    noExtremeWeather = plantingConditions.getBoolean("no_extreme_weather"),
                    overallRating = plantingConditions.getString("overall_rating")
                )
            )
        )
    }
    
    /**
     * Parse hourly forecast data
     */
    private fun parseHourlyForecast(json: JSONObject): List<HourlyForecast> {
        val hourlyArray = json.getJSONArray("hourly")
        val forecast = mutableListOf<HourlyForecast>()
        
        for (i in 0 until hourlyArray.length()) {
            val hour = hourlyArray.getJSONObject(i)
            val avgSoilMoisture = (hour.getDouble("soil_moisture_0_1cm") + 
                                  hour.getDouble("soil_moisture_1_3cm") + 
                                  hour.getDouble("soil_moisture_3_9cm")) / 3
            
            forecast.add(
                HourlyForecast(
                    hour = hour.getInt("hour"),
                    temperature = hour.getDouble("temperature"),
                    humidity = hour.getDouble("humidity"),
                    precipitation = hour.getDouble("precipitation"),
                    precipitationProbability = hour.getDouble("precipitation_probability"),
                    weatherCode = hour.getInt("weather_code"),
                    soilMoisture = avgSoilMoisture,
                    uvIndex = hour.getDouble("uv_index")
                )
            )
        }
        
        return forecast
    }
    
    /**
     * Parse daily forecast data
     */
    private fun parseDailyForecast(json: JSONObject): List<DailyForecast> {
        val dailyArray = json.getJSONArray("daily")
        val forecast = mutableListOf<DailyForecast>()
        
        for (i in 0 until dailyArray.length()) {
            val day = dailyArray.getJSONObject(i)
            
            forecast.add(
                DailyForecast(
                    day = day.getInt("day"),
                    weatherCode = day.getInt("weather_code"),
                    tempMax = day.getDouble("temp_max"),
                    tempMin = day.getDouble("temp_min"),
                    precipitationSum = day.getDouble("precipitation_sum"),
                    precipitationProbability = day.getDouble("precipitation_probability"),
                    windSpeedMax = day.getDouble("wind_speed_max"),
                    uvIndexMax = day.getDouble("uv_index_max")
                )
            )
        }
        
        return forecast
    }
    
    /**
     * Create mock weather data for development/fallback
     */
    private fun createMockWeatherData(latitude: Double, longitude: Double): JSONObject {
        return JSONObject().apply {
            put("status", "success")
            put("current", JSONObject().apply {
                put("temperature", 28.5)
                put("humidity", 65.0)
                put("precipitation", 0.0)
                put("weather_code", 1)
                put("wind_speed", 5.2)
                put("pressure", 1013.2)
            })
            put("hourly", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("hour", 0)
                    put("temperature", 28.5)
                    put("humidity", 65.0)
                    put("precipitation", 0.0)
                    put("precipitation_probability", 10.0)
                    put("weather_code", 1)
                    put("soil_temp_0cm", 26.0)
                    put("soil_temp_6cm", 25.5)
                    put("soil_temp_18cm", 25.0)
                    put("soil_moisture_0_1cm", 0.25)
                    put("soil_moisture_1_3cm", 0.28)
                    put("soil_moisture_3_9cm", 0.30)
                    put("uv_index", 6.5)
                })
            })
            put("agricultural_metrics", JSONObject().apply {
                put("avg_temperature_24h", 28.0)
                put("avg_humidity_24h", 65.0)
                put("total_precipitation_7d", 15.2)
                put("avg_soil_moisture", 0.28)
                put("growing_degree_days", 126.0)
                put("water_stress_index", 0.3)
                put("heat_stress_risk", "low")
                put("irrigation_need_index", 0.4)
                put("frost_risk", false)
                put("optimal_planting_conditions", JSONObject().apply {
                    put("temperature_suitable", true)
                    put("moisture_adequate", true)
                    put("no_extreme_weather", true)
                    put("overall_rating", "good")
                })
            })
        }
    }
    
    /**
     * Convert weather code to description
     */
    private fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61 -> "Light rain"
            63 -> "Moderate rain"
            65 -> "Heavy rain"
            66, 67 -> "Freezing rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown weather"
        }
    }
    
    /**
     * Get weather recommendation for crops
     */
    fun getWeatherBasedCropRecommendations(weatherData: WeatherData): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Temperature-based recommendations
        when {
            weatherData.currentTemperature > 35 -> {
                recommendations.add("🌡️ High temperature detected. Consider heat-tolerant crops like sorghum or pearl millet.")
                recommendations.add("💧 Increase irrigation frequency to prevent heat stress.")
            }
            weatherData.currentTemperature < 10 -> {
                recommendations.add("❄️ Low temperature detected. Consider cold-tolerant crops like wheat or barley.")
                recommendations.add("🔥 Monitor for frost risk and use protective measures if needed.")
            }
            else -> {
                recommendations.add("🌤️ Temperature is optimal for most crops.")
            }
        }
        
        // Humidity-based recommendations
        when {
            weatherData.currentHumidity > 80 -> {
                recommendations.add("💨 High humidity detected. Monitor for fungal diseases.")
                recommendations.add("🌾 Ensure good air circulation around crops.")
            }
            weatherData.currentHumidity < 40 -> {
                recommendations.add("🏜️ Low humidity detected. Increase watering frequency.")
                recommendations.add("🌿 Consider mulching to retain moisture.")
            }
        }
        
        // Agricultural metrics recommendations
        val metrics = weatherData.agriculturalMetrics
        
        when (metrics.heatStressRisk) {
            "high" -> recommendations.add("🔥 High heat stress risk. Provide shade and increase water supply.")
            "moderate" -> recommendations.add("☀️ Moderate heat stress. Monitor crop health closely.")
        }
        
        when {
            metrics.irrigationNeedIndex > 0.7 -> {
                recommendations.add("💧 High irrigation need detected. Schedule immediate watering.")
            }
            metrics.irrigationNeedIndex > 0.4 -> {
                recommendations.add("🚿 Moderate irrigation need. Plan watering within 24 hours.")
            }
        }
        
        if (metrics.frostRisk) {
            recommendations.add("❄️ Frost risk detected. Cover sensitive crops or use frost protection methods.")
        }
        
        // Planting conditions
        val planting = metrics.plantingConditions
        when (planting.overallRating) {
            "excellent" -> recommendations.add("🌱 Excellent conditions for planting new crops!")
            "good" -> recommendations.add("🌾 Good conditions for planting. Consider sowing soon.")
            "fair" -> recommendations.add("⚠️ Fair conditions. Wait for better weather if possible.")
            "poor" -> recommendations.add("⛔ Poor planting conditions. Delay sowing until conditions improve.")
        }
        
        return recommendations
    }
}