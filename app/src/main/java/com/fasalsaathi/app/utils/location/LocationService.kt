package com.fasalsaathi.app.utils.location

import android.content.Context
import android.location.Location
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Google Places and Geocoding Integration
 * Provides unlimited location selection with real-time weather data
 */
class LocationService(private val context: Context) {
    
    companion object {
        private const val TAG = "LocationService"
        private const val PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place"
        private const val GEOCODING_API_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json"
        
        // For demonstration - In production, store securely
        private const val GOOGLE_API_KEY = "YOUR_GOOGLE_PLACES_API_KEY"
        
        // Weather API integration
        private const val WEATHER_SERVICE_TIMEOUT = 15000 // 15 seconds
    }
    
    data class PlaceResult(
        val placeId: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val type: String = "locality"
    )
    
    data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val address: String,
        val city: String,
        val state: String = "",
        val country: String = "",
        val temperature: Double? = null,
        val humidity: Double? = null,
        val rainfall: Double? = null
    )
    
    /**
     * Search for places using Google Places Autocomplete
     */
    suspend fun searchPlaces(query: String, countryCode: String = "in"): List<PlaceResult> = withContext(Dispatchers.IO) {
        try {
            if (query.length < 2) return@withContext emptyList()
            
            // For areas without Google API key, use fallback search
            if (GOOGLE_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
                return@withContext fallbackLocationSearch(query)
            }
            
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$PLACES_API_BASE_URL/autocomplete/json?" +
                    "input=$encodedQuery&" +
                    "types=(cities)&" +
                    "components=country:$countryCode&" +
                    "key=$GOOGLE_API_KEY"
            
            val response = makeHttpRequest(url)
            parsePlacesResponse(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Places search failed: ${e.message}")
            fallbackLocationSearch(query)
        }
    }
    
    /**
     * Get detailed location information including weather data
     */
    suspend fun getLocationInfo(latitude: Double, longitude: Double): LocationInfo = withContext(Dispatchers.IO) {
        try {
            // Get address from coordinates
            val addressInfo = reverseGeocode(latitude, longitude)
            
            // Get weather data
            val weatherData = getWeatherDataForLocation(latitude, longitude)
            
            LocationInfo(
                latitude = latitude,
                longitude = longitude,
                address = addressInfo.address,
                city = addressInfo.city,
                state = addressInfo.state,
                country = addressInfo.country,
                temperature = weatherData?.get("temperature"),
                humidity = weatherData?.get("humidity"),
                rainfall = weatherData?.get("rainfall")
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location info: ${e.message}")
            LocationInfo(
                latitude = latitude,
                longitude = longitude,
                address = "Unknown Location",
                city = "Unknown",
                temperature = 25.0, // Fallback values
                humidity = 65.0,
                rainfall = 800.0
            )
        }
    }
    
    /**
     * Get place details by place ID
     */
    suspend fun getPlaceDetails(placeId: String): PlaceResult? = withContext(Dispatchers.IO) {
        try {
            if (GOOGLE_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
                return@withContext null
            }
            
            val url = "$PLACES_API_BASE_URL/details/json?" +
                    "place_id=$placeId&" +
                    "fields=name,formatted_address,geometry&" +
                    "key=$GOOGLE_API_KEY"
            
            val response = makeHttpRequest(url)
            parsePlaceDetailsResponse(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Place details failed: ${e.message}")
            null
        }
    }
    
    /**
     * Reverse geocode coordinates to address
     */
    private suspend fun reverseGeocode(lat: Double, lng: Double): LocationInfo {
        try {
            if (GOOGLE_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
                return LocationInfo(lat, lng, "Location", "City", "State", "India")
            }
            
            val url = "$GEOCODING_API_BASE_URL?" +
                    "latlng=$lat,$lng&" +
                    "key=$GOOGLE_API_KEY"
            
            val response = makeHttpRequest(url)
            return parseGeocodingResponse(response, lat, lng)
            
        } catch (e: Exception) {
            Log.e(TAG, "Reverse geocoding failed: ${e.message}")
            return LocationInfo(lat, lng, "Unknown Location", "Unknown")
        }
    }
    
    /**
     * Get weather data for location coordinates
     */
    private suspend fun getWeatherDataForLocation(lat: Double, lng: Double): Map<String, Double>? {
        return try {
            // Use our existing weather service integration
            val command = "python weather_service.py --lat $lat --lng $lng --quick"
            val processBuilder = ProcessBuilder("bash", "-c", command)
            processBuilder.directory(File("/home/wizardking/Documents/Projects/SIHv2/SIH25"))
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            
            // Parse weather response
            val json = JSONObject(output)
            if (json.optBoolean("success", false)) {
                val current = json.optJSONObject("current_weather")
                current?.let {
                    mapOf(
                        "temperature" to it.optDouble("temperature", 25.0),
                        "humidity" to it.optDouble("relative_humidity", 65.0),
                        "rainfall" to it.optDouble("precipitation", 0.0)
                    )
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Weather fetch failed, using defaults: ${e.message}")
            null
        }
    }
    
    /**
     * Fallback location search for areas without Google API
     */
    private fun fallbackLocationSearch(query: String): List<PlaceResult> {
        val commonCities = mapOf(
            "delhi" to PlaceResult("delhi", "Delhi", "Delhi, India", 28.7041, 77.1025),
            "mumbai" to PlaceResult("mumbai", "Mumbai", "Mumbai, Maharashtra, India", 19.0760, 72.8777),
            "bangalore" to PlaceResult("bangalore", "Bangalore", "Bangalore, Karnataka, India", 12.9716, 77.5946),
            "chennai" to PlaceResult("chennai", "Chennai", "Chennai, Tamil Nadu, India", 13.0827, 80.2707),
            "kolkata" to PlaceResult("kolkata", "Kolkata", "Kolkata, West Bengal, India", 22.5726, 88.3639),
            "hyderabad" to PlaceResult("hyderabad", "Hyderabad", "Hyderabad, Telangana, India", 17.3850, 78.4867),
            "pune" to PlaceResult("pune", "Pune", "Pune, Maharashtra, India", 18.5204, 73.8567),
            "ahmedabad" to PlaceResult("ahmedabad", "Ahmedabad", "Ahmedabad, Gujarat, India", 23.0225, 72.5714),
            "jaipur" to PlaceResult("jaipur", "Jaipur", "Jaipur, Rajasthan, India", 26.9124, 75.7873),
            "surat" to PlaceResult("surat", "Surat", "Surat, Gujarat, India", 21.1702, 72.8311),
            "lucknow" to PlaceResult("lucknow", "Lucknow", "Lucknow, Uttar Pradesh, India", 26.8467, 80.9462),
            "kanpur" to PlaceResult("kanpur", "Kanpur", "Kanpur, Uttar Pradesh, India", 26.4499, 80.3319),
            "nagpur" to PlaceResult("nagpur", "Nagpur", "Nagpur, Maharashtra, India", 21.1458, 79.0882),
            "patna" to PlaceResult("patna", "Patna", "Patna, Bihar, India", 25.5941, 85.1376),
            "indore" to PlaceResult("indore", "Indore", "Indore, Madhya Pradesh, India", 22.7196, 75.8577),
            "bhopal" to PlaceResult("bhopal", "Bhopal", "Bhopal, Madhya Pradesh, India", 23.2599, 77.4126),
            "agra" to PlaceResult("agra", "Agra", "Agra, Uttar Pradesh, India", 27.1767, 78.0081),
            "varanasi" to PlaceResult("varanasi", "Varanasi", "Varanasi, Uttar Pradesh, India", 25.3176, 82.9739),
            "amritsar" to PlaceResult("amritsar", "Amritsar", "Amritsar, Punjab, India", 31.6340, 74.8723),
            "chandigarh" to PlaceResult("chandigarh", "Chandigarh", "Chandigarh, India", 30.7333, 76.7794),
            "kochi" to PlaceResult("kochi", "Kochi", "Kochi, Kerala, India", 9.9312, 76.2673),
            "thiruvananthapuram" to PlaceResult("thiruvananthapuram", "Thiruvananthapuram", "Thiruvananthapuram, Kerala, India", 8.5241, 76.9366),
            "coimbatore" to PlaceResult("coimbatore", "Coimbatore", "Coimbatore, Tamil Nadu, India", 11.0168, 76.9558),
            "madurai" to PlaceResult("madurai", "Madurai", "Madurai, Tamil Nadu, India", 9.9252, 78.1198),
            "visakhapatnam" to PlaceResult("visakhapatnam", "Visakhapatnam", "Visakhapatnam, Andhra Pradesh, India", 17.6868, 83.2185),
            "vijayawada" to PlaceResult("vijayawada", "Vijayawada", "Vijayawada, Andhra Pradesh, India", 16.5062, 80.6480),
            "bhubaneswar" to PlaceResult("bhubaneswar", "Bhubaneswar", "Bhubaneswar, Odisha, India", 20.2961, 85.8245),
            "guwahati" to PlaceResult("guwahati", "Guwahati", "Guwahati, Assam, India", 26.1445, 91.7362),
            "dehradun" to PlaceResult("dehradun", "Dehradun", "Dehradun, Uttarakhand, India", 30.3165, 78.0322),
            "ranchi" to PlaceResult("ranchi", "Ranchi", "Ranchi, Jharkhand, India", 23.3441, 85.3096)
        )
        
        val lowerQuery = query.lowercase()
        return commonCities.filter { (key, _) ->
            key.contains(lowerQuery) || lowerQuery.contains(key)
        }.values.toList()
    }
    
    /**
     * Make HTTP request
     */
    private fun makeHttpRequest(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream.bufferedReader().readText()
            } else {
                throw IOException("HTTP error code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Parse Places API response
     */
    private fun parsePlacesResponse(response: String): List<PlaceResult> {
        val json = JSONObject(response)
        val predictions = json.optJSONArray("predictions") ?: return emptyList()
        
        val results = mutableListOf<PlaceResult>()
        for (i in 0 until predictions.length()) {
            val prediction = predictions.getJSONObject(i)
            results.add(
                PlaceResult(
                    placeId = prediction.getString("place_id"),
                    name = prediction.getJSONObject("structured_formatting").getString("main_text"),
                    address = prediction.getString("description"),
                    latitude = 0.0, // Will be filled by place details call
                    longitude = 0.0
                )
            )
        }
        return results
    }
    
    /**
     * Parse Place Details response
     */
    private fun parsePlaceDetailsResponse(response: String): PlaceResult? {
        val json = JSONObject(response)
        val result = json.optJSONObject("result") ?: return null
        
        val geometry = result.getJSONObject("geometry")
        val location = geometry.getJSONObject("location")
        
        return PlaceResult(
            placeId = "",
            name = result.getString("name"),
            address = result.getString("formatted_address"),
            latitude = location.getDouble("lat"),
            longitude = location.getDouble("lng")
        )
    }
    
    /**
     * Parse Geocoding response
     */
    private fun parseGeocodingResponse(response: String, lat: Double, lng: Double): LocationInfo {
        val json = JSONObject(response)
        val results = json.optJSONArray("results")
        
        if (results != null && results.length() > 0) {
            val result = results.getJSONObject(0)
            val components = result.getJSONArray("address_components")
            
            var city = ""
            var state = ""
            var country = ""
            
            for (i in 0 until components.length()) {
                val component = components.getJSONObject(i)
                val types = component.getJSONArray("types")
                
                for (j in 0 until types.length()) {
                    when (types.getString(j)) {
                        "locality", "administrative_area_level_2" -> {
                            if (city.isEmpty()) city = component.getString("long_name")
                        }
                        "administrative_area_level_1" -> {
                            state = component.getString("long_name")
                        }
                        "country" -> {
                            country = component.getString("long_name")
                        }
                    }
                }
            }
            
            return LocationInfo(
                latitude = lat,
                longitude = lng,
                address = result.getString("formatted_address"),
                city = city.ifEmpty { "Unknown" },
                state = state,
                country = country
            )
        }
        
        return LocationInfo(lat, lng, "Unknown Location", "Unknown")
    }
}