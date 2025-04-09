plugins {
    alias(libs.plugins.android.application)
    // Nếu bạn không dùng version catalog 'libs', cách khai báo plugin thông thường là:
    // id 'com.android.application'
}

android {
    namespace = "com.example.newtube"
    compileSdk = 35 // Lưu ý: compileSdk 35 (Android 15) có thể còn mới, 34 (Android 14) ổn định hơn nếu bạn gặp vấn đề build

    defaultConfig {
        applicationId = "com.example.newtube"
        minSdk = 24
        targetSdk = 34 // targetSdk nên khớp với phiên bản Android bạn đang test chính
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
    // Nếu bạn dùng View Binding hoặc Data Binding, cần thêm block buildFeatures ở đây
    // buildFeatures {
    //     viewBinding = true
    // }
}

dependencies {
    // Các thư viện từ libs (version catalog) - GIỮ NGUYÊN
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout) // Đã bao gồm constraintlayout

    // Testing - GIỮ NGUYÊN
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Có thể không cần dòng này nếu libs.constraintlayout đã có version mới hơn
    // implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // annotationProcessor("com.github.bumptech.glide:compiler:4.12.0") // Không cần cho Java

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0") // Kiểm tra version mới hơn nếu cần

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1") // GIỮ LẠI MỘT DÒNG
    // implementation("androidx.media3:media3-exoplayer-smoothstreaming:1.3.1") // Thêm nếu cần SmoothStreaming

    // *** THÊM CÁC DEPENDENCY CÒN THIẾU ***
    implementation("androidx.media:media:1.7.0") // **QUAN TRỌNG: Cho MediaStyle Notification**
    implementation("androidx.fragment:fragment:1.7.1") // **QUAN TRỌNG: Khai báo rõ ràng cho Fragment (Kiểm tra version mới nhất)**
    // Nếu bạn dùng Kotlin thì dùng: implementation("androidx.fragment:fragment-ktx:1.7.1")

    // (Chưa cần ngay) Retrofit và Gson cho API
    // implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    // implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    // implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0' // Hoặc version mới hơn
}