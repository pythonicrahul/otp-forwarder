plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.otpforwarder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.otpforwarder"
        minSdk = 34
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.work.runtime)
    implementation(libs.room.runtime)
    implementation(libs.core)
    androidTestImplementation(libs.testng)
    annotationProcessor(libs.room.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}