package com.fasalsaathi.app.ui.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.appbar.MaterialToolbar

data class MarketPrice(
    val id: Int,
    val cropName: String,
    val variety: String,
    val currentPrice: String,
    val previousPrice: String,
    val priceChange: String,
    val isIncreasing: Boolean,
    val market: String,
    val lastUpdated: String,
    val unit: String = "per quintal"
)

class MarketUpdatesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var marketAdapter: MarketPriceAdapter
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_updates)

        setupToolbar()
        setupRecyclerView()
        loadMarketPrices()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Market Updates"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewMarket)
        marketAdapter = MarketPriceAdapter { price ->
            Toast.makeText(this, "View details for ${price.cropName}", Toast.LENGTH_SHORT).show()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MarketUpdatesActivity)
            adapter = marketAdapter
        }
    }

    private fun loadMarketPrices() {
        val samplePrices = listOf(
            MarketPrice(
                1,
                "Wheat",
                "HD-2967",
                "₹2,180",
                "₹2,150",
                "+₹30",
                true,
                "Delhi Mandi",
                "2 hours ago"
            ),
            MarketPrice(
                2,
                "Rice",
                "Basmati 1121",
                "₹4,500",
                "₹4,650",
                "-₹150",
                false,
                "Karnal Mandi",
                "3 hours ago"
            ),
            MarketPrice(
                3,
                "Tomato",
                "Hybrid",
                "₹1,800",
                "₹1,650",
                "+₹150",
                true,
                "Azadpur Mandi",
                "1 hour ago"
            ),
            MarketPrice(
                4,
                "Onion",
                "Nashik Red",
                "₹3,200",
                "₹3,400",
                "-₹200",
                false,
                "Nashik Mandi",
                "4 hours ago"
            ),
            MarketPrice(
                5,
                "Cotton",
                "Shankar-6",
                "₹6,800",
                "₹6,750",
                "+₹50",
                true,
                "Guntur Mandi",
                "5 hours ago"
            ),
            MarketPrice(
                6,
                "Sugarcane",
                "Co-86032",
                "₹350",
                "₹345",
                "+₹5",
                true,
                "Muzaffarnagar",
                "6 hours ago",
                "per ton"
            )
        )
        
        marketAdapter.updatePrices(samplePrices)
    }
}