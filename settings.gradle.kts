pluginManagement {
    plugins {
        id("com.android.application") version "9.1.1"
        id("org.jetbrains.kotlin.android") version "2.2.20"
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "acesso60"
include(":core", ":app")
project(":core").projectDir = file("core")
project(":app").projectDir = file("Acesso60/app")
