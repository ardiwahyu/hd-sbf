import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinParcelize)
}

val secretPropertiesFile = rootProject.file("secret.properties")
val secretProperties = Properties()
secretProperties.load(FileInputStream(secretPropertiesFile))

android {
    namespace = "com.bm.hdsbf"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bm.hdsbf"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL_SPREADSHEET", secretProperties["BASE_URL_SPREADSHEET"].toString())
        buildConfigField("String", "BASE_URL_DRIVE", secretProperties["BASE_URL_DRIVE"].toString())
        buildConfigField("String", "API_KEY", secretProperties["API_KEY"].toString())
        buildConfigField("String", "SPREADSHEET_ID", secretProperties["SPREADSHEET_ID"].toString())
        buildConfigField("String", "APP_ID", secretProperties["APP_ID"].toString())
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.calendar)

    implementation(libs.jsoup)

    implementation(libs.hilt.android)
    implementation(libs.hilt.common)
    implementation(libs.hilt.work)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.livedata)

    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.room.runtime)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.worker)

    implementation(libs.progressbar)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
}