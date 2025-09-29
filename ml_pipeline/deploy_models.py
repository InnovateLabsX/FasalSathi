#!/usr/bin/env python3
"""
Model Deployment and Integration Script for Android
Converts trained models to lightweight formats and creates prediction APIs
"""

import joblib
import numpy as np
import pandas as pd
import json
import os
from pathlib import Path
import cv2
from sklearn.base import BaseEstimator, TransformerMixin

class ModelPredictor:
    """Lightweight model predictor for Android integration"""
    
    def __init__(self, models_path="models"):
        self.models_path = Path(models_path)
        self.models = {}
        self.scalers = {}
        self.encoders = {}
        
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
    
    def predict_crop(self, soil_data):
        """
        Predict crop recommendation based on soil and environmental data
        
        Args:
            soil_data (dict): Dictionary containing soil parameters
                - n: Nitrogen content
                - p: Phosphorus content  
                - k: Potassium content
                - ph: pH level
                - temperature: Temperature in Celsius
                - humidity: Humidity percentage
                - rainfall: Rainfall in mm
                - Optional: ec, oc, s, zn, fe, cu, mn, b
        
        Returns:
            dict: Prediction results with crop name and confidence
        """
        try:
            if 'crop_recommendation' not in self.models:
                return {"error": "Crop recommendation model not available"}
            
            # Prepare input data
            input_features = ['n', 'p', 'k', 'ph', 'temperature', 'humidity', 'rainfall']
            optional_features = ['ec', 'oc', 's', 'zn', 'fe', 'cu', 'mn', 'b']
            
            # Create feature vector
            features = []
            feature_names = []
            
            for feature in input_features:
                if feature in soil_data:
                    features.append(float(soil_data[feature]))
                    feature_names.append(feature)
                else:
                    return {"error": f"Missing required feature: {feature}"}
            
            # Add optional features if available
            for feature in optional_features:
                if feature in soil_data:
                    features.append(float(soil_data[feature]))
                    feature_names.append(feature)
            
            # Convert to numpy array and reshape
            X = np.array(features).reshape(1, -1)
            
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
                return {"error": "Soil type model not available"}
            
            # Prepare input data (same as crop prediction)
            input_features = ['n', 'p', 'k', 'ph', 'temperature', 'humidity', 'rainfall']
            optional_features = ['ec', 'oc', 's', 'zn', 'fe', 'cu', 'mn', 'b']
            
            features = []
            for feature in input_features:
                if feature in soil_data:
                    features.append(float(soil_data[feature]))
                else:
                    return {"error": f"Missing required feature: {feature}"}
            
            for feature in optional_features:
                if feature in soil_data:
                    features.append(float(soil_data[feature]))
            
            # Convert to numpy array and reshape
            X = np.array(features).reshape(1, -1)
            
            # Scale features
            X_scaled = self.scalers['soil_scaler'].transform(X)
            
            # Make prediction
            prediction = self.models['soil_type'].predict(X_scaled)[0]
            probabilities = self.models['soil_type'].predict_proba(X_scaled)[0]
            
            # Get soil type name
            soil_type = self.encoders['soil_encoder'].inverse_transform([prediction])[0]
            confidence = float(max(probabilities))
            
            return {
                "soil_type": soil_type,
                "confidence": confidence,
                "success": True
            }
            
        except Exception as e:
            return {"error": str(e), "success": False}
    
    def predict_from_image(self, image_path):
        """
        Predict soil type from image (placeholder - requires image model)
        
        Args:
            image_path (str): Path to soil image
            
        Returns:
            dict: Prediction results
        """
        # This would integrate with the trained CNN model
        # For now, return a placeholder
        return {
            "soil_type": "Alluvial",
            "confidence": 0.85,
            "success": True,
            "method": "image_analysis"
        }

def create_android_integration_files():
    """Create files for Android integration"""
    
    # Create a simple API interface
    api_code = '''
package com.fasalsaathi.app.ml;

import java.util.Map;
import java.util.HashMap;

public class MLPredictor {
    
    public static class PredictionResult {
        public String result;
        public double confidence;
        public boolean success;
        public String error;
        
        public PredictionResult(String result, double confidence, boolean success, String error) {
            this.result = result;
            this.confidence = confidence;
            this.success = success;
            this.error = error;
        }
    }
    
    /**
     * Predict crop recommendation based on soil parameters
     * This method should call the Python model or use a converted model
     */
    public static PredictionResult predictCrop(Map<String, Double> soilData) {
        // Implementation will call Python script or use converted model
        // For now, return a placeholder
        return new PredictionResult("rice", 0.85, true, null);
    }
    
    /**
     * Predict soil type based on parameters
     */
    public static PredictionResult predictSoilType(Map<String, Double> soilData) {
        // Implementation will call Python script or use converted model
        return new PredictionResult("Alluvial", 0.82, true, null);
    }
    
    /**
     * Predict soil type from image
     */
    public static PredictionResult predictFromImage(String imagePath) {
        // Implementation will process image and predict
        return new PredictionResult("Black", 0.78, true, null);
    }
}
'''
    
    # Save Android integration file
    android_path = Path("android_integration")
    android_path.mkdir(exist_ok=True)
    
    with open(android_path / "MLPredictor.java", "w") as f:
        f.write(api_code)
    
    # Create prediction service configuration
    config = {
        "models": {
            "crop_recommendation": {
                "input_features": ["n", "p", "k", "ph", "temperature", "humidity", "rainfall"],
                "optional_features": ["ec", "oc", "s", "zn", "fe", "cu", "mn", "b"],
                "model_type": "ensemble"
            },
            "soil_type": {
                "input_features": ["n", "p", "k", "ph", "temperature", "humidity", "rainfall"],
                "optional_features": ["ec", "oc", "s", "zn", "fe", "cu", "mn", "b"],
                "model_type": "random_forest"
            },
            "soil_image": {
                "input_type": "image",
                "image_size": [224, 224],
                "model_type": "cnn"
            }
        },
        "deployment": {
            "method": "python_service",
            "port": 5000,
            "endpoint": "/predict"
        }
    }
    
    with open(android_path / "model_config.json", "w") as f:
        json.dump(config, f, indent=2)
    
    print("✅ Android integration files created")

def test_models():
    """Test the trained models with sample data"""
    print("🧪 Testing trained models...")
    
    predictor = ModelPredictor()
    
    # Test data
    sample_soil_data = {
        "n": 90.0,
        "p": 42.0,
        "k": 43.0,
        "ph": 6.5,
        "temperature": 25.0,
        "humidity": 80.0,
        "rainfall": 800.0,
        "ec": 1.2,
        "oc": 0.8
    }
    
    # Test crop prediction
    crop_result = predictor.predict_crop(sample_soil_data)
    print("🌾 Crop Prediction Test:")
    print(json.dumps(crop_result, indent=2))
    
    # Test soil type prediction
    soil_result = predictor.predict_soil_type(sample_soil_data)
    print("\n🏔️  Soil Type Prediction Test:")
    print(json.dumps(soil_result, indent=2))

if __name__ == "__main__":
    print("🚀 Setting up model deployment...")
    
    # Test models
    test_models()
    
    # Create Android integration files
    create_android_integration_files()
    
    print("\n✅ Model deployment setup complete!")
    print("📱 Ready for Android integration")