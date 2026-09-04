import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // AGP 9 brings Kotlin support in-box; the separate kotlin-android plugin
    // is gone, and the Compose compiler comes with buildFeatures.compose.
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.drawably.compose"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        // The engine is pure Kotlin and must stay callable from anywhere.
        freeCompilerArgs.add("-Xexplicit-api=strict")
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    api(libs.compose.foundation)
    api(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    // only to read the golden fixtures; never reaches the published artifact
    testImplementation(libs.gson)
}
