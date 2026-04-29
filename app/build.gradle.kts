import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.rve.systemmonitor"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rve.systemmonitor"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        ndkVersion = "30.0.14904198"
        buildToolsVersion = "37.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    val signingPropertiesFile = rootProject.file("signing.properties")
    val signingProperties = Properties()
    if (signingPropertiesFile.exists()) {
        signingPropertiesFile.inputStream().use { signingProperties.load(it) }
    }

    signingConfigs {
        create("release") {
            if (signingProperties.isEmpty) {
                val debugConfig = getByName("debug")
                storeFile = debugConfig.storeFile
                storePassword = debugConfig.storePassword
                keyAlias = debugConfig.keyAlias
                keyPassword = debugConfig.keyPassword
            } else {
                storeFile = file(signingProperties.getProperty("KEYSTORE_PATH"))
                storePassword = signingProperties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = signingProperties.getProperty("KEY_ALIAS")
                keyPassword = signingProperties.getProperty("KEY_PASSWORD")
            }
        }
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        create("profile") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.icons.material.symbols.rounded.android)
    implementation(libs.icons.material.symbols.rounded.filled.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.backdrop)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register<Exec>("buildRustLibraries") {
    group = "rust"
    description = "Compiles Rust code using cargo-ndk and outputs the .so files to jniLibs"
    workingDir = file("../rust")

    commandLine(
        "cargo", "ndk",
        "-t", "armeabi-v7a",
        "-t", "arm64-v8a",
        "-o", "../app/src/main/jniLibs",
        "build", "--release",
    )
}

tasks.named("preBuild") {
    dependsOn("buildRustLibraries")
}
