# FasalSathi - Agricultural Companion App 🌾

> **Smart Agricultural Assistant for Farmers in India**

FasalSathi is a comprehensive Android application designed to empower farmers with modern technology, providing weather information, AI-powered agricultural guidance, and smart farming recommendations.

## 🌟 Key Features

- Bottom navigation bar with 5 tabs (Home, Crops, Detect, Market, Profile)
- Dark mode functionality
- Multi-language support (English, Hindi, Hinglish)
- Dashboard with greeting and weather sections
- AI Assistant integration
- Profile management
- Weather information display
- Navigation drawer with comprehensive menu

## 📱 Activities

- Dashboard, AI Assistant, Profile, Settings, Weather
- CropRecommendationActivity, DiseaseDetectionActivity, MarketActivity

## 🛠️ UI Components

- Material Design 3, Bottom Navigation, Cards, FAB
- Light/Dark mode support
- Multi-language with LanguageManager

## 🧠 Machine Learning Models

- **Crop Recommendation Model**: Uses soil health, weather, and location features to suggest optimal crops for farmers. Trained on realistic datasets and deployed for fast inference.
- **Soil Health Model**: Predicts soil type and health using soil parameters and image classification. Supports soil health card data and real-time image analysis.
- **Disease Detection Model**: AI-powered image classifier for crop disease detection using photos taken in the app.
- **Model Files**: Located in `ml_models/` and `ml_pipeline/models/` (see `.pkl`, `.joblib`, `.h5` files).
- **Integration**: ML models are integrated via the `MLPredictor.java` bridge and Python backend scripts for advanced recommendations.

## 🏗️ App Architecture

- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Layers**:
  - Presentation: Activities, Fragments, ViewModels
  - Domain: Use Cases, Business Logic
  - Data: Repositories, Data Sources (local/remote)
- **Navigation**: Bottom navigation and navigation drawer for seamless user experience.
- **Dependency Injection**: Modularized for scalability and testability.
- **Multi-language & Theme**: Managed by LanguageManager and ThemeManager classes.

## 🌱 Soil Parameters & Features

- **Supported Soil Parameters**:
  - pH, EC, OC, N, P, K, S, Zn, Fe, Cu, Mn, B
  - Soil texture, moisture, and color
- **Soil Health Card Integration**: Accepts government soil health card data for personalized recommendations.
- **Location Features**: Uses GPS/location for region-specific advice and weather data.
- **Irrigation Features**: Supports irrigation type and scheduling for crop planning.

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest recommended)
- Android SDK (API 34+)
- Kotlin 1.9+
- Internet connection for weather data

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/InnovateLabsX/FasalSathi.git
   cd FasalSathi
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Project Structure

```
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/fasalsaathi/
│       │   └── res/
│       │       ├── layout/
│       │       ├── values/
│       │       ├── values-night/
│       │       └── xml/
│       ├── test/
│       └── androidTest/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── ml_models/
├── ml_pipeline/
└── gradlew
```

## Configuration

- **Package Name**: `com.fasalsaathi.app`
- **Min SDK**: 24
- **Target SDK**: 34
- **Compile SDK**: 34

## Development

- All activities are registered in `AndroidManifest.xml`
- Bottom navigation fully functional
- Modern UI with Material Design 3
- AI and ML integration for crop and disease recommendations
- Weather API integration
- Multi-language and dark mode support



