// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    //id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    kotlin("kapt") version "1.9.0"
    id("androidx.room") version "2.6.1" apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}