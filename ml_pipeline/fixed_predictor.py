#!/usr/bin/env python3
"""
Fixed Model Predictor for Android Integration
Handles proper feature matching and provides fallback values
"""

import joblib
import numpy as np
import pandas as pd
import json
import os
from pathlib import Path

class FixedModelPredictor:
    """Improved model predictor with proper feature handling"""
    
    def __init__(self, models_path="models"):
        self.models_path = Path(models_path)
        self.models = {}
        self.scalers = {}
        self.encoders = {}
        
        # Expected feature order (based on training)
        self.expected_features = [
            'n', 'p', 'k', 'ph', 'temperature', 'humidity', 'rainfall',
            'ec', 'oc', 's', 'zn', 'fe', 'cu', 'mn', 'b'
        ]
        
        # Default values for optional features
        self.default_values = {
            'ec': 1.0, 'oc': 0.8, 's': 15.0, 'zn': 1.0, 'fe': 8.0, 
            'cu': 1.0, 'mn': 3.0, 'b': 0.5
        }
        
        self.load_models()
    
    def load_models(self):
        """Load all trained models"""
        try:
            # Load crop recommendation model
            if (self.models_path / 'crop_recommendation_model.joblib').exists():
                self.models['crop_recommendation'] = joblib.load(
                    self.models_path / 'crop_recommendation_model.joblib'
                )
                self.scalers['crop_scaler'] = joblib.load(
                    self.models_path / 'crop_scaler.joblib'
                )
                self.encoders['crop_encoder'] = joblib.load(
                    self.models_path / 'crop_encoder.joblib'
                )
                print("✅ Crop recommendation model loaded")
            
            # Load soil type model
            if (self.models_path / 'soil_type_model.joblib').exists():
                self.models['soil_type'] = joblib.load(
                    self.models_path / 'soil_type_model.joblib'
                )
                self.scalers['soil_scaler'] = joblib.load(
                    self.models_path / 'soil_scaler.joblib'
                )
                self.encoders['soil_encoder'] = joblib.load(
                    self.models_path / 'soil_encoder.joblib'
                )
                print("✅ Soil type model loaded")
                
        except Exception as e:
            print(f"❌ Error loading models: {e}")
    
    def prepare_features(self, soil_data):
        """Prepare feature vector with proper ordering and default values"""
        features = []
        
        for feature in self.expected_features:
            if feature in soil_data:
                features.append(float(soil_data[feature]))
            elif feature in self.default_values:
                features.append(self.default_values[feature])
            else:
                # For required features, return error
                required_features = ['n', 'p', 'k', 'ph', 'temperature', 'humidity', 'rainfall']
                if feature in required_features:
                    return None, f"Missing required feature: {feature}"
                features.append(0.0)  # Default to 0 for any missing feature
        
        return np.array(features).reshape(1, -1), None
    
    def predict_crop(self, soil_data):
        """
        Predict crop recommendation based on soil and environmental data
        
        Args:
            soil_data (dict): Dictionary containing soil parameters
                Required: n, p, k, ph, temperature, humidity, rainfall
                Optional: ec, oc, s, zn, fe, cu, mn, b
        
        Returns:
            dict: Prediction results with crop name and confidence
        """
        try:
            if 'crop_recommendation' not in self.models:
                return {"error": "Crop recommendation model not available", "success": False}
            
            # Prepare feature vector
            X, error = self.prepare_features(soil_data)
            if error:
                return {"error": error, "success": False}
            
            # Scale features
            X_scaled = self.scalers['crop_scaler'].transform(X)
            
            # Make prediction
            prediction = self.models['crop_recommendation'].predict(X_scaled)[0]
            probabilities = self.models['crop_recommendation'].predict_proba(X_scaled)[0]
            
            # Get crop name
            crop_name = self.encoders['crop_encoder'].inverse_transform([prediction])[0]
            confidence = float(max(probabilities))
            
            # Get top 3 recommendations
            top_indices = np.argsort(probabilities)[-3:][::-1]
            top_crops = []
            
            for idx in top_indices:
                crop = self.encoders['crop_encoder'].inverse_transform([idx])[0]
                prob = float(probabilities[idx])
                top_crops.append({"crop": crop, "confidence": prob})
            
            return {
                "recommended_crop": crop_name,
                "confidence": confidence,
                "top_recommendations": top_crops,
                "success": True
            }
            
        except Exception as e:
            return {"error": str(e), "success": False}
    
    def predict_soil_type(self, soil_data):
        """
        Predict soil type based on soil parameters
        
        Args:
            soil_data (dict): Dictionary containing soil parameters
        
        Returns:
            dict: Prediction results with soil type and confidence
        """
        try:
            if 'soil_type' not in self.models:
                return {"error": "Soil type model not available", "success": False}
            
            # Prepare feature vector
            X, error = self.prepare_features(soil_data)
            if error:
                return {"error": error, "success": False}
            
            # Scale features
            X_scaled = self.scalers['soil_scaler'].transform(X)
            
            # Make prediction
            prediction = self.models['soil_type'].predict(X_scaled)[0]
            probabilities = self.models['soil_type'].predict_proba(X_scaled)[0]
            
            # Get soil type name
            soil_type = self.encoders['soil_encoder'].inverse_transform([prediction])[0]
            confidence = float(max(probabilities))
            
            # Get top 3 soil type predictions
            top_indices = np.argsort(probabilities)[-3:][::-1]
            top_soils = []
            
            for idx in top_indices:
                soil = self.encoders['soil_encoder'].inverse_transform([idx])[0]
                prob = float(probabilities[idx])
                top_soils.append({"soil_type": soil, "confidence": prob})
            
            return {
                "soil_type": soil_type,
                "confidence": confidence,
                "top_predictions": top_soils,
                "success": True
            }
            
        except Exception as e:
            return {"error": str(e), "success": False}

def test_fixed_models():
    """Test the fixed models with sample data"""
    print("🧪 Testing fixed models...")
    
    predictor = FixedModelPredictor()
    
    # Test data with all required features
    sample_soil_data = {
        "n": 90.0,
        "p": 42.0,
        "k": 43.0,
        "ph": 6.5,
        "temperature": 25.0,
        "humidity": 80.0,
        "rainfall": 800.0
        # Optional features will use defaults
    }
    
    # Test crop prediction
    crop_result = predictor.predict_crop(sample_soil_data)
    print("🌾 Crop Prediction Test:")
    print(json.dumps(crop_result, indent=2))
    
    # Test soil type prediction
    soil_result = predictor.predict_soil_type(sample_soil_data)
    print("\n🏔️  Soil Type Prediction Test:")
    print(json.dumps(soil_result, indent=2))
    
    # Test with more detailed data
    detailed_soil_data = {
        "n": 120.0,
        "p": 38.0,
        "k": 55.0,
        "ph": 7.2,
        "temperature": 28.0,
        "humidity": 65.0,
        "rainfall": 600.0,
        "ec": 1.3,
        "oc": 0.9,
        "s": 18.0
    }
    
    print("\n🌾 Detailed Crop Prediction Test:")
    detailed_crop_result = predictor.predict_crop(detailed_soil_data)
    print(json.dumps(detailed_crop_result, indent=2))

if __name__ == "__main__":
    test_fixed_models()