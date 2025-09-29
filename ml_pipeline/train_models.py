#!/usr/bin/env python3
"""
Comprehensive ML Pipeline for Fasal Sathi
Includes Soil Type Detection and Crop Recommendation
Using Random Forest and Ensemble Methods
"""

import pandas as pd
import numpy as np
import cv2
import os
import joblib
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
from sklearn.model_selection import train_test_split, cross_val_score, GridSearchCV
from sklearn.ensemble import RandomForestClassifier, VotingClassifier, GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
from sklearn.utils import class_weight
from imblearn.over_sampling import SMOTE
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
import warnings
warnings.filterwarnings('ignore')

class FasalSaathiMLPipeline:
    def __init__(self, datasets_path="/home/wizardking/Documents/Projects/SIHv2/SIH25/Datasets"):
        self.datasets_path = Path(datasets_path)
        self.models = {}
        self.scalers = {}
        self.encoders = {}
        self.model_save_path = Path("models")
        self.model_save_path.mkdir(exist_ok=True)
        
        # Initialize data containers
        self.soil_data = None
        self.crop_data = None
        self.image_data = None
        
        # Soil type mapping
        self.soil_types = {
            'Alluvial': 0, 'Black': 1, 'Red': 2, 'Laterite': 3,
            'Desert': 4, 'Mountain': 5, 'Arid': 6, 'Yellow': 7
        }
        
        print("🌱 Fasal Sathi ML Pipeline Initialized")
        print(f"📂 Datasets path: {self.datasets_path}")
    
    def load_csv_datasets(self):
        """Load and combine all CSV datasets"""
        print("\n📊 Loading CSV datasets...")
        
        # Load main datasets
        datasets = {}
        
        try:
            # Realistic crop soil dataset
            realistic_df = pd.read_csv(self.datasets_path / "realistic_crop_soil_dataset.csv")
            print(f"✅ Loaded realistic_crop_soil_dataset.csv: {realistic_df.shape}")
            datasets['realistic'] = realistic_df
            
            # ML soil health dataset
            soil_health_df = pd.read_csv(self.datasets_path / "ml_soil_health_dataset.csv")
            print(f"✅ Loaded ml_soil_health_dataset.csv: {soil_health_df.shape}")
            datasets['soil_health'] = soil_health_df
            
            # Sample soil health card data
            try:
                sample_df = pd.read_csv(self.datasets_path / "sample_soil_health_card_data.csv")
                print(f"✅ Loaded sample_soil_health_card_data.csv: {sample_df.shape}")
                datasets['sample'] = sample_df
            except:
                print("⚠️  sample_soil_health_card_data.csv not found or has issues")
            
        except Exception as e:
            print(f"❌ Error loading datasets: {e}")
            return None
            
        return datasets
    
    def preprocess_data(self, datasets):
        """Preprocess and combine datasets"""
        print("\n🔄 Preprocessing data...")
        
        # Combine datasets with common columns
        combined_data = []
        
        for name, df in datasets.items():
            print(f"Processing {name} dataset...")
            
            # Standardize column names
            df.columns = df.columns.str.lower().str.strip()
            
            # Select common features for soil and crop prediction
            feature_columns = [
                'n', 'p', 'k', 'ph', 'temperature', 'humidity', 'rainfall'
            ]
            
            # Additional features if available
            optional_features = ['ec', 'oc', 's', 'zn', 'fe', 'cu', 'mn', 'b']
            for col in optional_features:
                if col in df.columns:
                    feature_columns.append(col)
            
            # Target columns
            if 'soil_type' in df.columns and 'crop_recommended' in df.columns:
                target_columns = ['soil_type', 'crop_recommended']
                
                # Select available columns
                available_columns = [col for col in feature_columns + target_columns if col in df.columns]
                subset_df = df[available_columns].copy()
                
                # Remove rows with missing critical values
                subset_df = subset_df.dropna(subset=['soil_type', 'crop_recommended'])
                
                combined_data.append(subset_df)
                print(f"  ✅ Added {len(subset_df)} rows from {name}")
        
        if not combined_data:
            print("❌ No valid data found")
            return None, None
            
        # Combine all datasets
        combined_df = pd.concat(combined_data, ignore_index=True)
        print(f"📈 Combined dataset shape: {combined_df.shape}")
        
        # Separate features and targets
        feature_cols = [col for col in combined_df.columns if col not in ['soil_type', 'crop_recommended']]
        X = combined_df[feature_cols]
        y_soil = combined_df['soil_type']
        y_crop = combined_df['crop_recommended']
        
        # Handle missing values
        X = X.fillna(X.median())
        
        print(f"🎯 Features shape: {X.shape}")
        print(f"🏷️  Soil types: {y_soil.nunique()} unique types")
        print(f"🌾 Crops: {y_crop.nunique()} unique crops")
        
        return X, y_soil, y_crop
    
    def load_soil_images(self):
        """Load and preprocess soil images"""
        print("\n🖼️  Loading soil images...")
        
        image_data = []
        labels = []
        
        # Process CyAUG-Dataset
        cyaug_path = self.datasets_path / "CyAUG-Dataset"
        if cyaug_path.exists():
            for soil_type in cyaug_path.iterdir():
                if soil_type.is_dir():
                    soil_name = soil_type.name.replace('_', ' ').replace('Soil', '').strip()
                    print(f"  Processing {soil_name} images...")
                    
                    image_count = 0
                    for img_file in soil_type.glob("*"):
                        if img_file.suffix.lower() in ['.jpg', '.jpeg', '.png', '.webp']:
                            try:
                                # Load and preprocess image
                                img = cv2.imread(str(img_file))
                                if img is not None:
                                    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
                                    img = cv2.resize(img, (224, 224))
                                    img = img / 255.0  # Normalize
                                    
                                    image_data.append(img)
                                    labels.append(soil_name)
                                    image_count += 1
                                    
                                    # Limit images per class to prevent memory issues
                                    if image_count >= 500:
                                        break
                            except Exception as e:
                                continue
                    
                    print(f"    ✅ Loaded {image_count} images for {soil_name}")
        
        if image_data:
            X_images = np.array(image_data)
            y_images = np.array(labels)
            print(f"🖼️  Total images loaded: {X_images.shape}")
            return X_images, y_images
        else:
            print("⚠️  No images loaded")
            return None, None
    
    def train_crop_recommendation_model(self, X, y_crop):
        """Train crop recommendation model using ensemble methods"""
        print("\n🌾 Training Crop Recommendation Model...")
        
        # Encode crop labels
        crop_encoder = LabelEncoder()
        y_crop_encoded = crop_encoder.fit_transform(y_crop)
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X, y_crop_encoded, test_size=0.2, random_state=42, stratify=y_crop_encoded
        )
        
        # Scale features
        scaler = StandardScaler()
        X_train_scaled = scaler.fit_transform(X_train)
        X_test_scaled = scaler.transform(X_test)
        
        # Handle class imbalance
        smote = SMOTE(random_state=42)
        X_train_balanced, y_train_balanced = smote.fit_resample(X_train_scaled, y_train)
        
        # Define models
        models = {
            'random_forest': RandomForestClassifier(
                n_estimators=200,
                max_depth=15,
                min_samples_split=5,
                min_samples_leaf=2,
                random_state=42,
                n_jobs=-1
            ),
            'gradient_boosting': GradientBoostingClassifier(
                n_estimators=100,
                learning_rate=0.1,
                max_depth=10,
                random_state=42
            ),
            'svm': SVC(
                kernel='rbf',
                C=1.0,
                probability=True,
                random_state=42
            )
        }
        
        # Train individual models
        trained_models = []
        model_scores = {}
        
        for name, model in models.items():
            print(f"  Training {name}...")
            model.fit(X_train_balanced, y_train_balanced)
            
            # Evaluate
            y_pred = model.predict(X_test_scaled)
            accuracy = accuracy_score(y_test, y_pred)
            model_scores[name] = accuracy
            trained_models.append((name, model))
            
            print(f"    {name} accuracy: {accuracy:.4f}")
        
        # Create ensemble model
        ensemble = VotingClassifier(
            estimators=trained_models,
            voting='soft'
        )
        
        print("  Training ensemble model...")
        ensemble.fit(X_train_balanced, y_train_balanced)
        
        # Evaluate ensemble
        y_pred_ensemble = ensemble.predict(X_test_scaled)
        ensemble_accuracy = accuracy_score(y_test, y_pred_ensemble)
        print(f"    Ensemble accuracy: {ensemble_accuracy:.4f}")
        
        # Save models
        self.models['crop_recommendation'] = ensemble
        self.scalers['crop_scaler'] = scaler
        self.encoders['crop_encoder'] = crop_encoder
        
        # Save to disk
        joblib.dump(ensemble, self.model_save_path / 'crop_recommendation_model.joblib')
        joblib.dump(scaler, self.model_save_path / 'crop_scaler.joblib')
        joblib.dump(crop_encoder, self.model_save_path / 'crop_encoder.joblib')
        
        # Print detailed classification report
        print("\n📊 Crop Recommendation Model Performance:")
        print(classification_report(y_test, y_pred_ensemble, 
                                  target_names=crop_encoder.classes_))
        
        return ensemble_accuracy
    
    def train_soil_type_model(self, X, y_soil):
        """Train soil type classification model"""
        print("\n🏔️  Training Soil Type Classification Model...")
        
        # Encode soil labels
        soil_encoder = LabelEncoder()
        y_soil_encoded = soil_encoder.fit_transform(y_soil)
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X, y_soil_encoded, test_size=0.2, random_state=42, stratify=y_soil_encoded
        )
        
        # Scale features
        scaler = StandardScaler()
        X_train_scaled = scaler.fit_transform(X_train)
        X_test_scaled = scaler.transform(X_test)
        
        # Handle class imbalance
        smote = SMOTE(random_state=42)
        X_train_balanced, y_train_balanced = smote.fit_resample(X_train_scaled, y_train)
        
        # Random Forest with GridSearch
        param_grid = {
            'n_estimators': [100, 200, 300],
            'max_depth': [10, 15, 20, None],
            'min_samples_split': [2, 5, 10],
            'min_samples_leaf': [1, 2, 4]
        }
        
        rf = RandomForestClassifier(random_state=42, n_jobs=-1)
        
        print("  Performing GridSearch for optimal parameters...")
        grid_search = GridSearchCV(
            rf, param_grid, cv=5, scoring='accuracy', n_jobs=-1, verbose=1
        )
        
        grid_search.fit(X_train_balanced, y_train_balanced)
        
        best_model = grid_search.best_estimator_
        print(f"  Best parameters: {grid_search.best_params_}")
        
        # Evaluate
        y_pred = best_model.predict(X_test_scaled)
        accuracy = accuracy_score(y_test, y_pred)
        print(f"  Soil Type Model accuracy: {accuracy:.4f}")
        
        # Save models
        self.models['soil_type'] = best_model
        self.scalers['soil_scaler'] = scaler
        self.encoders['soil_encoder'] = soil_encoder
        
        # Save to disk
        joblib.dump(best_model, self.model_save_path / 'soil_type_model.joblib')
        joblib.dump(scaler, self.model_save_path / 'soil_scaler.joblib')
        joblib.dump(soil_encoder, self.model_save_path / 'soil_encoder.joblib')
        
        # Print detailed classification report
        print("\n📊 Soil Type Model Performance:")
        print(classification_report(y_test, y_pred, 
                                  target_names=soil_encoder.classes_))
        
        # Feature importance
        feature_importance = pd.DataFrame({
            'feature': X.columns,
            'importance': best_model.feature_importances_
        }).sort_values('importance', ascending=False)
        
        print("\n🔝 Top 10 Feature Importances:")
        print(feature_importance.head(10))
        
        return accuracy
    
    def train_soil_image_classifier(self, X_images, y_images):
        """Train CNN for soil image classification"""
        if X_images is None or y_images is None:
            print("⚠️  No image data available for training")
            return None
            
        print("\n🖼️  Training Soil Image Classification Model...")
        
        # Encode labels
        image_encoder = LabelEncoder()
        y_encoded = image_encoder.fit_transform(y_images)
        
        # Convert to categorical
        num_classes = len(np.unique(y_encoded))
        y_categorical = tf.keras.utils.to_categorical(y_encoded, num_classes)
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X_images, y_categorical, test_size=0.2, random_state=42, stratify=y_encoded
        )
        
        # Build CNN model
        model = keras.Sequential([
            layers.Conv2D(32, (3, 3), activation='relu', input_shape=(224, 224, 3)),
            layers.MaxPooling2D((2, 2)),
            layers.Conv2D(64, (3, 3), activation='relu'),
            layers.MaxPooling2D((2, 2)),
            layers.Conv2D(128, (3, 3), activation='relu'),
            layers.MaxPooling2D((2, 2)),
            layers.Conv2D(128, (3, 3), activation='relu'),
            layers.MaxPooling2D((2, 2)),
            layers.Flatten(),
            layers.Dropout(0.5),
            layers.Dense(512, activation='relu'),
            layers.Dropout(0.5),
            layers.Dense(num_classes, activation='softmax')
        ])
        
        model.compile(
            optimizer='adam',
            loss='categorical_crossentropy',
            metrics=['accuracy']
        )
        
        print("  CNN Architecture:")
        model.summary()
        
        # Train model
        history = model.fit(
            X_train, y_train,
            batch_size=32,
            epochs=20,
            validation_data=(X_test, y_test),
            verbose=1
        )
        
        # Evaluate
        test_loss, test_accuracy = model.evaluate(X_test, y_test, verbose=0)
        print(f"  Image Classification Model accuracy: {test_accuracy:.4f}")
        
        # Save model
        model.save(self.model_save_path / 'soil_image_classifier.h5')
        joblib.dump(image_encoder, self.model_save_path / 'image_encoder.joblib')
        
        self.models['soil_image'] = model
        self.encoders['image_encoder'] = image_encoder
        
        return test_accuracy
    
    def create_android_compatible_models(self):
        """Convert models to Android-compatible format"""
        print("\n📱 Creating Android-compatible models...")
        
        # Create simplified prediction functions
        android_models = {
            'crop_recommendation': {
                'model_path': 'crop_recommendation_model.joblib',
                'scaler_path': 'crop_scaler.joblib',
                'encoder_path': 'crop_encoder.joblib'
            },
            'soil_type': {
                'model_path': 'soil_type_model.joblib',
                'scaler_path': 'soil_scaler.joblib',
                'encoder_path': 'soil_encoder.joblib'
            }
        }
        
        # Save metadata
        import json
        with open(self.model_save_path / 'model_metadata.json', 'w') as f:
            json.dump(android_models, f, indent=2)
        
        print("✅ Android-compatible models created")
    
    def run_complete_training(self):
        """Run the complete training pipeline"""
        print("🚀 Starting Complete ML Training Pipeline")
        print("=" * 60)
        
        # Load datasets
        datasets = self.load_csv_datasets()
        if not datasets:
            print("❌ Failed to load datasets")
            return
        
        # Preprocess data
        X, y_soil, y_crop = self.preprocess_data(datasets)
        if X is None:
            print("❌ Failed to preprocess data")
            return
        
        # Train models
        results = {}
        
        # Train crop recommendation model
        try:
            crop_accuracy = self.train_crop_recommendation_model(X, y_crop)
            results['crop_recommendation'] = crop_accuracy
        except Exception as e:
            print(f"❌ Error training crop model: {e}")
        
        # Train soil type model
        try:
            soil_accuracy = self.train_soil_type_model(X, y_soil)
            results['soil_type'] = soil_accuracy
        except Exception as e:
            print(f"❌ Error training soil model: {e}")
        
        # Load and train on images
        try:
            X_images, y_images = self.load_soil_images()
            if X_images is not None:
                image_accuracy = self.train_soil_image_classifier(X_images, y_images)
                results['soil_image'] = image_accuracy
        except Exception as e:
            print(f"❌ Error training image model: {e}")
        
        # Create Android-compatible models
        self.create_android_compatible_models()
        
        # Print final results
        print("\n🎉 Training Complete!")
        print("=" * 60)
        print("📊 Model Accuracies:")
        for model_name, accuracy in results.items():
            print(f"  {model_name}: {accuracy:.4f} ({accuracy*100:.2f}%)")
        
        print(f"\n💾 Models saved in: {self.model_save_path}")
        print("🔗 Ready for Android integration!")
        
        return results

if __name__ == "__main__":
    # Initialize and run the ML pipeline
    pipeline = FasalSaathiMLPipeline()
    results = pipeline.run_complete_training()