plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
}

android {
    namespace = "com.rafiq"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rafiq.dating"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        compose = true
    }
    
    // compose compiler is now handled by org.jetbrains.kotlin.plugin.compose
    
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.composables:icons-lucide-android:2.2.1")
    implementation("androidx.emoji2:emoji2-emojipicker:1.5.0")
    
    // Hilt Dependency Injection
    // Hilt Dependency Injection (using modern KSP instead of KAPT)
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")
    ksp("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    
    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.3"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Hilt Navigation Compose (Fixes hiltViewModel unresolved reference)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Compose Animations & Foundation (Premium UI)
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui-util")

    // Coil 3 (Image Loading)
    implementation("io.coil-kt.coil3:coil-compose:3.0.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0")

    // Rive
    implementation("app.rive:rive-android:9.3.0")

    // Requested Extra Libraries
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.0")
    implementation("androidx.paging:paging-compose:3.3.0")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.20")
    implementation("com.halilibo.compose-richtext:richtext-ui:0.17.0")
    
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    implementation("com.google.android.play:review-ktx:2.0.1")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Android Credential Manager for Google Passwords / Auto-fill
    implementation("androidx.credentials:credentials:1.3.0-beta02")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-beta02")

    // Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.00"))
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")
    
    // Socket.IO for Firebase-free Push Notifications
    implementation("io.socket:socket.io-client:2.1.0")

    // WebRTC for Active Call Screen
    implementation("io.getstream:stream-webrtc-android:1.3.8")

    // Google Play Billing for Diamonds Economy
    implementation("com.android.billingclient:billing-ktx:6.2.0")
}
dependencies { implementation(project(":designsystem")) }
