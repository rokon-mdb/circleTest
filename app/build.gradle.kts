plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.testcircle"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.testcircle"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //circular imageview
    implementation(libs.circleimageview)

    // agora rtm and rtc
    implementation("io.agora.rtm:rtm-sdk:1.5.3")
    implementation("io.agora.rtc:full-sdk:4.3.2")
    implementation("com.guolindev.permissionx:permissionx:1.7.1")

    //ImagePicker
//    implementation("com.github.dhaval2404:imagepicker:2.1")
//    implementation("com.github.SimformSolutionsPvtLtd:SSImagePicker:2.3")
    // SVGA
//    implementation(libs.svgaplayer.android)

    // Banuba Face AR dependency for playing AR filters
//    implementation(libs.banuba.sdk)
//
//    // Banuba extension for Agora
//    implementation(libs.agora.extension)
}
