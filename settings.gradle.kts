pluginManagement {
    repositories {
        maven {
            name = "NeoForge"
            url = uri("https://maven.neoforged.net/releases")
        }

        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val parent = rootDir.parentFile.name
val root = rootDir.name

rootProject.name = "${parent}+${root}"