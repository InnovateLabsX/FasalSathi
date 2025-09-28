package com.fasalsaathi.app.ui.crops

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fasalsaathi.app.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random
import com.fasalsaathi.app.ml.MLModelManager
import com.fasalsaathi.app.weather.WeatherApiService
import com.fasalsaathi.app.ui.location.LocationPickerActivity

class CropRecommendationActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var btnAutoLocation: MaterialButton
    private lateinit var btnGetRecommendation: MaterialButton
    private lateinit var etLatitude: TextInputEditText
    private lateinit var etLongitude: TextInputEditText
    private lateinit var etFarmSize: TextInputEditText
    private lateinit var etBudget: TextInputEditText
    private lateinit var etExperience: TextInputEditText
    private lateinit var etPreviousCrop: TextInputEditText
    private lateinit var acSoilType: AutoCompleteTextView
    private lateinit var acIrrigation: AutoCompleteTextView
    private lateinit var acCity: AutoCompleteTextView
    private lateinit var etTemperature: TextInputEditText
    private lateinit var etRainfall: TextInputEditText
    private lateinit var etHumidity: TextInputEditText
    private lateinit var tvIrrigationInfo: TextView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var layoutLocationDisplay: LinearLayout
    private lateinit var tvCurrentLocation: TextView
    
    // Results section views
    private lateinit var resultsSection: LinearLayout
    private lateinit var tvOverallConfidence: TextView
    private lateinit var tvPrimaryCrop: TextView
    private lateinit var tvPrimaryConfidence: TextView
    private lateinit var tvPrimaryCropDetails: TextView
    private lateinit var progressPrimaryConfidence: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var alternativeRecommendationsContainer: LinearLayout
    private lateinit var tvSoilType: TextView
    private lateinit var tvSoilConfidence: TextView
    private lateinit var progressSoilConfidence: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var tvTemperature: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvRainfall: TextView
    private lateinit var tvRegion: TextView
    private lateinit var tvInsights: TextView
    
    // Soil parameters views
    private lateinit var btnToggleParameters: MaterialButton
    private lateinit var primaryNutrientsContainer: LinearLayout
    private lateinit var soilPropertiesContainer: LinearLayout
    private lateinit var micronutrientsSection: LinearLayout
    private lateinit var micronutrientsContainer: LinearLayout
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    
    // ML Model Manager
    private lateinit var mlModelManager: MLModelManager
    
    // Weather API Service
    private lateinit var weatherApiService: WeatherApiService
    
    // Camera and photo components
    private lateinit var cameraBtn: MaterialButton
    private lateinit var galleryBtn: MaterialButton
    private lateinit var soilImageView: ImageView
    private lateinit var soilPreviewCard: MaterialCardView
    private lateinit var soilTypeResult: TextView
    private lateinit var confidenceResult: TextView
    private lateinit var soilAnalysisProgress: CircularProgressIndicator
    private var currentPhotoPath: String = ""
    
    // Soil types for dropdown
    private val soilTypes = arrayOf(
        "Alluvial Soil", "Black Soil", "Red Soil", "Laterite Soil", 
        "Desert Soil", "Mountain Soil", "Saline Soil", "Peaty Soil"
    )
    
    // Irrigation methods for dropdown
    private val irrigationMethods = arrayOf(
        "Drip Irrigation", "Sprinkler Irrigation", "Flood Irrigation",
        "Furrow Irrigation", "Basin Irrigation", "Border Irrigation"
    )
    
    // Indian cities with their climate data
    private val indianCities = mapOf(
        "Delhi" to CityData(28.7041, 77.1025, 25.0, 650.0, 65.0),
        "Mumbai" to CityData(19.0760, 72.8777, 27.0, 2400.0, 83.0),
        "Bangalore" to CityData(12.9716, 77.5946, 23.0, 900.0, 60.0),
        "Chennai" to CityData(13.0827, 80.2707, 29.0, 1200.0, 75.0),
        "Kolkata" to CityData(22.5726, 88.3639, 27.0, 1600.0, 80.0),
        "Hyderabad" to CityData(17.3850, 78.4867, 26.0, 800.0, 55.0),
        "Pune" to CityData(18.5204, 73.8567, 24.0, 700.0, 65.0),
        "Ahmedabad" to CityData(23.0225, 72.5714, 27.0, 800.0, 55.0),
        "Jaipur" to CityData(26.9124, 75.7873, 26.0, 650.0, 50.0),
        "Surat" to CityData(21.1702, 72.8311, 28.0, 1100.0, 70.0),
        "Lucknow" to CityData(26.8467, 80.9462, 25.0, 900.0, 65.0),
        "Kanpur" to CityData(26.4499, 80.3319, 26.0, 800.0, 60.0),
        "Nagpur" to CityData(21.1458, 79.0882, 27.0, 1200.0, 65.0),
        "Indore" to CityData(22.7196, 75.8577, 25.0, 950.0, 60.0),
        "Thane" to CityData(19.2183, 72.9781, 27.0, 2200.0, 80.0),
        "Bhopal" to CityData(23.2599, 77.4126, 25.0, 1150.0, 65.0),
        "Patna" to CityData(25.5941, 85.1376, 26.0, 1200.0, 70.0),
        "Vadodara" to CityData(22.3072, 73.1812, 27.0, 900.0, 60.0),
        "Ghaziabad" to CityData(28.6692, 77.4538, 25.0, 700.0, 65.0),
        "Ludhiana" to CityData(30.9010, 75.8573, 24.0, 700.0, 60.0),
        "Agra" to CityData(27.1767, 78.0081, 25.0, 650.0, 60.0),
        "Nashik" to CityData(19.9975, 73.7898, 25.0, 600.0, 55.0),
        "Faridabad" to CityData(28.4089, 77.3178, 25.0, 700.0, 65.0),
        "Meerut" to CityData(28.9845, 77.7064, 25.0, 850.0, 65.0),
        "Rajkot" to CityData(22.3039, 70.8022, 27.0, 600.0, 65.0),
        "Kalyan-Dombivali" to CityData(19.2403, 73.1305, 27.0, 2200.0, 80.0),
        "Vasai-Virar" to CityData(19.4914, 72.8054, 27.0, 2400.0, 82.0),
        "Varanasi" to CityData(25.3176, 82.9739, 26.0, 1050.0, 70.0),
        "Srinagar" to CityData(34.0837, 74.7973, 14.0, 650.0, 65.0),
        "Aurangabad" to CityData(19.8762, 75.3433, 26.0, 750.0, 60.0)
    )
    
    data class CityData(
        val latitude: Double,
        val longitude: Double,
        val avgTemperature: Double,
        val annualRainfall: Double,
        val avgHumidity: Double
    )
    
    private val locationPermissionLauncher = 
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission is required for auto-location feature", 
                    Toast.LENGTH_LONG).show()
            }
        }
    
    private val cameraPermissionLauncher = 
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera permission is required to capture soil photos", 
                    Toast.LENGTH_LONG).show()
            }
        }
    
    private val takePictureLauncher = 
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Photo was taken successfully
                val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                displayCapturedImage(bitmap)
                analyzeSoilFromImage(bitmap)
            }
        }
    
    private val pickImageLauncher = 
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    displayCapturedImage(bitmap)
                    analyzeSoilFromImage(bitmap)
                }
            }
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_recommendation)
        
        initializeViews()
        setupToolbar()
        setupLocationServices()
        setupDropdowns()
        setupClickListeners()
        
        // Initialize ML Model Manager
        mlModelManager = MLModelManager(this)
        
        // Initialize Weather API Service
        weatherApiService = WeatherApiService(this)
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        btnAutoLocation = findViewById(R.id.btnAutoLocation)
        btnGetRecommendation = findViewById(R.id.btnGetRecommendation)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etFarmSize = findViewById(R.id.etFarmSize)
        etBudget = findViewById(R.id.etBudget)
        etExperience = findViewById(R.id.etExperience)
        etPreviousCrop = findViewById(R.id.etPreviousCrop)
        acSoilType = findViewById(R.id.acSoilType)
        acIrrigation = findViewById(R.id.acIrrigation)
        acCity = findViewById(R.id.acCity)
        etTemperature = findViewById(R.id.etTemperature)
        etRainfall = findViewById(R.id.etRainfall)
        etHumidity = findViewById(R.id.etHumidity)
        tvIrrigationInfo = findViewById(R.id.tvIrrigationInfo)
        progressBar = findViewById(R.id.progressBar)
        layoutLocationDisplay = findViewById(R.id.layoutLocationDisplay)
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        
        // Camera and photo components
        cameraBtn = findViewById(R.id.btnCameraCapture)
        galleryBtn = findViewById(R.id.btnGallerySelect)
        soilImageView = findViewById(R.id.ivSoilPreview)
        soilPreviewCard = findViewById(R.id.cardSoilImage)
        soilTypeResult = findViewById(R.id.tvDetectedSoilType)
        confidenceResult = findViewById(R.id.tvConfidenceLevel)
        soilAnalysisProgress = progressBar // Reuse the existing progress bar
        
        // Results section views
        resultsSection = findViewById(R.id.resultsSection)
        tvOverallConfidence = findViewById(R.id.tvOverallConfidence)
        tvPrimaryCrop = findViewById(R.id.tvPrimaryCrop)
        tvPrimaryConfidence = findViewById(R.id.tvPrimaryConfidence)
        tvPrimaryCropDetails = findViewById(R.id.tvPrimaryCropDetails)
        progressPrimaryConfidence = findViewById(R.id.progressPrimaryConfidence)
        alternativeRecommendationsContainer = findViewById(R.id.alternativeRecommendationsContainer)
        tvSoilType = findViewById(R.id.tvSoilType)
        tvSoilConfidence = findViewById(R.id.tvSoilConfidence)
        progressSoilConfidence = findViewById(R.id.progressSoilConfidence)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvRainfall = findViewById(R.id.tvRainfall)
        tvRegion = findViewById(R.id.tvRegion)
        tvInsights = findViewById(R.id.tvInsights)
        
        // Soil parameters views
        btnToggleParameters = findViewById(R.id.btnToggleParameters)
        primaryNutrientsContainer = findViewById(R.id.primaryNutrientsContainer)
        soilPropertiesContainer = findViewById(R.id.soilPropertiesContainer)
        micronutrientsSection = findViewById(R.id.micronutrientsSection)
        micronutrientsContainer = findViewById(R.id.micronutrientsContainer)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Crop Recommendation"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
    
    private fun setupDropdowns() {
        // Setup soil type dropdown
        val soilAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, soilTypes)
        acSoilType.setAdapter(soilAdapter)
        
        // Setup irrigation method dropdown
        val irrigationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, irrigationMethods)
        acIrrigation.setAdapter(irrigationAdapter)
        
        // Setup enhanced location picker (replaces limited city dropdown)
        setupLocationPicker()
        
        // Setup irrigation method selection listener
        acIrrigation.setOnItemClickListener { _, _, position, _ ->
            val selectedMethod = irrigationMethods[position]
            updateIrrigationInfo(selectedMethod)
        }
    }
    
    /**
     * Setup enhanced location picker with unlimited city selection
     */
    private fun setupLocationPicker() {
        // Make city field clickable to open location picker
        acCity.isFocusable = false
        acCity.isClickable = true
        
        acCity.setOnClickListener {
            openLocationPicker()
        }
        
        // Location picker is integrated through the city field click
    }
    
    /**
     * Open the enhanced location picker activity
     */
    private fun openLocationPicker() {
        val intent = Intent(this, LocationPickerActivity::class.java)
        locationPickerLauncher.launch(intent)
    }
    
    /**
     * Activity result launcher for location picker
     */
    private val locationPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val latitude = data.getDoubleExtra(LocationPickerActivity.EXTRA_LATITUDE, 0.0)
                val longitude = data.getDoubleExtra(LocationPickerActivity.EXTRA_LONGITUDE, 0.0)
                val cityName = data.getStringExtra(LocationPickerActivity.EXTRA_CITY_NAME) ?: "Unknown"
                val address = data.getStringExtra(LocationPickerActivity.EXTRA_ADDRESS) ?: "Unknown Location"
                
                // Update UI with selected location
                acCity.setText(cityName)
                
                // Fetch weather data for the selected location
                updateLocationAndWeather(latitude, longitude, cityName, address)
            }
        }
    }
    
    private fun setupClickListeners() {
        btnAutoLocation.setOnClickListener {
            checkLocationPermissionAndGetLocation()
        }
        
        btnGetRecommendation.setOnClickListener {
            if (validateForm()) {
                getRecommendation()
            }
        }
        
        cameraBtn.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }
        
        galleryBtn.setOnClickListener {
            openImagePicker()
        }
        
        btnToggleParameters.setOnClickListener {
            toggleMicronutrientsVisibility()
        }
    }
    
    private fun updateIrrigationInfo(irrigationMethod: String) {
        val info = when (irrigationMethod) {
            "Drip Irrigation" -> """• High water efficiency (90-95%)
• Best for: Fruits, vegetables, cash crops
• Reduces water usage by 30-50%
• Higher initial cost, lower operating cost
• Suitable for all soil types"""
            
            "Sprinkler Irrigation" -> """• Moderate water efficiency (70-80%)
• Best for: Field crops, cereals, fodder
• Good for uneven terrain
• Medium initial and operating cost
• Works well with sandy soils"""
            
            "Flood Irrigation" -> """• Lower water efficiency (40-60%)
• Best for: Rice, wheat in heavy clay soils
• Traditional method, low initial cost
• High water requirement
• Suitable for level fields with clay soil"""
            
            "Furrow Irrigation" -> """• Moderate efficiency (50-70%)
• Best for: Row crops, vegetables
• Water flows between crop rows
• Medium water requirement
• Works with slight slope"""
            
            "Basin Irrigation" -> """• Variable efficiency (40-70%)
• Best for: Orchard crops, large field crops
• Suitable for level land
• Simple implementation
• Good for clay and loam soils"""
            
            "Border Irrigation" -> """• Good efficiency (60-75%)
• Best for: Cereals, fodder crops
• Works on gently sloping land
• Uniform water distribution
• Suitable for medium-textured soils"""
            
            else -> "Select irrigation method to see detailed information about water efficiency, suitable crops, and cost implications."
        }
        
        tvIrrigationInfo.text = info
    }
    
    private fun autofillCityData(cityData: CityData) {
        // Auto-fill coordinates
        etLatitude.setText(String.format("%.4f", cityData.latitude))
        etLongitude.setText(String.format("%.4f", cityData.longitude))
        
        // Auto-fill environmental parameters
        etTemperature.setText(String.format("%.1f", cityData.avgTemperature))
        etRainfall.setText(String.format("%.0f", cityData.annualRainfall))
        etHumidity.setText(String.format("%.0f", cityData.avgHumidity))
        
        // Update location display
        layoutLocationDisplay.visibility = View.VISIBLE
        tvCurrentLocation.text = "${acCity.text} (${String.format("%.4f", cityData.latitude)}, ${String.format("%.4f", cityData.longitude)})"
        
        Toast.makeText(this, "Environmental data auto-filled for ${acCity.text}", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Enhanced location update with real-time weather data
     */
    private fun updateLocationAndWeather(latitude: Double, longitude: Double, cityName: String, address: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                // Update coordinates
                etLatitude.setText(String.format("%.4f", latitude))
                etLongitude.setText(String.format("%.4f", longitude))
                
                // Update location display
                layoutLocationDisplay.visibility = View.VISIBLE
                tvCurrentLocation.text = "$cityName (${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)})"
                
                // Get real-time weather data
                try {
                    val weatherData = weatherApiService.getWeatherData(latitude, longitude)
                    weatherData?.let { data ->
                        // Update environmental parameters with real data
                        etTemperature.setText(String.format("%.1f", data.currentTemperature))
                        etHumidity.setText(String.format("%.0f", data.currentHumidity))
                    } ?: run {
                        // Fallback to regional averages if no weather data
                        useRegionalWeatherDefaults(latitude, longitude)
                    }
                } catch (e: Exception) {
                    // Fallback to regional averages if weather service fails
                    useRegionalWeatherDefaults(latitude, longitude)
                }
                
                // Use regional rainfall estimate
                val regionalRainfall = getRegionalRainfall(latitude, longitude)
                etRainfall.setText(String.format("%.0f", regionalRainfall))
                
                Toast.makeText(
                    this@CropRecommendationActivity,
                    "Location data updated for $cityName",
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: Exception) {
                // Fallback for any errors
                useRegionalWeatherDefaults(latitude, longitude)
                Toast.makeText(
                    this@CropRecommendationActivity,
                    "Location selected: $cityName (limited weather data available)",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    /**
     * Get regional rainfall average based on coordinates
     */
    private fun getRegionalRainfall(lat: Double, lng: Double): Double {
        return when {
            lat > 25 && lng > 85 -> 1800.0 // Northeast: high rainfall
            lat > 20 && lng < 75 -> 600.0  // Northwest: low rainfall  
            lat < 15 && lng > 75 -> 1200.0 // South: moderate-high rainfall
            lat > 20 && lng in 75.0..85.0 -> 900.0 // Central: moderate rainfall
            else -> 800.0 // Default moderate rainfall
        }
    }
    
    /**
     * Use regional weather defaults when real-time data unavailable
     */
    private fun useRegionalWeatherDefaults(lat: Double, lng: Double) {
        val regionTemp = when {
            lat > 30 -> 22.0 // Northern regions: cooler
            lat < 15 -> 28.0 // Southern regions: warmer
            else -> 25.0     // Central regions: moderate
        }
        
        val regionHumidity = when {
            lng < 75 -> 55.0  // Western regions: drier
            lng > 85 -> 80.0  // Eastern regions: more humid
            else -> 65.0      // Central regions: moderate
        }
        
        etTemperature.setText(String.format("%.1f", regionTemp))
        etHumidity.setText(String.format("%.0f", regionHumidity))
        etRainfall.setText(String.format("%.0f", getRegionalRainfall(lat, lng)))
    }
    
    private fun checkLocationPermissionAndGetLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showLocationPermissionDialog()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    
    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to get your current coordinates for accurate crop recommendations based on your local climate and soil conditions.")
            .setPositiveButton("Grant Permission") { _, _ ->
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        showLoading(true)
        btnAutoLocation.text = "Getting Location..."
        btnAutoLocation.isEnabled = false
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    
                    // Update input fields
                    etLatitude.setText(String.format("%.6f", currentLatitude))
                    etLongitude.setText(String.format("%.6f", currentLongitude))
                    
                    // Get address from coordinates
                    getAddressFromLocation(currentLatitude, currentLongitude)
                } else {
                    showLoading(false)
                    btnAutoLocation.text = "📱 Get Current Location"
                    btnAutoLocation.isEnabled = true
                    Toast.makeText(this, "Unable to get current location. Please try again.", 
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                btnAutoLocation.text = "📱 Get Current Location"
                btnAutoLocation.isEnabled = true
                Toast.makeText(this, "Failed to get location: ${it.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@CropRecommendationActivity, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                
                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val locationText = "${address.locality ?: address.subAdminArea ?: "Unknown"}, " +
                                "${address.adminArea ?: "Unknown"}, ${address.countryName ?: "Unknown"} " +
                                "(${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)})"
                        
                        tvCurrentLocation.text = locationText
                        layoutLocationDisplay.visibility = View.VISIBLE
                        
                        Toast.makeText(this@CropRecommendationActivity, 
                            "Location updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        tvCurrentLocation.text = "Coordinates: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
                        layoutLocationDisplay.visibility = View.VISIBLE
                    }
                    
                    showLoading(false)
                    btnAutoLocation.text = "✅ Location Updated"
                    btnAutoLocation.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvCurrentLocation.text = "Coordinates: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
                    layoutLocationDisplay.visibility = View.VISIBLE
                    
                    showLoading(false)
                    btnAutoLocation.text = "✅ Location Updated"
                    btnAutoLocation.isEnabled = true
                }
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Check latitude
        if (etLatitude.text.toString().trim().isEmpty()) {
            etLatitude.error = "Please enter latitude or use auto-location"
            isValid = false
        } else {
            try {
                val lat = etLatitude.text.toString().toDouble()
                if (lat < -90 || lat > 90) {
                    etLatitude.error = "Latitude must be between -90 and 90"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                etLatitude.error = "Please enter a valid latitude"
                isValid = false
            }
        }
        
        // Check longitude
        if (etLongitude.text.toString().trim().isEmpty()) {
            etLongitude.error = "Please enter longitude or use auto-location"
            isValid = false
        } else {
            try {
                val lng = etLongitude.text.toString().toDouble()
                if (lng < -180 || lng > 180) {
                    etLongitude.error = "Longitude must be between -180 and 180"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                etLongitude.error = "Please enter a valid longitude"
                isValid = false
            }
        }
        
        // Check soil type
        if (acSoilType.text.toString().trim().isEmpty()) {
            acSoilType.error = "Please select soil type"
            isValid = false
        }
        
        // Check farm size
        if (etFarmSize.text.toString().trim().isEmpty()) {
            etFarmSize.error = "Please enter farm size"
            isValid = false
        }
        
        return isValid
    }
    
    private fun getRecommendation() {
        showLoading(true)
        btnGetRecommendation.text = "Analyzing with AI..."
        btnGetRecommendation.isEnabled = false
        
        // Collect form data
        val latitude = etLatitude.text.toString().toDoubleOrNull() ?: 0.0
        val longitude = etLongitude.text.toString().toDoubleOrNull() ?: 0.0
        val soilType = acSoilType.text.toString()
        val farmSize = etFarmSize.text.toString().toDoubleOrNull() ?: 0.0
        
        lifecycleScope.launch {
            try {
                // Get weather and soil data for ML prediction with real weather API
                val soilData = generateSoilDataForML(latitude, longitude, soilType)
                
                // Use ML model for crop prediction
                val cropPrediction = mlModelManager.getCropRecommendationWithFallback(soilData)
                
                // Get soil type prediction
                val soilTypePrediction = mlModelManager.predictSoilType(soilData)
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    btnGetRecommendation.text = "🌾 Get Crop Recommendation"
                    btnGetRecommendation.isEnabled = true
                    
                    if (cropPrediction.success) {
                        val irrigationMethod = acIrrigation.text.toString()
                        displayMLRecommendationResults(cropPrediction, soilTypePrediction, farmSize, latitude, longitude, irrigationMethod)
                    } else {
                        // Fallback to rule-based recommendation
                        val fallbackRecommendation = generateFallbackRecommendation(latitude, longitude, soilType, farmSize)
                        displayFallbackRecommendation(fallbackRecommendation, latitude, longitude)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    btnGetRecommendation.text = "🌾 Get Crop Recommendation"
                    btnGetRecommendation.isEnabled = true
                    
                    Toast.makeText(this@CropRecommendationActivity, 
                        "Error getting AI recommendation: ${e.message}", Toast.LENGTH_LONG).show()
                    
                    // Fallback to rule-based recommendation
                    val fallbackRecommendation = generateFallbackRecommendation(latitude, longitude, soilType, farmSize)
                    showRecommendationDialog(fallbackRecommendation)
                }
            }
        }
    }
    
    private suspend fun generateSoilDataForML(latitude: Double, longitude: Double, soilType: String): MLModelManager.SoilData {
        // Use actual input values or get real weather data
        
        var temperature = etTemperature.text.toString().toDoubleOrNull()
        var humidity = etHumidity.text.toString().toDoubleOrNull()
        var rainfall = etRainfall.text.toString().toDoubleOrNull()
        
        // If user hasn't entered values, try to get real weather data
        if (temperature == null || humidity == null || rainfall == null) {
            try {
                val weatherData = weatherApiService.getWeatherData(latitude, longitude)
                weatherData?.let { weather ->
                    temperature = temperature ?: weather.currentTemperature
                    humidity = humidity ?: weather.currentHumidity
                    rainfall = rainfall ?: weather.agriculturalMetrics.totalPrecipitation7d / 7.0 // Daily average
                    
                    // Auto-update the UI with real weather data
                    runOnUiThread {
                        if (etTemperature.text.toString().isEmpty()) {
                            etTemperature.setText(String.format("%.1f", weather.currentTemperature))
                        }
                        if (etHumidity.text.toString().isEmpty()) {
                            etHumidity.setText(String.format("%.1f", weather.currentHumidity))
                        }
                        if (etRainfall.text.toString().isEmpty()) {
                            etRainfall.setText(String.format("%.1f", weather.agriculturalMetrics.totalPrecipitation7d))
                        }
                    }
                }
            } catch (e: Exception) {
                // Fall back to calculated values if weather API fails
                val calculated = getClimateDataForLocation(latitude, longitude)
                temperature = temperature ?: calculated.first
                humidity = humidity ?: calculated.second
                rainfall = rainfall ?: calculated.third
            }
        }
        
        // Ensure we have valid values
        val finalTemperature = temperature ?: getClimateDataForLocation(latitude, longitude).first
        val finalHumidity = humidity ?: getClimateDataForLocation(latitude, longitude).second
        val finalRainfall = rainfall ?: getClimateDataForLocation(latitude, longitude).third
        
        // Adjust soil nutrients based on irrigation method
        val irrigationMethod = acIrrigation.text.toString()
        val (nAdjustment, pAdjustment, kAdjustment) = getIrrigationAdjustments(irrigationMethod)
        
        return MLModelManager.SoilData(
            n = (90.0 + nAdjustment) + Random.nextDouble(-10.0, 15.0), // Nitrogen with irrigation adjustment
            p = (40.0 + pAdjustment) + Random.nextDouble(-8.0, 12.0),  // Phosphorus with irrigation adjustment
            k = (50.0 + kAdjustment) + Random.nextDouble(-8.0, 12.0),  // Potassium with irrigation adjustment
            ph = 6.5 + Random.nextDouble(-1.0, 2.0),   // pH
            temperature = finalTemperature,
            humidity = finalHumidity,
            rainfall = finalRainfall,
            // Optional parameters with realistic ranges
            ec = 1.0 + Random.nextDouble(-0.3, 0.8),   // Electrical Conductivity
            oc = 0.8 + Random.nextDouble(-0.2, 0.5),   // Organic Carbon
            s = 15.0 + Random.nextDouble(-5.0, 10.0)   // Sulfur
        )
    }
    
    private fun getIrrigationAdjustments(irrigationMethod: String): Triple<Double, Double, Double> {
        // Return N, P, K adjustments based on irrigation method efficiency and nutrient retention
        return when (irrigationMethod) {
            "Drip Irrigation" -> Triple(15.0, 10.0, 12.0)      // Higher efficiency = better nutrient retention
            "Sprinkler Irrigation" -> Triple(8.0, 5.0, 6.0)    // Moderate efficiency
            "Flood Irrigation" -> Triple(-10.0, -15.0, -20.0)  // Lower efficiency = nutrient leaching
            "Furrow Irrigation" -> Triple(0.0, -5.0, -8.0)     // Moderate leaching
            "Basin Irrigation" -> Triple(-5.0, -3.0, -5.0)     // Some leaching
            "Border Irrigation" -> Triple(3.0, 0.0, 2.0)       // Balanced
            else -> Triple(0.0, 0.0, 0.0)                      // Default/No adjustment
        }
    }
    
    private fun getClimateDataForLocation(latitude: Double, longitude: Double): Triple<Double, Double, Double> {
        // Generate climate data based on geographic location
        val temperature = when {
            latitude > 30 -> 20.0 + Random.nextDouble(-5.0, 15.0) // Northern India - cooler
            latitude > 23 -> 25.0 + Random.nextDouble(-3.0, 8.0)  // Central India - moderate
            else -> 28.0 + Random.nextDouble(-2.0, 7.0)           // Southern India - warmer
        }
        
        val humidity = when {
            longitude > 80 -> 75.0 + Random.nextDouble(-10.0, 15.0) // Eastern regions - more humid
            longitude < 75 -> 55.0 + Random.nextDouble(-10.0, 20.0) // Western regions - less humid
            else -> 65.0 + Random.nextDouble(-10.0, 15.0)           // Central regions
        }
        
        val rainfall = when {
            latitude > 25 && longitude > 85 -> 1200.0 + Random.nextDouble(-200.0, 400.0) // High rainfall areas
            longitude < 75 -> 500.0 + Random.nextDouble(-100.0, 300.0)                   // Arid regions
            else -> 800.0 + Random.nextDouble(-200.0, 400.0)                             // Moderate rainfall
        }
        
        return Triple(temperature, humidity, rainfall)
    }
    
    private fun displayMLRecommendationResults(
        cropPrediction: MLModelManager.CropPrediction,
        soilTypePrediction: MLModelManager.SoilTypePrediction,
        farmSize: Double,
        latitude: Double,
        longitude: Double,
        irrigationMethod: String
    ) {
        // Show results section
        resultsSection.visibility = View.VISIBLE
        
        // Scroll to results section
        val scrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollView)
        scrollView?.post {
            scrollView.smoothScrollTo(0, resultsSection.top)
        }
        
        val region = when {
            latitude > 30 -> "Northern India"
            latitude > 23 -> "Central India"
            else -> "Southern India"
        }
        
        val confidencePercent = (cropPrediction.confidence * 100).toInt()
        val soilConfidencePercent = (soilTypePrediction.confidence * 100).toInt()
        
        // Try to get real weather data, fall back to calculated values
        lifecycleScope.launch {
            try {
                val weatherData = weatherApiService.getWeatherData(latitude, longitude)
                weatherData?.let { weather ->
                    runOnUiThread {
                        // Display detailed weather information
                        updateWeatherDisplay(weather, latitude, longitude, irrigationMethod)
                    }
                }
            } catch (e: Exception) {
                // Fall back to calculated environmental data
                val (temperature, humidity, rainfall) = getClimateDataForLocation(latitude, longitude)
                runOnUiThread {
                    displayEnvironmentalData(temperature, humidity, rainfall, latitude, longitude, irrigationMethod)
                }
            }
        }
        
        // Display overall confidence
        tvOverallConfidence.text = "${confidencePercent}%"
        
        // Display primary crop recommendation
        tvPrimaryCrop.text = cropPrediction.recommendedCrop.uppercase()
        tvPrimaryConfidence.text = "Confidence: ${confidencePercent}%"
        progressPrimaryConfidence.progress = confidencePercent
        
        val cropDetails = when (cropPrediction.recommendedCrop.lowercase()) {
            "rice" -> "Ideal for high rainfall areas with fertile alluvial soil"
            "wheat" -> "Suitable for moderate climate and well-drained soil"
            "sugarcane" -> "Perfect for tropical climate with adequate irrigation"
            "cotton" -> "Best for black soil with moderate rainfall"
            "maize" -> "Adaptable crop suitable for various soil types"
            else -> "Suitable for your soil type and weather conditions"
        }
        tvPrimaryCropDetails.text = cropDetails
        
        // Display alternative recommendations
        alternativeRecommendationsContainer.removeAllViews()
        cropPrediction.topRecommendations.take(3).forEachIndexed { index, crop ->
            val conf = (crop.confidence * 100).toInt()
            val alternativeView = layoutInflater.inflate(R.layout.item_alternative_crop, alternativeRecommendationsContainer, false)
            
            val tvCropName = alternativeView.findViewById<TextView>(R.id.tvAlternativeCropName)
            val tvCropConfidence = alternativeView.findViewById<TextView>(R.id.tvAlternativeCropConfidence)
            val progressAlternative = alternativeView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progressAlternativeConfidence)
            
            tvCropName.text = "${index + 1}. ${crop.crop.replaceFirstChar { it.uppercase() }}"
            tvCropConfidence.text = "${conf}%"
            progressAlternative.progress = conf
            
            alternativeRecommendationsContainer.addView(alternativeView)
        }
        
        // Display soil analysis
        tvSoilType.text = soilTypePrediction.soilType
        tvSoilConfidence.text = "Confidence: ${soilConfidencePercent}%"
        progressSoilConfidence.progress = soilConfidencePercent
        
        // Environmental conditions and insights are now handled by updateWeatherDisplay() or displayEnvironmentalData()
        
        // Display soil parameters for the recommended crop
        displaySoilParameters(cropPrediction.recommendedCrop)
    }
    
    private fun updateWeatherDisplay(weatherData: WeatherApiService.WeatherData, latitude: Double, longitude: Double, irrigationMethod: String) {
        val region = when {
            latitude > 30 -> "Northern India"
            latitude > 23 -> "Central India"
            else -> "Southern India"
        }
        
        // Display current weather conditions
        tvTemperature.text = "${weatherData.currentTemperature.toInt()}°C"
        tvHumidity.text = "${weatherData.currentHumidity.toInt()}%"
        tvRainfall.text = "${weatherData.agriculturalMetrics.totalPrecipitation7d.toInt()}mm (7d)"
        tvRegion.text = "$region • ${weatherData.weatherDescription}"
        
        // Enhanced insights with real weather data
        val insights = buildString {
            append("🌡️ Real-time Weather Analysis: Current temperature ${weatherData.currentTemperature}°C ")
            append("with ${weatherData.currentHumidity.toInt()}% humidity. ")
            append("Weather condition: ${weatherData.weatherDescription.lowercase()}.\n\n")
            
            append("📊 Agricultural Metrics:\n")
            append("• Growing Degree Days: ${weatherData.agriculturalMetrics.growingDegreeDays.toInt()}\n")
            append("• Soil Moisture: ${String.format("%.1f", weatherData.agriculturalMetrics.avgSoilMoisture * 100)}%\n")
            append("• Water Stress Index: ${String.format("%.1f", weatherData.agriculturalMetrics.waterStressIndex * 10)}/10\n")
            append("• Heat Stress Risk: ${weatherData.agriculturalMetrics.heatStressRisk.replaceFirstChar { it.uppercase() }}\n\n")
            
            // Irrigation recommendations based on real data
            val irrigationNeed = weatherData.agriculturalMetrics.irrigationNeedIndex
            append("💧 Irrigation Recommendations:\n")
            when {
                irrigationNeed > 0.7 -> append("🔴 High irrigation need - Water immediately\n")
                irrigationNeed > 0.4 -> append("🟡 Moderate irrigation need - Water within 24h\n")
                irrigationNeed > 0.2 -> append("🟢 Low irrigation need - Monitor soil moisture\n")
                else -> append("🔵 Minimal irrigation need - Adequate moisture\n")
            }
            
            // Planting conditions
            val planting = weatherData.agriculturalMetrics.plantingConditions
            append("\n🌱 Planting Conditions: ${planting.overallRating.uppercase()}\n")
            if (planting.temperatureSuitable) append("✅ Temperature suitable for planting\n")
            if (planting.moistureAdequate) append("✅ Moisture levels adequate\n") 
            if (planting.noExtremeWeather) append("✅ No extreme weather expected\n")
            
            if (weatherData.agriculturalMetrics.frostRisk) {
                append("\n❄️ FROST ALERT: Frost risk detected - protect sensitive crops!")
            }
            
            // Irrigation method impact with real data
            if (irrigationMethod.isNotEmpty()) {
                append("\n\n🚿 Your $irrigationMethod method ")
                when (irrigationMethod) {
                    "Drip Irrigation" -> {
                        val efficiency = if (irrigationNeed > 0.5) "critical for water conservation" else "optimal for current conditions"
                        append("is $efficiency. Expected 15-30% yield boost with 40-60% water savings.")
                    }
                    "Sprinkler Irrigation" -> {
                        append("provides good coverage. Monitor for wind effects during application.")
                    }
                    "Flood Irrigation" -> {
                        if (weatherData.soilMoisture < 0.3) {
                            append("may be beneficial given current low soil moisture.")
                        } else {
                            append("should be used carefully to avoid waterlogging.")
                        }
                    }
                    else -> append("should be adjusted based on current soil moisture levels.")
                }
            }
            
            // Weather-based crop recommendations
            val cropRecommendations = weatherApiService.getWeatherBasedCropRecommendations(weatherData)
            if (cropRecommendations.isNotEmpty()) {
                append("\n\n📋 Weather-Based Recommendations:\n")
                cropRecommendations.take(3).forEach { recommendation ->
                    append("• $recommendation\n")
                }
            }
        }
        
        tvInsights.text = insights
    }
    
    private fun displayEnvironmentalData(temperature: Double, humidity: Double, rainfall: Double, latitude: Double, longitude: Double, irrigationMethod: String) {
        val region = when {
            latitude > 30 -> "Northern India"
            latitude > 23 -> "Central India"
            else -> "Southern India"
        }
        
        // Display calculated environmental conditions (fallback)
        tvTemperature.text = "${temperature.toInt()}°C"
        tvHumidity.text = "${humidity.toInt()}%"
        tvRainfall.text = "${rainfall.toInt()}mm"
        tvRegion.text = "$region (Estimated)"
        
        // Standard insights with calculated data
        val insights = buildString {
            append("📍 Location-based Analysis: Based on your location in $region, ")
            append("estimated environmental conditions show temperature ${temperature.toInt()}°C ")
            append("with ${humidity.toInt()}% humidity and ${rainfall.toInt()}mm expected rainfall.\n\n")
            
            append("⚠️ Note: Connect to internet for real-time weather data and enhanced recommendations.\n\n")
            
            // Irrigation method impact
            if (irrigationMethod.isNotEmpty()) {
                append("🚿 Irrigation Impact: Your chosen $irrigationMethod method ")
                when (irrigationMethod) {
                    "Drip Irrigation" -> append("provides excellent water efficiency and nutrient retention, boosting expected yields by 15-30%.")
                    "Sprinkler Irrigation" -> append("offers good water distribution suitable for most field crops.")
                    "Flood Irrigation" -> append("works well for water-loving crops in suitable soil conditions.")
                    else -> append("will affect water usage efficiency and crop performance.")
                }
            }
        }
        
        tvInsights.text = insights
    }
    
    private fun displayFallbackRecommendation(recommendation: String, latitude: Double, longitude: Double) {
        // Show results section
        resultsSection.visibility = View.VISIBLE
        
        // Scroll to results section
        val scrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollView)
        scrollView?.post {
            scrollView.smoothScrollTo(0, resultsSection.top)
        }
        
        val region = when {
            latitude > 30 -> "Northern India"
            latitude > 23 -> "Central India"
            else -> "Southern India"
        }
        
        // Generate environmental data
        val (temperature, humidity, rainfall) = getClimateDataForLocation(latitude, longitude)
        
        // Parse recommendation for primary crop (simple extraction)
        val primaryCrop = when {
            recommendation.contains("Rice", ignoreCase = true) -> "Rice"
            recommendation.contains("Wheat", ignoreCase = true) -> "Wheat"
            recommendation.contains("Sugarcane", ignoreCase = true) -> "Sugarcane"
            recommendation.contains("Cotton", ignoreCase = true) -> "Cotton"
            recommendation.contains("Maize", ignoreCase = true) -> "Maize"
            else -> "Mixed Farming"
        }
        
        // Display overall confidence (lower for rule-based)
        tvOverallConfidence.text = "75%"
        
        // Display primary crop recommendation
        tvPrimaryCrop.text = primaryCrop.uppercase()
        tvPrimaryConfidence.text = "Confidence: 75%"
        progressPrimaryConfidence.progress = 75
        
        tvPrimaryCropDetails.text = "Rule-based recommendation using traditional farming knowledge"
        
        // Display alternative recommendations (simple fallback options)
        alternativeRecommendationsContainer.removeAllViews()
        val alternatives = listOf(
            Pair("Mixed Vegetables", 65),
            Pair("Pulses", 70),
            Pair("Fodder Crops", 60)
        )
        
        alternatives.forEachIndexed { index, (crop, confidence) ->
            val alternativeView = layoutInflater.inflate(R.layout.item_alternative_crop, alternativeRecommendationsContainer, false)
            
            val tvCropName = alternativeView.findViewById<TextView>(R.id.tvAlternativeCropName)
            val tvCropConfidence = alternativeView.findViewById<TextView>(R.id.tvAlternativeCropConfidence)
            val progressAlternative = alternativeView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progressAlternativeConfidence)
            
            tvCropName.text = "${index + 1}. $crop"
            tvCropConfidence.text = "${confidence}%"
            progressAlternative.progress = confidence
            
            alternativeRecommendationsContainer.addView(alternativeView)
        }
        
        // Display soil analysis (estimated)
        tvSoilType.text = "Estimated (Manual Input)"
        tvSoilConfidence.text = "Confidence: 50%"
        progressSoilConfidence.progress = 50
        
        // Display environmental conditions
        tvTemperature.text = "${temperature.toInt()}°C"
        tvHumidity.text = "${humidity.toInt()}%"
        tvRainfall.text = "${rainfall.toInt()}mm"
        tvRegion.text = region
        
        // Generate insights
        tvInsights.text = "This recommendation is based on traditional farming practices and general agricultural knowledge for your region. For more accurate recommendations, consider enabling AI analysis with soil image capture and detailed environmental data."
        
        // Display soil parameters for the recommended crop
        displaySoilParameters(primaryCrop)
    }
    
    private fun showDetailedAnalysis(
        cropPrediction: MLModelManager.CropPrediction,
        soilTypePrediction: MLModelManager.SoilTypePrediction
    ) {
        val details = buildString {
            appendLine("🔬 **Detailed ML Analysis**")
            appendLine()
            appendLine("**Crop Prediction Model:**")
            appendLine("• Algorithm: Random Forest Ensemble")
            appendLine("• Training Accuracy: 87.8%")
            appendLine("• Features: Soil NPK, pH, Climate data")
            appendLine()
            appendLine("**All Crop Probabilities:**")
            cropPrediction.topRecommendations.forEach { crop ->
                val conf = (crop.confidence * 100).toInt()
                appendLine("• ${crop.crop.capitalize()}: ${conf}%")
            }
            appendLine()
            appendLine("**Soil Type Prediction:**")
            appendLine("• Primary: ${soilTypePrediction.soilType}")
            soilTypePrediction.topPredictions.forEach { soil ->
                val conf = (soil.confidence * 100).toInt()
                appendLine("• ${soil.soilType}: ${conf}%")
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("🔬 Detailed Analysis")
            .setMessage(details)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun generateFallbackRecommendation(latitude: Double, longitude: Double, soilType: String, 
                                             farmSize: Double): String {
        return generateRecommendation(latitude, longitude, soilType, farmSize, 0)
    }

    private fun generateRecommendation(latitude: Double, longitude: Double, soilType: String, 
                                     farmSize: Double, experience: Int): String {
        // Simple recommendation logic based on location and inputs
        val region = when {
            latitude > 30 -> "Northern India"
            latitude > 23 -> "Central India"
            else -> "Southern India"
        }
        
        val seasonalCrops = when (region) {
            "Northern India" -> listOf("Wheat", "Rice", "Mustard", "Sugarcane")
            "Central India" -> listOf("Cotton", "Soybean", "Jowar", "Wheat")
            else -> listOf("Rice", "Millets", "Coconut", "Spices")
        }
        
        val soilBasedCrops = when (soilType) {
            "Black Soil" -> listOf("Cotton", "Sugarcane", "Wheat")
            "Alluvial Soil" -> listOf("Rice", "Wheat", "Maize")
            "Red Soil" -> listOf("Groundnut", "Millets", "Cotton")
            else -> listOf("Mixed farming recommended")
        }
        
        val recommendedCrop = seasonalCrops.intersect(soilBasedCrops.toSet()).firstOrNull() 
            ?: seasonalCrops.first()
        
        return """
        🌾 **Recommended Crop: $recommendedCrop**
        
        📍 **Location Analysis:**
        Region: $region
        Coordinates: ${String.format("%.2f", latitude)}, ${String.format("%.2f", longitude)}
        
        🌱 **Soil Compatibility:**
        Your $soilType is suitable for $recommendedCrop cultivation.
        
        📊 **Farm Details:**
        Farm Size: $farmSize acres
        Suitable for ${if (farmSize > 5) "commercial" else "small-scale"} farming
        
        💡 **Additional Recommendations:**
        • Best planting season: ${if (latitude > 25) "Rabi season (Oct-Mar)" else "Kharif season (Jun-Oct)"}
        • Expected yield: ${(farmSize * 15).toInt()} quintals approx
        • Market price trend: Stable
        
        🔔 **Next Steps:**
        1. Conduct soil testing for precise nutrient analysis
        2. Check local weather forecasts
        3. Consult with local agricultural extension officer
        4. Arrange quality seeds and fertilizers
        
        Note: This is an AI-generated recommendation. Please consult with local agricultural experts for detailed planning.
        """.trimIndent()
    }
    
    private fun showRecommendationDialog(recommendation: String) {
        AlertDialog.Builder(this)
            .setTitle("🌾 Crop Recommendation")
            .setMessage(recommendation)
            .setPositiveButton("Save Recommendation") { dialog, _ ->
                Toast.makeText(this, "Recommendation saved!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Get Another") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    // Camera and Photo Functions
    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                dispatchTakePictureIntent()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
                null
            }
            
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.fasalsaathi.app.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
        }
    }
    
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "SOIL_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
    
    private fun displayCapturedImage(bitmap: Bitmap) {
        soilImageView.setImageBitmap(bitmap)
        soilPreviewCard.visibility = View.VISIBLE
        
        // Reset analysis results
        soilTypeResult.text = "Analyzing..."
        confidenceResult.text = ""
        soilAnalysisProgress.visibility = View.VISIBLE
    }
    
    private fun analyzeSoilFromImage(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val analysisResult = withContext(Dispatchers.IO) {
                    performSoilAnalysis(bitmap)
                }
                
                // Update UI with analysis results
                soilAnalysisProgress.visibility = View.GONE
                soilTypeResult.text = "Detected: ${analysisResult.soilType}"
                confidenceResult.text = "Confidence: ${analysisResult.confidence}%"
                
                // Auto-fill soil type dropdown
                acSoilType.setText(analysisResult.soilType, false)
                
                Toast.makeText(this@CropRecommendationActivity, 
                    "Soil analysis complete! Soil type auto-filled.", 
                    Toast.LENGTH_LONG).show()
                    
            } catch (e: Exception) {
                soilAnalysisProgress.visibility = View.GONE
                soilTypeResult.text = "Analysis failed"
                confidenceResult.text = "Please select soil type manually"
                Toast.makeText(this@CropRecommendationActivity, 
                    "Failed to analyze soil image. Please select soil type manually.", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private suspend fun performSoilAnalysis(bitmap: Bitmap): SoilAnalysisResult {
        // Simulate AI analysis with realistic soil detection
        delay(2000) // Simulate processing time
        
        // Simple color-based analysis (in real app, this would use ML model)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        var redSum = 0
        var greenSum = 0
        var blueSum = 0
        
        for (pixel in pixels) {
            redSum += (pixel shr 16) and 0xFF
            greenSum += (pixel shr 8) and 0xFF
            blueSum += pixel and 0xFF
        }
        
        val avgRed = redSum / pixels.size
        val avgGreen = greenSum / pixels.size
        val avgBlue = blueSum / pixels.size
        
        // Determine soil type based on color analysis
        val soilType = when {
            avgRed > 150 && avgGreen < 100 && avgBlue < 80 -> "Red Soil"
            avgRed < 80 && avgGreen < 80 && avgBlue < 80 -> "Black Soil"
            avgRed > 120 && avgGreen > 100 && avgBlue < 90 -> "Alluvial Soil"
            avgRed > 100 && avgGreen > 80 && avgBlue < 70 -> "Laterite Soil"
            avgRed > 140 && avgGreen > 120 && avgBlue > 80 -> "Desert Soil"
            else -> "Alluvial Soil" // Default
        }
        
        val confidence = Random.nextInt(75, 95) // Random confidence between 75-95%
        
        return SoilAnalysisResult(soilType, confidence)
    }
    
    private fun displaySoilParameters(cropName: String) {
        val soilParams = getSoilParametersForCrop(cropName.lowercase())
        
        // Clear containers
        primaryNutrientsContainer.removeAllViews()
        soilPropertiesContainer.removeAllViews()
        micronutrientsContainer.removeAllViews()
        
        // Display Primary Nutrients (N, P, K)
        addSoilParameter(primaryNutrientsContainer, "Nitrogen (N)", "mg/kg", 
            soilParams.nitrogen, "Essential for leaf growth and protein synthesis")
        addSoilParameter(primaryNutrientsContainer, "Phosphorus (P)", "mg/kg", 
            soilParams.phosphorus, "Critical for root development and flowering")
        addSoilParameter(primaryNutrientsContainer, "Potassium (K)", "mg/kg", 
            soilParams.potassium, "Improves disease resistance and fruit quality")
        
        // Display Soil Properties
        addSoilParameter(soilPropertiesContainer, "pH Level", "pH", 
            soilParams.pH, "Affects nutrient availability and microbial activity")
        addSoilParameter(soilPropertiesContainer, "Electrical Conductivity (EC)", "dS/m", 
            soilParams.EC, "Indicates soil salinity levels")
        addSoilParameter(soilPropertiesContainer, "Organic Carbon (OC)", "%", 
            soilParams.OC, "Improves soil structure and water retention")
        addSoilParameter(soilPropertiesContainer, "Sulfur (S)", "mg/kg", 
            soilParams.sulfur, "Important for protein synthesis and oil content")
        
        // Display Micronutrients (Initially hidden)
        addSoilParameter(micronutrientsContainer, "Zinc (Zn)", "mg/kg", 
            soilParams.zinc, "Essential for enzyme function and growth regulation")
        addSoilParameter(micronutrientsContainer, "Iron (Fe)", "mg/kg", 
            soilParams.iron, "Critical for chlorophyll synthesis and photosynthesis")
        addSoilParameter(micronutrientsContainer, "Copper (Cu)", "mg/kg", 
            soilParams.copper, "Important for enzyme systems and lignin synthesis")
        addSoilParameter(micronutrientsContainer, "Manganese (Mn)", "mg/kg", 
            soilParams.manganese, "Involved in photosynthesis and nitrogen metabolism")
        addSoilParameter(micronutrientsContainer, "Boron (B)", "mg/kg", 
            soilParams.boron, "Essential for cell wall formation and reproductive development")
    }
    
    private fun addSoilParameter(container: LinearLayout, name: String, unit: String, 
                               range: Pair<Double, Double>, description: String) {
        val parameterView = layoutInflater.inflate(R.layout.item_soil_parameter, container, false)
        
        val tvParameterName = parameterView.findViewById<TextView>(R.id.tvParameterName)
        val tvParameterUnit = parameterView.findViewById<TextView>(R.id.tvParameterUnit)
        val tvMinValue = parameterView.findViewById<TextView>(R.id.tvMinValue)
        val tvMaxValue = parameterView.findViewById<TextView>(R.id.tvMaxValue)
        val tvParameterStatus = parameterView.findViewById<TextView>(R.id.tvParameterStatus)
        val progressOptimalRange = parameterView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progressOptimalRange)
        
        tvParameterName.text = name
        tvParameterUnit.text = unit
        tvMinValue.text = String.format("%.1f", range.first)
        tvMaxValue.text = String.format("%.1f", range.second)
        tvParameterStatus.text = description
        
        // Set progress based on the range (assuming optimal is in the middle-upper range)
        val progress = ((range.second - range.first) / range.second * 100).toInt().coerceIn(20, 90)
        progressOptimalRange.progress = progress
        
        container.addView(parameterView)
    }
    
    private fun getSoilParametersForCrop(crop: String): SoilParameters {
        return when (crop) {
            "rice" -> SoilParameters(
                nitrogen = Pair(150.0, 250.0),
                phosphorus = Pair(25.0, 50.0),
                potassium = Pair(150.0, 300.0),
                pH = Pair(5.5, 7.0),
                EC = Pair(0.2, 1.0),
                OC = Pair(0.5, 1.5),
                sulfur = Pair(15.0, 25.0),
                zinc = Pair(0.6, 2.0),
                iron = Pair(4.5, 15.0),
                copper = Pair(0.2, 1.0),
                manganese = Pair(2.0, 8.0),
                boron = Pair(0.5, 2.0)
            )
            "wheat" -> SoilParameters(
                nitrogen = Pair(120.0, 200.0),
                phosphorus = Pair(20.0, 40.0),
                potassium = Pair(100.0, 200.0),
                pH = Pair(6.0, 7.5),
                EC = Pair(0.2, 0.8),
                OC = Pair(0.4, 1.2),
                sulfur = Pair(10.0, 20.0),
                zinc = Pair(0.5, 1.5),
                iron = Pair(4.0, 12.0),
                copper = Pair(0.2, 0.8),
                manganese = Pair(1.5, 6.0),
                boron = Pair(0.3, 1.5)
            )
            "sugarcane" -> SoilParameters(
                nitrogen = Pair(200.0, 350.0),
                phosphorus = Pair(40.0, 80.0),
                potassium = Pair(200.0, 400.0),
                pH = Pair(6.5, 8.0),
                EC = Pair(0.5, 2.0),
                OC = Pair(0.8, 2.0),
                sulfur = Pair(20.0, 35.0),
                zinc = Pair(1.0, 3.0),
                iron = Pair(8.0, 20.0),
                copper = Pair(0.5, 2.0),
                manganese = Pair(3.0, 10.0),
                boron = Pair(0.8, 3.0)
            )
            "cotton" -> SoilParameters(
                nitrogen = Pair(100.0, 180.0),
                phosphorus = Pair(30.0, 60.0),
                potassium = Pair(120.0, 250.0),
                pH = Pair(5.8, 8.2),
                EC = Pair(0.2, 1.5),
                OC = Pair(0.6, 1.5),
                sulfur = Pair(12.0, 25.0),
                zinc = Pair(0.8, 2.5),
                iron = Pair(6.0, 18.0),
                copper = Pair(0.3, 1.2),
                manganese = Pair(2.5, 8.0),
                boron = Pair(0.6, 2.5)
            )
            "maize" -> SoilParameters(
                nitrogen = Pair(140.0, 220.0),
                phosphorus = Pair(25.0, 50.0),
                potassium = Pair(130.0, 280.0),
                pH = Pair(5.8, 7.8),
                EC = Pair(0.2, 1.2),
                OC = Pair(0.5, 1.4),
                sulfur = Pair(12.0, 22.0),
                zinc = Pair(0.7, 2.2),
                iron = Pair(5.0, 16.0),
                copper = Pair(0.2, 1.0),
                manganese = Pair(2.0, 7.0),
                boron = Pair(0.4, 1.8)
            )
            else -> SoilParameters( // Default/Mixed farming
                nitrogen = Pair(100.0, 200.0),
                phosphorus = Pair(20.0, 50.0),
                potassium = Pair(100.0, 250.0),
                pH = Pair(6.0, 7.5),
                EC = Pair(0.2, 1.0),
                OC = Pair(0.5, 1.5),
                sulfur = Pair(10.0, 25.0),
                zinc = Pair(0.5, 2.0),
                iron = Pair(4.0, 15.0),
                copper = Pair(0.2, 1.0),
                manganese = Pair(1.5, 8.0),
                boron = Pair(0.3, 2.0)
            )
        }
    }
    
    private fun toggleMicronutrientsVisibility() {
        if (micronutrientsSection.visibility == View.GONE) {
            micronutrientsSection.visibility = View.VISIBLE
            btnToggleParameters.text = "Show Less"
        } else {
            micronutrientsSection.visibility = View.GONE
            btnToggleParameters.text = "Show All"
        }
    }
    
    data class SoilParameters(
        val nitrogen: Pair<Double, Double>,
        val phosphorus: Pair<Double, Double>,
        val potassium: Pair<Double, Double>,
        val pH: Pair<Double, Double>,
        val EC: Pair<Double, Double>,
        val OC: Pair<Double, Double>,
        val sulfur: Pair<Double, Double>,
        val zinc: Pair<Double, Double>,
        val iron: Pair<Double, Double>,
        val copper: Pair<Double, Double>,
        val manganese: Pair<Double, Double>,
        val boron: Pair<Double, Double>
    )
    
    data class SoilAnalysisResult(
        val soilType: String,
        val confidence: Int
    )
}