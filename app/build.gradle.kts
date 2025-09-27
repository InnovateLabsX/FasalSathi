plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.fasalsaathi.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fasalsaathi.app"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildToolsVersion = "35.0.1"
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Room Database (simplified)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Circle Image View for profile pictures
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // For future AI enhancements (commented out for now)
    // implementation("org.tensorflow:tensorflow-lite:2.13.0")
    // implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}