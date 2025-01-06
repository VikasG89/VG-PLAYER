// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath(libs.oss.licenses.plugin)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
}