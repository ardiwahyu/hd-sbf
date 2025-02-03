import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.googleService)
}

val secretPropertiesFile = rootProject.file("secret.properties")
val secretProperties = Properties()
secretProperties.load(FileInputStream(secretPropertiesFile))

android {
    namespace = "com.bm.hdsbf"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file(secretProperties["RELEASE_STORE_FILE"].toString())
            storePassword = secretProperties["RELEASE_STORE_PASSWORD"].toString()
            keyAlias = secretProperties["RELEASE_KEY_ALIAS"].toString()
            keyPassword = secretProperties["RELEASE_KEY_PASSWORD"].toString()
        }
    }

    defaultConfig {
        applicationId = "com.bm.hdsbf"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "2.2"
        setProperty("archivesBaseName", "HDSBF-v$versionName-$versionCode")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL_SPREADSHEET", secretProperties["BASE_URL_SPREADSHEET"].toString())
        buildConfigField("String", "BASE_URL_DRIVE", secretProperties["BASE_URL_DRIVE"].toString())
        buildConfigField("String", "API_KEY", secretProperties["API_KEY"].toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.activity)
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

    implementation(platform(libs.firebase.boom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
}