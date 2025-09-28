# FasalSathi - Agricultural Companion App 🌾

> **Smart Agricultural Assistant for Farmers in India**

FasalSathi is a comprehensive Android application designed to empower farmers with modern technology, providing weather information, AI-powered agricultural guidance, and smart farming recommendations.

## 🌟 Features

### 📱 Core Functionality
- **Dashboard Overview**: Clean, intuitive interface with quick access to all features
- **User Profile Management**: Personalized farmer profiles with location-based services
- **Multi-language Support**: Available in multiple Indian languages

### 🌤️ Weather Integration
- **Real-time Weather Data**: Integration with OpenWeatherMap API
- **Location-based Forecasting**: Weather information for 200+ Indian cities
- **Agricultural Weather Insights**: Farming recommendations based on weather conditions
- **Enhanced Simulation**: Fallback system with realistic weather data

### 🤖 AI-Powered FAQ Assistant
- **Voice Recognition**: Ask questions using voice input
- **Image Analysis**: Upload crop photos for AI-powered analysis
- **Text-to-Speech**: Listen to AI responses
- **Chat Interface**: Interactive conversation with agricultural AI assistant
- **Multilingual Support**: Questions and answers in local languages

### 🏛️ Government Schemes
- **Scheme Information**: Access to various agricultural government schemes
- **Eligibility Checker**: Check eligibility for different programs
- **Application Guidance**: Step-by-step application process help

## 🛠️ Technical Stack

### Mobile Development
- **Language**: Kotlin
- **Platform**: Android (API 21+)
- **Architecture**: MVVM with Clean Architecture
- **UI Framework**: Material Design 3
- **Database**: Room (SQLite)

### AI & Machine Learning
- **Voice Recognition**: Android Speech Recognition API
- **Text-to-Speech**: Android TTS Engine
- **Image Analysis**: Custom AI models for crop analysis
- **Natural Language Processing**: For multilingual support

### APIs & Services
- **Weather API**: OpenWeatherMap integration
- **Location Services**: GPS and network-based location
- **Cloud Storage**: For user data and preferences

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK (API 21 or higher)
- Kotlin 1.9+
- Internet connection for weather data

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/SIH25.git
   cd SIH25
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Configure API Keys**
   ```kotlin
   // In WeatherService.kt
   private const val OPENWEATHER_API_KEY = "your_api_key_here"
   private const val USE_REAL_API = true // Set to false to use simulation
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Project Structure

```
├── app/
│   ├── build.gradle.kts          # App-level build configuration
│   ├── proguard-rules.pro        # ProGuard rules for code obfuscation
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/myandroidapp/
│       │   │   └── MainActivity.kt
│       │   └── res/
│       │       ├── layout/
│       │       │   └── activity_main.xml
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       ├── values-night/
│       │       │   └── themes.xml
│       │       └── xml/
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       ├── test/
│       │   └── java/com/example/myandroidapp/
│       │       └── ExampleUnitTest.kt
│       └── androidTest/
│           └── java/com/example/myandroidapp/
│               └── ExampleInstrumentedTest.kt
├── build.gradle.kts              # Project-level build configuration
├── gradle.properties             # Gradle properties
├── settings.gradle.kts           # Gradle settings
└── gradlew                       # Gradle wrapper script
```

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Android SDK
- Android Studio or VS Code with Android extensions

## Building the Project

To build the project:

```bash
./gradlew build
```

To build a debug APK:

```bash
./gradlew assembleDebug
```

To build a release APK:

```bash
./gradlew assembleRelease
```

## Running the Project

To run on an emulator or connected device:

```bash
./gradlew installDebug
```

## Project Configuration

- **Package Name**: `com.example.myandroidapp`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## Dependencies

- AndroidX Core KTX
- AndroidX AppCompat
- Material Design Components
- ConstraintLayout
- JUnit for testing
- Espresso for UI testing

## Development

The main activity is located at `app/src/main/java/com/example/myandroidapp/MainActivity.kt` and displays a simple "Hello World!" message.

You can customize the app by:
1. Modifying the layout in `app/src/main/res/layout/activity_main.xml`
2. Adding new activities and resources
3. Updating the app name and package in the configuration files
4. Adding new dependencies in `app/build.gradle.kts`