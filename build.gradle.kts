// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.arthenica.com")
    }
    dependencies {
        classpath(libs.oss.licenses.plugin)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
}