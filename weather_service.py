#!/usr/bin/env python3
"""
Weather Service for Fasal Sathi
Provides real-time weather data using Open-Meteo API
"""

import openmeteo_requests
import pandas as pd
import requests_cache
from retry_requests import retry
import json
import sys
from datetime import datetime, timedelta

class WeatherService:
    def __init__(self):
        # Setup the Open-Meteo API client with cache and retry on error
        cache_session = requests_cache.CachedSession('.cache', expire_after=3600)
        retry_session = retry(cache_session, retries=5, backoff_factor=0.2)
        self.openmeteo = openmeteo_requests.Client(session=retry_session)
        
    def get_comprehensive_weather_data(self, latitude, longitude):
        """
        Get comprehensive weather data for crop recommendation
        """
        url = "https://api.open-meteo.com/v1/forecast"
        
        # Parameters for agricultural weather data
        params = {
            "latitude": latitude,
            "longitude": longitude,
            "current": [
                "temperature_2m", "relative_humidity_2m", "apparent_temperature",
                "precipitation", "weather_code", "cloud_cover", "pressure_msl",
                "wind_speed_10m", "wind_direction_10m"
            ],
            "hourly": [
                "temperature_2m", "relative_humidity_2m", "precipitation",
                "precipitation_probability", "weather_code", "visibility",
                "cloud_cover", "wind_speed_10m", "wind_direction_10m",
                "soil_temperature_0cm", "soil_temperature_6cm", "soil_temperature_18cm",
                "soil_moisture_0_to_1cm", "soil_moisture_1_to_3cm", "soil_moisture_3_to_9cm",
                "uv_index"
            ],
            "daily": [
                "weather_code", "temperature_2m_max", "temperature_2m_min",
                "precipitation_sum", "precipitation_hours", "precipitation_probability_max",
                "wind_speed_10m_max", "wind_gusts_10m_max", "wind_direction_10m_dominant",
                "sunshine_duration", "uv_index_max"
            ],
            "forecast_days": 7,
            "timezone": "auto"
        }
        
        try:
            responses = self.openmeteo.weather_api(url, params=params)
            response = responses[0]
            
            # Process current weather
            current = response.Current()
            current_data = {
                "temperature": float(round(current.Variables(0).Value(), 1)),
                "humidity": float(round(current.Variables(1).Value(), 1)),
                "apparent_temperature": float(round(current.Variables(2).Value(), 1)),
                "precipitation": float(round(current.Variables(3).Value(), 2)),
                "weather_code": int(current.Variables(4).Value()),
                "cloud_cover": float(round(current.Variables(5).Value(), 1)),
                "pressure": float(round(current.Variables(6).Value(), 1)),
                "wind_speed": float(round(current.Variables(7).Value(), 1)),
                "wind_direction": float(round(current.Variables(8).Value(), 1)),
                "timestamp": int(current.Time())
            }
            
            # Process hourly data for next 24 hours
            hourly = response.Hourly()
            hourly_data = []
            
            # Get next 24 hours of data
            temp_values = hourly.Variables(0).ValuesAsNumpy()
            humidity_values = hourly.Variables(1).ValuesAsNumpy()
            precip_values = hourly.Variables(2).ValuesAsNumpy()
            precip_prob_values = hourly.Variables(3).ValuesAsNumpy()
            weather_code_values = hourly.Variables(4).ValuesAsNumpy()
            visibility_values = hourly.Variables(5).ValuesAsNumpy()
            cloud_cover_values = hourly.Variables(6).ValuesAsNumpy()
            wind_speed_values = hourly.Variables(7).ValuesAsNumpy()
            wind_dir_values = hourly.Variables(8).ValuesAsNumpy()
            soil_temp_0_values = hourly.Variables(9).ValuesAsNumpy()
            soil_temp_6_values = hourly.Variables(10).ValuesAsNumpy()
            soil_temp_18_values = hourly.Variables(11).ValuesAsNumpy()
            soil_moisture_0_1_values = hourly.Variables(12).ValuesAsNumpy()
            soil_moisture_1_3_values = hourly.Variables(13).ValuesAsNumpy()
            soil_moisture_3_9_values = hourly.Variables(14).ValuesAsNumpy()
            uv_values = hourly.Variables(15).ValuesAsNumpy()
            
            for i in range(min(24, len(temp_values))):
                hour_data = {
                    "hour": i,
                    "temperature": float(round(temp_values[i], 1)),
                    "humidity": float(round(humidity_values[i], 1)),
                    "precipitation": float(round(precip_values[i], 2)),
                    "precipitation_probability": float(round(precip_prob_values[i], 1)),
                    "weather_code": int(weather_code_values[i]),
                    "visibility": float(round(visibility_values[i], 1)),
                    "cloud_cover": float(round(cloud_cover_values[i], 1)),
                    "wind_speed": float(round(wind_speed_values[i], 1)),
                    "wind_direction": float(round(wind_dir_values[i], 1)),
                    "soil_temp_0cm": float(round(soil_temp_0_values[i], 1)),
                    "soil_temp_6cm": float(round(soil_temp_6_values[i], 1)),
                    "soil_temp_18cm": float(round(soil_temp_18_values[i], 1)),
                    "soil_moisture_0_1cm": float(round(soil_moisture_0_1_values[i], 3)),
                    "soil_moisture_1_3cm": float(round(soil_moisture_1_3_values[i], 3)),
                    "soil_moisture_3_9cm": float(round(soil_moisture_3_9_values[i], 3)),
                    "uv_index": float(round(uv_values[i], 1))
                }
                hourly_data.append(hour_data)
            
            # Process daily data for next 7 days
            daily = response.Daily()
            daily_data = []
            
            daily_weather_codes = daily.Variables(0).ValuesAsNumpy()
            daily_temp_max = daily.Variables(1).ValuesAsNumpy()
            daily_temp_min = daily.Variables(2).ValuesAsNumpy()
            daily_precip_sum = daily.Variables(3).ValuesAsNumpy()
            daily_precip_hours = daily.Variables(4).ValuesAsNumpy()
            daily_precip_prob = daily.Variables(5).ValuesAsNumpy()
            daily_wind_max = daily.Variables(6).ValuesAsNumpy()
            daily_wind_gusts = daily.Variables(7).ValuesAsNumpy()
            daily_wind_dir = daily.Variables(8).ValuesAsNumpy()
            daily_sunshine = daily.Variables(9).ValuesAsNumpy()
            daily_uv_max = daily.Variables(10).ValuesAsNumpy()
            
            for i in range(min(7, len(daily_weather_codes))):
                day_data = {
                    "day": i,
                    "weather_code": int(daily_weather_codes[i]),
                    "temp_max": float(round(daily_temp_max[i], 1)),
                    "temp_min": float(round(daily_temp_min[i], 1)),
                    "precipitation_sum": float(round(daily_precip_sum[i], 2)),
                    "precipitation_hours": float(round(daily_precip_hours[i], 1)),
                    "precipitation_probability": float(round(daily_precip_prob[i], 1)),
                    "wind_speed_max": float(round(daily_wind_max[i], 1)),
                    "wind_gusts_max": float(round(daily_wind_gusts[i], 1)),
                    "wind_direction": float(round(daily_wind_dir[i], 1)),
                    "sunshine_duration": float(round(daily_sunshine[i], 1)),
                    "uv_index_max": float(round(daily_uv_max[i], 1))
                }
                daily_data.append(day_data)
            
            # Calculate agricultural metrics
            agricultural_metrics = self.calculate_agricultural_metrics(current_data, hourly_data, daily_data)
            
            weather_data = {
                "coordinates": {
                    "latitude": float(response.Latitude()),
                    "longitude": float(response.Longitude()),
                    "elevation": float(response.Elevation()),
                    "timezone": str(response.TimezoneAbbreviation())
                },
                "current": current_data,
                "hourly": hourly_data,
                "daily": daily_data,
                "agricultural_metrics": agricultural_metrics,
                "status": "success",
                "timestamp": datetime.now().isoformat()
            }
            
            return weather_data
            
        except Exception as e:
            return {
                "status": "error",
                "message": str(e),
                "timestamp": datetime.now().isoformat()
            }
    
    def calculate_agricultural_metrics(self, current, hourly, daily):
        """
        Calculate agricultural-specific metrics from weather data
        """
        # Calculate average temperature for next 24 hours
        avg_temp_24h = sum(hour["temperature"] for hour in hourly) / len(hourly)
        
        # Calculate average humidity for next 24 hours
        avg_humidity_24h = sum(hour["humidity"] for hour in hourly) / len(hourly)
        
        # Calculate total precipitation for next 7 days
        total_precipitation_7d = sum(day["precipitation_sum"] for day in daily)
        
        # Calculate average soil moisture (0-9cm depth)
        avg_soil_moisture = sum(
            (hour["soil_moisture_0_1cm"] + hour["soil_moisture_1_3cm"] + hour["soil_moisture_3_9cm"]) / 3
            for hour in hourly
        ) / len(hourly)
        
        # Calculate growing degree days (base 10°C)
        gdd_base_10 = sum(
            max(0, (day["temp_max"] + day["temp_min"]) / 2 - 10)
            for day in daily
        )
        
        # Calculate water stress index
        water_stress_index = self.calculate_water_stress_index(hourly, daily)
        
        # Calculate heat stress risk
        heat_stress_risk = self.calculate_heat_stress_risk(daily)
        
        # Calculate irrigation need index
        irrigation_need = self.calculate_irrigation_need(hourly, daily)
        
        return {
            "avg_temperature_24h": round(avg_temp_24h, 1),
            "avg_humidity_24h": round(avg_humidity_24h, 1),
            "total_precipitation_7d": round(total_precipitation_7d, 2),
            "avg_soil_moisture": round(avg_soil_moisture, 3),
            "growing_degree_days": round(gdd_base_10, 1),
            "water_stress_index": round(water_stress_index, 2),
            "heat_stress_risk": heat_stress_risk,
            "irrigation_need_index": round(irrigation_need, 2),
            "frost_risk": any(day["temp_min"] < 2 for day in daily),
            "optimal_planting_conditions": self.assess_planting_conditions(current, daily)
        }
    
    def calculate_water_stress_index(self, hourly, daily):
        """Calculate water stress based on soil moisture and precipitation"""
        avg_soil_moisture = sum(
            (hour["soil_moisture_0_1cm"] + hour["soil_moisture_1_3cm"] + hour["soil_moisture_3_9cm"]) / 3
            for hour in hourly
        ) / len(hourly)
        
        recent_precipitation = sum(day["precipitation_sum"] for day in daily[:3])
        
        # Simple water stress calculation (0 = no stress, 1 = high stress)
        if avg_soil_moisture > 0.3 and recent_precipitation > 10:
            return 0.1  # Low stress
        elif avg_soil_moisture > 0.2 and recent_precipitation > 5:
            return 0.3  # Moderate stress
        elif avg_soil_moisture > 0.1:
            return 0.6  # High stress
        else:
            return 0.9  # Very high stress
    
    def calculate_heat_stress_risk(self, daily):
        """Calculate heat stress risk for crops"""
        max_temps = [day["temp_max"] for day in daily[:3]]
        
        if any(temp > 40 for temp in max_temps):
            return "high"
        elif any(temp > 35 for temp in max_temps):
            return "moderate"
        elif any(temp > 30 for temp in max_temps):
            return "low"
        else:
            return "none"
    
    def calculate_irrigation_need(self, hourly, daily):
        """Calculate irrigation need index"""
        # Calculate estimated water need based on temperature and humidity
        avg_temp = sum(hour["temperature"] for hour in hourly) / len(hourly)
        avg_humidity = sum(hour["humidity"] for hour in hourly) / len(hourly)
        precipitation = sum(day["precipitation_sum"] for day in daily[:3])
        
        # Estimate water need based on temperature and humidity
        estimated_water_need = max(0, (avg_temp - 15) * 2 + (100 - avg_humidity) * 0.5)
        
        # Simple irrigation need calculation
        water_balance = precipitation - estimated_water_need
        
        if water_balance < -15:
            return 0.9  # High need
        elif water_balance < -8:
            return 0.6  # Moderate need
        elif water_balance < 0:
            return 0.3  # Low need
        else:
            return 0.1  # Minimal need
    
    def assess_planting_conditions(self, current, daily):
        """Assess if conditions are optimal for planting"""
        avg_temp = sum(day["temp_max"] + day["temp_min"] for day in daily[:3]) / (3 * 2)
        total_rain = sum(day["precipitation_sum"] for day in daily[:3])
        
        conditions = {
            "temperature_suitable": 15 <= avg_temp <= 30,
            "moisture_adequate": total_rain >= 5,
            "no_extreme_weather": all(day["wind_speed_max"] < 25 for day in daily[:3]),
            "overall_rating": "good"
        }
        
        suitable_count = sum(1 for v in conditions.values() if isinstance(v, bool) and v)
        
        if suitable_count >= 3:
            conditions["overall_rating"] = "excellent"
        elif suitable_count >= 2:
            conditions["overall_rating"] = "good"
        elif suitable_count >= 1:
            conditions["overall_rating"] = "fair"
        else:
            conditions["overall_rating"] = "poor"
        
        return conditions

