package com.fasalsaathi.app.ui.location

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.fasalsaathi.app.utils.location.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationPickerActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_CITY_NAME = "city_name"
        const val EXTRA_ADDRESS = "address"
        const val REQUEST_CODE_LOCATION_PICKER = 1001
    }
    
    private lateinit var toolbar: Toolbar
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noResultsText: TextView
    private lateinit var currentLocationButton: Button
    
    private lateinit var locationService: LocationService
    private lateinit var adapter: LocationAdapter
    
    private var searchJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)
        
        initViews()
        setupToolbar()
        setupLocationService()
        setupRecyclerView()
        setupSearch()
        setupCurrentLocationButton()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        searchEditText = findViewById(R.id.etLocationSearch)
        recyclerView = findViewById(R.id.rvLocationResults)
        progressBar = findViewById(R.id.pbLoading)
        noResultsText = findViewById(R.id.tvNoResults)
        currentLocationButton = findViewById(R.id.btnCurrentLocation)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Select Location"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupLocationService() {
        locationService = LocationService(this)
    }
    
    private fun setupRecyclerView() {
        adapter = LocationAdapter { place ->
            selectLocation(place)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                
                val query = s.toString().trim()
                if (query.length >= 2) {
                    searchJob = lifecycleScope.launch {
                        delay(300) // Debounce
                        searchLocations(query)
                    }
                } else {
                    adapter.updateResults(emptyList())
                    updateUIState(showNoResults = false)
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Show initial popular cities
        showPopularCities()
    }
    
    private fun setupCurrentLocationButton() {
        currentLocationButton.setOnClickListener {
            // In production, implement GPS location detection
            Toast.makeText(this, "GPS location detection - implement with location permissions", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun searchLocations(query: String) {
        lifecycleScope.launch {
            updateUIState(showLoading = true)
            
            try {
                val results = locationService.searchPlaces(query)
                
                if (results.isEmpty()) {
                    updateUIState(showNoResults = true)
                } else {
                    adapter.updateResults(results)
                    updateUIState(showResults = true)
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@LocationPickerActivity, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                updateUIState(showNoResults = true)
            }
        }
    }
    
    private fun showPopularCities() {
        val popularCities = listOf(
            "Delhi", "Mumbai", "Bangalore", "Chennai", "Kolkata",
            "Hyderabad", "Pune", "Ahmedabad", "Jaipur", "Surat"
        )
        
        lifecycleScope.launch {
            val results = mutableListOf<LocationService.PlaceResult>()
            popularCities.forEach { city ->
                val cityResults = locationService.searchPlaces(city)
                if (cityResults.isNotEmpty()) {
                    results.add(cityResults.first())
                }
            }
            
            adapter.updateResults(results)
            updateUIState(showResults = true)
        }
    }
    
    private fun selectLocation(place: LocationService.PlaceResult) {
        lifecycleScope.launch {
            updateUIState(showLoading = true)
            
            try {
                // Get detailed location info with weather
                val locationInfo = if (place.latitude != 0.0 && place.longitude != 0.0) {
                    locationService.getLocationInfo(place.latitude, place.longitude)
                } else {
                    // Get coordinates from place details
                    val details = locationService.getPlaceDetails(place.placeId)
                    if (details != null) {
                        locationService.getLocationInfo(details.latitude, details.longitude)
                    } else {
                        null
                    }
                }
                
                if (locationInfo != null) {
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_LATITUDE, locationInfo.latitude)
                        putExtra(EXTRA_LONGITUDE, locationInfo.longitude)
                        putExtra(EXTRA_CITY_NAME, locationInfo.city)
                        putExtra(EXTRA_ADDRESS, locationInfo.address)
                    }
                    
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@LocationPickerActivity, "Failed to get location details", Toast.LENGTH_SHORT).show()
                    updateUIState(showResults = true)
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@LocationPickerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                updateUIState(showResults = true)
            }
        }
    }
    
    private fun updateUIState(
        showLoading: Boolean = false,
        showResults: Boolean = false,
        showNoResults: Boolean = false
    ) {
        progressBar.visibility = if (showLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (showResults) View.VISIBLE else View.GONE
        noResultsText.visibility = if (showNoResults) View.VISIBLE else View.GONE
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}