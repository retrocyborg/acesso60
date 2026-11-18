plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    kotlin("jvm") version "2.2.20" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

group = "br.com.acesso60"
version = "1.1.0"

// Detekt configuration (optional defaults)
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        toolVersion = "1.23.6"
        buildUponDefaultConfig = true
        allRules = false
        ignoreFailures = true
        // jvmTarget will be set via task configuration below
    }
    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    }
    // Ensure Detekt runs with JVM target 17 (compatible with project)
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
        jvmTarget = "17"
    }
}
