import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Пароли ключа подписи лежат в signing.properties (в корне проекта).
val signingProps = Properties().apply {
    val f = rootProject.file("signing.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.avmemori"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.avmemori"
        minSdk = 26
        targetSdk = 35
        // Увеличивайте versionCode при каждом обновлении: иначе Android не поставит APK поверх.
        versionCode = 3
        versionName = "0.3.0"
    }

    // Постоянный ключ подписи: все сборки подписаны одним ключом,
    // поэтому обновления ставятся поверх без удаления приложения.
    signingConfigs {
        create("release") {
            storeFile = rootProject.file(signingProps.getProperty("storeFile", "keystore/av_memori.jks"))
            storePassword = signingProps.getProperty("storePassword")
            keyAlias = signingProps.getProperty("keyAlias", "avmemori")
            keyPassword = signingProps.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
