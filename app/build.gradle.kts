plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.newtube"
    compileSdk = 35 // hoặc 34

    defaultConfig {
        applicationId = "com.example.newtube"
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Các thư viện cơ bản từ catalog
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Các thư viện khác sử dụng alias từ catalog
    implementation(libs.core.splashscreen)
    implementation(libs.glide)
    implementation(libs.viewpager2)

    // ExoPlayer / Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer.dash) // Nếu dùng
    implementation(libs.media3.exoplayer.hls)  // Nếu dùng

    // androidx.media & fragment
    implementation(libs.androidx.media) // Cho MediaStyle
    implementation(libs.fragment)      // Cho Fragment
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

}