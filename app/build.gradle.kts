plugins {
    alias(libs.plugins.android.application)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "com.mytechnology.video.vgplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mytechnology.video.vgplayer"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.1"

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
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.media3.exoplayer)
    implementation (libs.media3.exoplayer.dash)
    implementation (libs.media3.ui)

    implementation(libs.glide)

    implementation(libs.review)
    implementation(libs.play.services.oss.licenses)

    implementation("androidx.media3:media3-transformer:1.7.1")
    


}