import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.fahim.uber_quickride"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fahim.uber_quickride"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
//        debug {
//            buildConfigField("String", "MAPS_API_KEY", "\"${project.property("MAPS_API_KEY")}\"")
//        }
        debug {
            resValue(
                "string",
                "MAPS_API_KEY",
                gradleLocalProperties(rootDir,project.providers).getProperty("MAPS_API_KEY")
            )
        }
        release {
            resValue(
                "string",
                "MAPS_API_KEY",
                gradleLocalProperties(rootDir,project.providers).getProperty("MAPS_API_KEY")
            )
//            buildConfigField("String", "MAPS_API_KEY", "\"${project.property("MAPS_API_KEY")}\"")
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation ("com.google.maps:google-maps-services:0.19.0")
    implementation(project(":simulator"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}