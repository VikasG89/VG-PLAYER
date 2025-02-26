import org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS

pluginManagement {
    repositories {
        // Prioritize the Gradle Plugin Portal for plugin resolution.
        gradlePluginPortal()

        // Configure Google's Maven repository for Android-related plugins.
        google {
            content {
                // Use more specific includes for better control and clarity.
                includeGroupByRegex("com\\.android\\..*") // Matches com.android.tools, com.android.support, etc.
                includeGroupByRegex("com\\.google\\..*")  // Matches com.google.gms, com.google.firebase, etc.
                includeGroupByRegex("androidx\\..*")      // Matches androidx.appcompat, androidx.core, etc.
            }
        }
        // Use mavenCentral as a fallback for other plugins.
        mavenCentral()
    }
    // Prevent plugins from being resolved from project-level repositories.
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == null) {
                // If the plugin doesn't have a namespace, it's likely a legacy plugin.
                // Consider logging a warning or failing the build if you want to enforce namespaced plugins.
                // println("Warning: Legacy plugin without namespace: ${requested.id.id}")
            }
        }
    }
}

dependencyResolutionManagement {
    // Enforce that dependencies are only resolved from the repositories defined here.
    repositoriesMode.set(FAIL_ON_PROJECT_REPOS)
    repositories {
        // Prioritize Google's Maven repository for Android-related dependencies.
        google()
        // Use mavenCentral as a fallback for other dependencies.
        mavenCentral()
        // Consider if these are necessary, and if so, if they should be restricted to specific dependencies.
        maven("https://jitpack.io") {
            content {
                // Example: Only allow dependencies from specific groups.
                // includeGroup("com.github.user")
            }
        }
        maven("https://maven.arthenica.com") {
            content {
                // Example: Only allow dependencies from specific groups.
                // includeGroup("com.arthenica")
            }
        }
    }
}

rootProject.name = "VG Player"
include(":app")