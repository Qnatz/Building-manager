// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version by extra("1.8.20") // Define Kotlin version using `val by extra`
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0") // Existing Android Gradle Plugin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version") // Kotlin Gradle Plugin
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    // It's common to also apply the Kotlin plugin at the project level, apply false
    // id("org.jetbrains.kotlin.android") version "1.8.20" apply false // Ensure version matches
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
