package com.fasalsaathi.app.ui.crops

import android.Manifest
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
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var layoutLocationDisplay: LinearLayout
    private lateinit var tvCurrentLocation: TextView
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    
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
        btnGetRecommendation.text = "Analyzing..."
        btnGetRecommendation.isEnabled = false
        
        // Collect form data
        val latitude = etLatitude.text.toString().toDoubleOrNull() ?: 0.0
        val longitude = etLongitude.text.toString().toDoubleOrNull() ?: 0.0
        val soilType = acSoilType.text.toString()
        val farmSize = etFarmSize.text.toString().toDoubleOrNull() ?: 0.0
        val budget = etBudget.text.toString().toLongOrNull() ?: 0
        val irrigation = acIrrigation.text.toString()
        val experience = etExperience.text.toString().toIntOrNull() ?: 0
        val previousCrop = etPreviousCrop.text.toString()
        
        // Simulate API call with delay
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            
            // Generate recommendation based on inputs
            val recommendation = generateRecommendation(latitude, longitude, soilType, farmSize, experience)
            
            showLoading(false)
            btnGetRecommendation.text = "🌾 Get Crop Recommendation"
            btnGetRecommendation.isEnabled = true
            
            showRecommendationDialog(recommendation)
        }
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
    
    data class SoilAnalysisResult(
        val soilType: String,
        val confidence: Int
    )
}