import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.sentryAndroid)
    alias(libs.plugins.roborazzi)
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
            implementation(libs.koin.android)
            implementation(libs.androidx.security.crypto)
            implementation(libs.material.icons.extended)
            // Firebase (Android only — no KMP artifact)
            implementation(libs.firebase.messaging)
            implementation(libs.sentry.android)
            // Google Places (Android only — still used for nothing now; kept for future)
            implementation(libs.google.places)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            // Google Maps Compose — composeApp only targets Android so safe in commonMain
            implementation(libs.maps.compose)
            implementation(libs.play.services.maps)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.material.icons.extended)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.previewScannerSupport)
            implementation(libs.composable.preview.scanner)
            implementation(libs.androidx.compose.uiTest.junit4)
        }
    }
}

val localProps = Properties().also { props: Properties ->
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { stream -> props.load(stream) }
}

android {
    namespace = "com.example.hop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    signingConfigs {
        create("release") {
            storeFile = localProps.getProperty("KEYSTORE_PATH")?.let { file(it) }
            storePassword = localProps.getProperty("KEYSTORE_PASSWORD") ?: ""
            keyAlias = localProps.getProperty("KEY_ALIAS") ?: ""
            keyPassword = localProps.getProperty("KEY_PASSWORD") ?: ""
        }
    }

    defaultConfig {
        applicationId = "com.ridly.hop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SENTRY_DSN",
            "\"${project.findProperty("SENTRY_DSN") ?: ""}\"")
        buildConfigField("String", "SUPABASE_URL",
            "\"${project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",
            "\"${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
        buildConfigField("String", "MAPS_API_KEY",
            "\"${project.findProperty("MAPS_API_KEY") ?: ""}\"")
        manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
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
            buildConfigField("boolean", "DEV_MODE", "false")
        }
        getByName("release") {
            buildConfigField("boolean", "DEV_MODE", "false")
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware" }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    debugImplementation(libs.androidx.compose.uiTest.manifest)
}

// Screenshots of every @Preview, rendered on the JVM with Robolectric.
//   ./gradlew :composeApp:recordRoborazziDebug   → composeApp/screenshots/*.png
roborazzi {
    outputDir.set(file("screenshots"))
    generateComposePreviewRobolectricTests {
        enable = true
        packages = listOf("com.example.hop.ui")
        includePrivatePreviews = true
        robolectricConfig = mapOf(
            "sdk" to "[35]",
            "qualifiers" to "RobolectricDeviceQualifiers.Pixel5",
            // Skip the real Application (it starts Koin, Sentry, Firebase) — previews need none of it.
            "application" to "android.app.Application::class",
        )
    }
}

sentry {
    org = "habib-ahmed-wasi"
    projectName = "android"

    // Automatically upload ProGuard/R8 mapping file on release builds so that
    // Sentry crash reports show de-obfuscated class and method names.
    autoUploadProguardMapping = false

    // Include source context lines around each stack frame in Sentry events.
    includeSourceContext = true
}

// Fix implicit dependency between Sentry's tasks (GenerateBundleIdTask and
// CollectSourcesTask) and the Compose Multiplatform resource generator tasks.
// Without this, Gradle's task ordering is non-deterministic and the build fails
// with a configuration-cache validation error on the generated resource dirs.
afterEvaluate {
    val composeResourceTasks = listOf(
        "generateResourceAccessorsForAndroidMain",
        "generateActualResourceCollectorsForAndroidMain"
    )
    tasks.matching {
        it.name.startsWith("generateSentryBundleId") ||
            it.name.startsWith("sentryCollectSources")
    }.configureEach {
        dependsOn(tasks.matching { it.name in composeResourceTasks })
    }
}

