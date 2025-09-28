package com.fasalsaathi.app.ui.disease

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.R

class DiseaseDetectionActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detection)
        
        setupToolbar()
    }
    
    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Disease Detection"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}