def get_weather_description(weather_code):
    """Convert weather code to description"""
    weather_codes = {
        0: "Clear sky",
        1: "Mainly clear", 2: "Partly cloudy", 3: "Overcast",
        45: "Fog", 48: "Depositing rime fog",
        51: "Light drizzle", 53: "Moderate drizzle", 55: "Dense drizzle",
        56: "Light freezing drizzle", 57: "Dense freezing drizzle",
        61: "Slight rain", 63: "Moderate rain", 65: "Heavy rain",
        66: "Light freezing rain", 67: "Heavy freezing rain",
        71: "Slight snow", 73: "Moderate snow", 75: "Heavy snow",
        77: "Snow grains",
        80: "Slight rain showers", 81: "Moderate rain showers", 82: "Violent rain showers",
        85: "Slight snow showers", 86: "Heavy snow showers",
        95: "Thunderstorm", 96: "Thunderstorm with slight hail", 99: "Thunderstorm with heavy hail"
    }
    return weather_codes.get(weather_code, "Unknown weather")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python weather_service.py <latitude> <longitude>")
        sys.exit(1)
    
    try:
        latitude = float(sys.argv[1])
        longitude = float(sys.argv[2])
        
        weather_service = WeatherService()
        weather_data = weather_service.get_comprehensive_weather_data(latitude, longitude)
        
        print(json.dumps(weather_data, indent=2))
        
    except ValueError:
        print(json.dumps({"status": "error", "message": "Invalid latitude or longitude"}))
    except Exception as e:
        print(json.dumps({"status": "error", "message": str(e)}))