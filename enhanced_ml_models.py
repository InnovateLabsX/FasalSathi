#!/usr/bin/env python3
"""
Enhanced ML Models for Fasal Sathi
Advanced crop recommendation and soil analysis with improved accuracy
"""

import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import accuracy_score, classification_report
import joblib
import json
import sys
import os
from datetime import datetime

class EnhancedMLModel:
    def __init__(self):
        self.crop_model = None
        self.soil_model = None
        self.scaler = StandardScaler()
        self.crop_encoder = LabelEncoder()
        self.soil_encoder = LabelEncoder()
        
        # Enhanced crop database with regional variations
        self.crop_database = {
            'rice': {
                'optimal_conditions': {
                    'temperature': (20, 35), 'humidity': (70, 90), 'rainfall': (1000, 2500),
                    'ph': (5.5, 7.0), 'n': (120, 200), 'p': (30, 60), 'k': (120, 180)
                },
                'regions': ['all_india'],
                'seasons': ['kharif', 'rabi'],
                'soil_types': ['alluvial', 'black', 'red'],
                'varieties': ['basmati', 'non_basmati', 'aromatic']
            },
            'wheat': {
                'optimal_conditions': {
                    'temperature': (10, 25), 'humidity': (50, 70), 'rainfall': (300, 800),
                    'ph': (6.0, 7.5), 'n': (100, 150), 'p': (25, 50), 'k': (100, 150)
                },
                'regions': ['north_india', 'central_india'],
                'seasons': ['rabi'],
                'soil_types': ['alluvial', 'black'],
                'varieties': ['durum', 'bread_wheat', 'dicoccum']
            },
            'maize': {
                'optimal_conditions': {
                    'temperature': (18, 32), 'humidity': (60, 80), 'rainfall': (500, 1200),
                    'ph': (5.8, 7.0), 'n': (80, 140), 'p': (20, 50), 'k': (80, 120)
                },
                'regions': ['all_india'],
                'seasons': ['kharif', 'rabi', 'summer'],
                'soil_types': ['alluvial', 'red', 'black'],
                'varieties': ['hybrid', 'composite', 'sweet_corn']
            },
            'cotton': {
                'optimal_conditions': {
                    'temperature': (21, 35), 'humidity': (50, 80), 'rainfall': (500, 1000),
                    'ph': (5.8, 8.2), 'n': (60, 120), 'p': (15, 40), 'k': (60, 100)
                },
                'regions': ['central_india', 'south_india', 'west_india'],
                'seasons': ['kharif'],
                'soil_types': ['black', 'alluvial', 'red'],
                'varieties': ['bt_cotton', 'hybrid', 'desi']
            },
            'sugarcane': {
                'optimal_conditions': {
                    'temperature': (20, 40), 'humidity': (75, 85), 'rainfall': (1000, 2000),
                    'ph': (6.0, 8.5), 'n': (150, 250), 'p': (40, 80), 'k': (150, 250)
                },
                'regions': ['north_india', 'south_india', 'west_india'],
                'seasons': ['annual'],
                'soil_types': ['alluvial', 'black', 'red'],
                'varieties': ['early_maturing', 'mid_late', 'ratoon']
            },
            'soybean': {
                'optimal_conditions': {
                    'temperature': (20, 35), 'humidity': (60, 80), 'rainfall': (600, 1200),
                    'ph': (6.0, 7.5), 'n': (40, 80), 'p': (20, 40), 'k': (40, 80)
                },
                'regions': ['central_india', 'west_india'],
                'seasons': ['kharif'],
                'soil_types': ['black', 'alluvial', 'red'],
                'varieties': ['early', 'medium', 'late']
            },
            'groundnut': {
                'optimal_conditions': {
                    'temperature': (20, 35), 'humidity': (60, 75), 'rainfall': (500, 1000),
                    'ph': (6.0, 7.5), 'n': (20, 40), 'p': (40, 80), 'k': (60, 100)
                },
                'regions': ['south_india', 'west_india', 'central_india'],
                'seasons': ['kharif', 'rabi'],
                'soil_types': ['red', 'alluvial', 'black'],
                'varieties': ['spanish', 'valencia', 'virginia']
            },
            'sunflower': {
                'optimal_conditions': {
                    'temperature': (20, 30), 'humidity': (50, 70), 'rainfall': (400, 800),
                    'ph': (6.0, 7.5), 'n': (60, 100), 'p': (30, 60), 'k': (80, 120)
                },
                'regions': ['south_india', 'central_india'],
                'seasons': ['rabi', 'kharif'],
                'soil_types': ['black', 'red', 'alluvial'],
                'varieties': ['hybrid', 'composite', 'dwarf']
            }
        }
        
        # Enhanced soil type characteristics
        self.soil_characteristics = {
            'alluvial': {
                'drainage': 'good', 'fertility': 'high', 'water_retention': 'moderate',
                'suitable_crops': ['rice', 'wheat', 'sugarcane', 'maize'],
                'ph_range': (6.0, 8.0), 'organic_matter': 'medium_to_high'
            },
            'black': {
                'drainage': 'poor', 'fertility': 'high', 'water_retention': 'high',
                'suitable_crops': ['cotton', 'soybean', 'wheat', 'sugarcane'],
                'ph_range': (7.0, 8.5), 'organic_matter': 'high'
            },
            'red': {
                'drainage': 'good', 'fertility': 'medium', 'water_retention': 'low',
                'suitable_crops': ['groundnut', 'cotton', 'maize', 'sunflower'],
                'ph_range': (5.5, 7.0), 'organic_matter': 'low_to_medium'
            },
            'laterite': {
                'drainage': 'excellent', 'fertility': 'low', 'water_retention': 'very_low',
                'suitable_crops': ['cashew', 'coconut', 'spices'],
                'ph_range': (4.5, 6.5), 'organic_matter': 'low'
            },
            'desert': {
                'drainage': 'excellent', 'fertility': 'very_low', 'water_retention': 'very_low',
                'suitable_crops': ['drought_tolerant_crops'],
                'ph_range': (7.5, 9.0), 'organic_matter': 'very_low'
            },
            'mountain': {
                'drainage': 'good', 'fertility': 'medium', 'water_retention': 'moderate',
                'suitable_crops': ['temperate_crops'],
                'ph_range': (5.5, 7.5), 'organic_matter': 'medium'
            }
        }
    
    def generate_enhanced_dataset(self, n_samples=10000):
        """Generate enhanced synthetic dataset for training"""
        np.random.seed(42)
        
        data = []
        
        for _ in range(n_samples):
            # Randomly select a crop and generate data around its optimal conditions
            crop = np.random.choice(list(self.crop_database.keys()))
            crop_info = self.crop_database[crop]
            conditions = crop_info['optimal_conditions']
            
            # Add realistic variations around optimal conditions
            temperature = np.random.normal(
                (conditions['temperature'][0] + conditions['temperature'][1]) / 2, 3)
            humidity = np.random.normal(
                (conditions['humidity'][0] + conditions['humidity'][1]) / 2, 5)
            rainfall = np.random.normal(
                (conditions['rainfall'][0] + conditions['rainfall'][1]) / 2, 100)
            
            ph = np.random.normal(
                (conditions['ph'][0] + conditions['ph'][1]) / 2, 0.3)
            n = np.random.normal(
                (conditions['n'][0] + conditions['n'][1]) / 2, 15)
            p = np.random.normal(
                (conditions['p'][0] + conditions['p'][1]) / 2, 8)
            k = np.random.normal(
                (conditions['k'][0] + conditions['k'][1]) / 2, 12)
            
            # Add some noise for more realistic data
            temperature += np.random.normal(0, 2)
            humidity = max(10, min(100, humidity + np.random.normal(0, 3)))
            rainfall = max(0, rainfall + np.random.normal(0, 50))
            ph = max(3, min(10, ph + np.random.normal(0, 0.2)))
            
            # Generate corresponding soil type based on crop preferences
            suitable_soils = crop_info['soil_types']
            soil_type = np.random.choice(suitable_soils)
            
            # Add electrical conductivity and organic carbon
            ec = np.random.uniform(0.5, 2.5)
            oc = np.random.uniform(0.3, 1.5)
            
            data.append([
                n, p, k, temperature, humidity, ph, rainfall, ec, oc, crop, soil_type
            ])
        
        # Add some random samples for edge cases
        for _ in range(n_samples // 5):
            data.append([
                np.random.uniform(20, 300),    # N
                np.random.uniform(5, 100),     # P
                np.random.uniform(20, 300),    # K
                np.random.uniform(5, 45),      # Temperature
                np.random.uniform(20, 95),     # Humidity
                np.random.uniform(3.5, 9.5),   # pH
                np.random.uniform(100, 3000),   # Rainfall
                np.random.uniform(0.1, 3.0),   # EC
                np.random.uniform(0.1, 2.0),   # OC
                np.random.choice(list(self.crop_database.keys())),
                np.random.choice(list(self.soil_characteristics.keys()))
            ])
        
        columns = ['N', 'P', 'K', 'temperature', 'humidity', 'ph', 'rainfall', 
                  'ec', 'oc', 'label', 'soil_type']
        
        return pd.DataFrame(data, columns=columns)
    
    def train_models(self):
        """Train enhanced crop and soil prediction models"""
        print("🤖 Generating enhanced training dataset...")
        df = self.generate_enhanced_dataset(15000)
        
        # Prepare features
        feature_columns = ['N', 'P', 'K', 'temperature', 'humidity', 'ph', 'rainfall', 'ec', 'oc']
        X = df[feature_columns]
        
        # Scale features
        X_scaled = self.scaler.fit_transform(X)
        
        # Prepare crop labels
        y_crop = self.crop_encoder.fit_transform(df['label'])
        
        # Prepare soil labels
        y_soil = self.soil_encoder.fit_transform(df['soil_type'])
        
        print("📊 Training enhanced crop recommendation model...")
        # Use ensemble of multiple algorithms for better accuracy
        self.crop_model = GradientBoostingClassifier(
            n_estimators=200,
            learning_rate=0.1,
            max_depth=6,
            random_state=42,
            subsample=0.8
        )
        
        # Train with cross-validation
        X_train, X_test, y_crop_train, y_crop_test = train_test_split(
            X_scaled, y_crop, test_size=0.2, random_state=42
        )
        
        self.crop_model.fit(X_train, y_crop_train)
        crop_accuracy = self.crop_model.score(X_test, y_crop_test)
        
        # Cross-validation score
        cv_scores = cross_val_score(self.crop_model, X_scaled, y_crop, cv=5)
        
        print(f"✅ Crop Model Accuracy: {crop_accuracy:.1%}")
        print(f"✅ Cross-validation Score: {cv_scores.mean():.1%} (+/- {cv_scores.std() * 2:.1%})")
        
        print("🌱 Training enhanced soil type prediction model...")
        self.soil_model = GradientBoostingClassifier(
            n_estimators=150,
            learning_rate=0.15,
            max_depth=5,
            random_state=42,
            subsample=0.8
        )
        
        # Train soil model
        X_train_soil, X_test_soil, y_soil_train, y_soil_test = train_test_split(
            X_scaled, y_soil, test_size=0.2, random_state=42
        )
        
        self.soil_model.fit(X_train_soil, y_soil_train)
        soil_accuracy = self.soil_model.score(X_test_soil, y_soil_test)
        
        print(f"✅ Soil Model Accuracy: {soil_accuracy:.1%}")
        
        # Save models
        self.save_models()
        
        return crop_accuracy, soil_accuracy
    
    def save_models(self):
        """Save trained models and preprocessors"""
        models_dir = 'ml_models'
        os.makedirs(models_dir, exist_ok=True)
        
        joblib.dump(self.crop_model, f'{models_dir}/enhanced_crop_model.pkl')
        joblib.dump(self.soil_model, f'{models_dir}/enhanced_soil_model.pkl')
        joblib.dump(self.scaler, f'{models_dir}/enhanced_scaler.pkl')
        joblib.dump(self.crop_encoder, f'{models_dir}/enhanced_crop_encoder.pkl')
        joblib.dump(self.soil_encoder, f'{models_dir}/enhanced_soil_encoder.pkl')
        
        # Save crop database
        with open(f'{models_dir}/crop_database.json', 'w') as f:
            json.dump(self.crop_database, f, indent=2)
        
        print(f"💾 Enhanced models saved to {models_dir}/")
    
    def load_models(self):
        """Load pre-trained models"""
        models_dir = 'ml_models'
        
        try:
            self.crop_model = joblib.load(f'{models_dir}/enhanced_crop_model.pkl')
            self.soil_model = joblib.load(f'{models_dir}/enhanced_soil_model.pkl')
            self.scaler = joblib.load(f'{models_dir}/enhanced_scaler.pkl')
            self.crop_encoder = joblib.load(f'{models_dir}/enhanced_crop_encoder.pkl')
            self.soil_encoder = joblib.load(f'{models_dir}/enhanced_soil_encoder.pkl')
            
            return True
        except FileNotFoundError:
            print("⚠️ Model files not found. Training new models...")
            return False
    
    def predict_crop(self, soil_data):
        """Enhanced crop prediction with confidence scores"""
        try:
            # Prepare input data
            features = np.array([[
                soil_data['n'], soil_data['p'], soil_data['k'],
                soil_data['temperature'], soil_data['humidity'], soil_data['ph'],
                soil_data['rainfall'], soil_data.get('ec', 1.0), soil_data.get('oc', 0.8)
            ]])
            
            # Scale features
            features_scaled = self.scaler.transform(features)
            
            # Get prediction probabilities
            probabilities = self.crop_model.predict_proba(features_scaled)[0]
            
            # Get crop names
            crop_names = self.crop_encoder.classes_
            
            # Sort by confidence
            crop_confidence_pairs = list(zip(crop_names, probabilities))
            crop_confidence_pairs.sort(key=lambda x: x[1], reverse=True)
            
            # Get top recommendation
            top_crop = crop_confidence_pairs[0][0]
            top_confidence = crop_confidence_pairs[0][1]
            
            # Get all recommendations with confidence > 0.1
            all_recommendations = [
                {"crop": crop, "confidence": float(conf)}
                for crop, conf in crop_confidence_pairs if conf > 0.05
            ]
            
            # Add suitability analysis
            suitability_analysis = self.analyze_crop_suitability(soil_data, top_crop)
            
            return {
                "success": True,
                "recommended_crop": top_crop,
                "confidence": float(top_confidence),
                "top_recommendations": all_recommendations[:5],
                "suitability_analysis": suitability_analysis
            }
            
        except Exception as e:
            return {
                "success": False,
                "error": str(e),
                "recommended_crop": "maize",  # Safe fallback
                "confidence": 0.5,
                "top_recommendations": []
            }
    
    def predict_soil_type(self, soil_data):
        """Enhanced soil type prediction"""
        try:
            # Prepare input data
            features = np.array([[
                soil_data['n'], soil_data['p'], soil_data['k'],
                soil_data['temperature'], soil_data['humidity'], soil_data['ph'],
                soil_data['rainfall'], soil_data.get('ec', 1.0), soil_data.get('oc', 0.8)
            ]])
            
            # Scale features
            features_scaled = self.scaler.transform(features)
            
            # Get prediction probabilities
            probabilities = self.soil_model.predict_proba(features_scaled)[0]
            
            # Get soil type names
            soil_names = self.soil_encoder.classes_
            
            # Sort by confidence
            soil_confidence_pairs = list(zip(soil_names, probabilities))
            soil_confidence_pairs.sort(key=lambda x: x[1], reverse=True)
            
            # Get top prediction
            top_soil = soil_confidence_pairs[0][0]
            top_confidence = soil_confidence_pairs[0][1]
            
            # Get all predictions with confidence > 0.1
            all_predictions = [
                {"soil_type": soil, "confidence": float(conf)}
                for soil, conf in soil_confidence_pairs if conf > 0.05
            ]
            
            return {
                "success": True,
                "soil_type": top_soil,
                "confidence": float(top_confidence),
                "top_predictions": all_predictions[:3]
            }
            
        except Exception as e:
            return {
                "success": False,
                "error": str(e),
                "soil_type": "alluvial",  # Safe fallback
                "confidence": 0.5,
                "top_predictions": []
            }
    
    def analyze_crop_suitability(self, soil_data, crop):
        """Analyze suitability of crop for given conditions"""
        if crop not in self.crop_database:
            return {"suitability_score": 0.5, "recommendations": ["Crop data not available"]}
        
        crop_info = self.crop_database[crop]
        conditions = crop_info['optimal_conditions']
        
        suitability_factors = {}
        recommendations = []
        
        # Analyze each parameter
        for param, optimal_range in conditions.items():
            if param in soil_data:
                value = soil_data[param]
                min_val, max_val = optimal_range
                
                if min_val <= value <= max_val:
                    suitability_factors[param] = 1.0
                else:
                    # Calculate how far outside the range
                    if value < min_val:
                        deviation = (min_val - value) / min_val
                        recommendations.append(f"Increase {param}: current {value:.1f}, optimal {min_val}-{max_val}")
                    else:
                        deviation = (value - max_val) / max_val
                        recommendations.append(f"Reduce {param}: current {value:.1f}, optimal {min_val}-{max_val}")
                    
                    suitability_factors[param] = max(0.0, 1.0 - deviation)
        
        # Calculate overall suitability score
        suitability_score = np.mean(list(suitability_factors.values())) if suitability_factors else 0.5
        
        # Add management recommendations
        if suitability_score > 0.8:
            recommendations.append("✅ Excellent conditions for this crop")
        elif suitability_score > 0.6:
            recommendations.append("✅ Good conditions with minor adjustments needed")
        elif suitability_score > 0.4:
            recommendations.append("⚠️ Marginal conditions - consider soil amendments")
        else:
            recommendations.append("❌ Poor conditions - consider alternative crops")
        
        return {
            "suitability_score": float(suitability_score),
            "parameter_scores": suitability_factors,
            "recommendations": recommendations
        }

def main():
    if len(sys.argv) < 2:
        print("Usage: python enhanced_ml_models.py <command> [args...]")
        print("Commands:")
        print("  train                    - Train new enhanced models")
        print("  predict_crop <json>      - Predict crop for soil data")
        print("  predict_soil <json>      - Predict soil type for data")
        sys.exit(1)
    
    command = sys.argv[1]
    ml_model = EnhancedMLModel()
    
    if command == "train":
        print("🚀 Training Enhanced ML Models for Fasal Sathi...")
        crop_acc, soil_acc = ml_model.train_models()
        print(f"\n✅ Training Complete!")
        print(f"📊 Final Accuracies - Crop: {crop_acc:.1%}, Soil: {soil_acc:.1%}")
        
    elif command == "predict_crop":
        if len(sys.argv) < 3:
            print("Error: Missing soil data JSON")
            sys.exit(1)
        
        # Load models
        if not ml_model.load_models():
            ml_model.train_models()
        
        # Parse input
        soil_data = json.loads(sys.argv[2])
        result = ml_model.predict_crop(soil_data)
        print(json.dumps(result, indent=2))
        
    elif command == "predict_soil":
        if len(sys.argv) < 3:
            print("Error: Missing soil data JSON")
            sys.exit(1)
        
        # Load models
        if not ml_model.load_models():
            ml_model.train_models()
        
        # Parse input
        soil_data = json.loads(sys.argv[2])
        result = ml_model.predict_soil_type(soil_data)
        print(json.dumps(result, indent=2))
        
    else:
        print(f"Unknown command: {command}")
        sys.exit(1)

if __name__ == "__main__":
    main()