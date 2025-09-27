package com.fasalsaathi.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "users")
@Parcelize
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val phoneNumber: String?,
    val fullName: String,
    val profileImageUrl: String? = null,
    val location: String? = null,
    val state: String? = null,
    val district: String? = null,
    val pincode: String? = null,
    val languagePreference: String = "english",
    val farmingExperience: Int = 0,
    val farmSize: Double = 0.0, // in acres
    val soilType: String? = null,
    val primaryCrops: List<String> = emptyList(),
    val isProfileComplete: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable

@Entity(tableName = "crop_recommendations")
@Parcelize
data class CropRecommendation(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nitrogenLevel: Double,
    val phosphorusLevel: Double,
    val potassiumLevel: Double,
    val phLevel: Double,
    val rainfall: Double,
    val temperature: Double,
    val humidity: Double,
    val location: String,
    val previousCrop: String?,
    val season: String,
    val recommendedCrops: List<RecommendedCrop>,
    val sustainabilityScore: Double,
    val confidence: Double,
    val createdAt: Date = Date()
) : Parcelable

@Parcelize
data class RecommendedCrop(
    val name: String,
    val probability: Double,
    val expectedYield: Double,
    val profitProjection: Double,
    val riskLevel: String,
    val growthDuration: Int, // in days
    val waterRequirement: String,
    val marketDemand: String,
    val description: String
) : Parcelable

@Entity(tableName = "disease_detections")
@Parcelize
data class DiseaseDetection(
    @PrimaryKey
    val id: String,
    val userId: String,
    val imageUrl: String,
    val plantType: String,
    val detectedDiseases: List<DetectedDisease>,
    val confidence: Double,
    val isHealthy: Boolean,
    val location: String?,
    val createdAt: Date = Date()
) : Parcelable

@Parcelize
data class DetectedDisease(
    val name: String,
    val confidence: Double,
    val severity: String, // mild, moderate, severe
    val description: String,
    val treatment: List<String>,
    val prevention: List<String>,
    val organicSolutions: List<String>
) : Parcelable

@Entity(tableName = "market_prices")
@Parcelize
data class MarketPrice(
    @PrimaryKey
    val id: String,
    val cropName: String,
    val marketName: String,
    val state: String,
    val district: String,
    val pricePerQuintal: Double,
    val unit: String = "Quintal",
    val grade: String?,
    val variety: String?,
    val minPrice: Double,
    val maxPrice: Double,
    val modalPrice: Double,
    val date: Date,
    val source: String // eNAM, AGMARKNET, etc.
) : Parcelable

@Entity(tableName = "weather_data")
@Parcelize
data class WeatherData(
    @PrimaryKey
    val id: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Double,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Double,
    val windSpeed: Double,
    val windDirection: String,
    val weatherCondition: String,
    val description: String,
    val iconCode: String,
    val rainfall: Double = 0.0,
    val forecast: List<WeatherForecast> = emptyList(),
    val alerts: List<WeatherAlert> = emptyList(),
    val updatedAt: Date = Date()
) : Parcelable

@Parcelize
data class WeatherForecast(
    val date: Date,
    val minTemp: Double,
    val maxTemp: Double,
    val condition: String,
    val iconCode: String,
    val rainfall: Double,
    val windSpeed: Double,
    val humidity: Double
) : Parcelable

@Parcelize
data class WeatherAlert(
    val type: String, // storm, heavy_rain, heat_wave, etc.
    val severity: String, // low, medium, high
    val title: String,
    val description: String,
    val validFrom: Date,
    val validTo: Date
) : Parcelable

@Entity(tableName = "farm_data")
@Parcelize
data class FarmData(
    @PrimaryKey
    val id: String,
    val userId: String,
    val farmName: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val totalArea: Double, // in acres
    val soilType: String,
    val soilHealth: SoilHealth?,
    val currentCrops: List<CurrentCrop> = emptyList(),
    val irrigationType: String,
    val equipmentList: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable

@Parcelize
data class SoilHealth(
    val nitrogenLevel: Double,
    val phosphorusLevel: Double,
    val potassiumLevel: Double,
    val phLevel: Double,
    val organicCarbon: Double,
    val moisture: Double,
    val temperature: Double,
    val lastTested: Date
) : Parcelable

@Parcelize
data class CurrentCrop(
    val name: String,
    val variety: String,
    val plantedDate: Date,
    val expectedHarvestDate: Date,
    val area: Double, // in acres
    val stage: String, // sowing, growing, flowering, harvesting
    val healthStatus: String // healthy, diseased, pest_attack
) : Parcelable

@Entity(tableName = "notifications")
@Parcelize
data class Notification(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // weather_alert, price_alert, crop_advice, disease_alert, general
    val priority: String, // low, medium, high
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val imageUrl: String? = null,
    val data: Map<String, String> = emptyMap(),
    val createdAt: Date = Date()
) : Parcelable