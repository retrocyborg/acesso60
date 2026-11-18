import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("jacoco")
    kotlin("plugin.serialization") version "2.2.20"
}

android {
    namespace = "com.acesso60"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.acesso60"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Test libraries
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    // Existing JUnit 4 tests (if any)

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

// Lint task to detect escaped lambda arrows
tasks.register("checkEscapedArrows") {
    group = "verification"
    description = "Fails if any escaped lambda arrows (\u002d\\u003e) are present in Kotlin files."
    doLast {
        val escaped = "-\\u003e"
        val ktFiles = fileTree(".") {
            include("**/*.kt")
        }.files
        val offending = ktFiles.filter { it.readText().contains(escaped) }
        if (offending.isNotEmpty()) {
            throw GradleException("Escaped lambda arrows found in:\n${offending.joinToString("\n")}")
        } else {
            println("No escaped lambda arrows detected.")
        }
    }
    }
// Ensure the check task runs the lint guard
tasks.named("check") { dependsOn("checkEscapedArrows") }
