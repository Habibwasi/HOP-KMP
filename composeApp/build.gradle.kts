import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.sentryAndroid)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.androidx.security.crypto)
            implementation(libs.material.icons.extended)
            // Firebase (Android only — no KMP artifact)
            implementation(libs.firebase.messaging)
            implementation(libs.sentry.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.material.icons.extended)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.hop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.hop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SENTRY_DSN",
            "\"${project.findProperty("SENTRY_DSN") ?: ""}\"")
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("boolean", "DEV_MODE", "true")
        }
        getByName("release") {
            buildConfigField("boolean", "DEV_MODE", "false")
            isMinifyEnabled = true
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
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

sentry {
    org = "habib-ahmed-wasi"
    projectName = "android"

    // Automatically upload ProGuard/R8 mapping file on release builds so that
    // Sentry crash reports show de-obfuscated class and method names.
    autoUploadProguardMapping = true

    // Include source context lines around each stack frame in Sentry events.
    includeSourceContext = true
}

// Fix implicit dependency between Sentry's GenerateBundleIdTask and Compose
// Multiplatform resource generator tasks. Without this, Gradle's task ordering
// is non-deterministic and the build fails with a configuration error.
afterEvaluate {
    tasks.matching { it.name.startsWith("generateSentryBundleId") }.configureEach {
        dependsOn(
            tasks.matching { it.name == "generateResourceAccessorsForAndroidMain" },
            tasks.matching { it.name == "generateActualResourceCollectorsForAndroidMain" }
        )
    }
}

