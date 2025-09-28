package com.fasalsaathi.app.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.fasalsaathi.app.data.local.entities.*
import java.util.Date

class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromListString(list: List<String>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun fromRecommendedCropList(value: String): List<RecommendedCrop> {
        val listType = object : TypeToken<List<RecommendedCrop>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromListRecommendedCrop(list: List<RecommendedCrop>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun fromDetectedDiseaseList(value: String): List<DetectedDisease> {
        val listType = object : TypeToken<List<DetectedDisease>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromListDetectedDisease(list: List<DetectedDisease>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun fromWeatherForecastList(value: String): List<WeatherForecast> {
        val listType = object : TypeToken<List<WeatherForecast>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromListWeatherForecast(list: List<WeatherForecast>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun fromWeatherAlertList(value: String): List<WeatherAlert> {
        val listType = object : TypeToken<List<WeatherAlert>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromListWeatherAlert(list: List<WeatherAlert>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun fromCurrentCropList(value: String): List<CurrentCrop> {
        val listType = object : TypeToken<List<CurrentCrop>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromListCurrentCrop(list: List<CurrentCrop>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun fromSoilHealth(value: String?): SoilHealth? {
        return value?.let { gson.fromJson(it, SoilHealth::class.java) }
    }
    
    @TypeConverter
    fun fromSoilHealthToString(soilHealth: SoilHealth?): String? {
        return soilHealth?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun fromStringMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }
    
    @TypeConverter
    fun fromMapString(map: Map<String, String>): String {
        return gson.toJson(map)
    }
}