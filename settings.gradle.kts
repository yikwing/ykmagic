pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Version is hardcoded: the settings plugins block is evaluated before the
    // version catalog below exists, so libs.* accessors are unavailable here.
    id("io.github.ben-manes.versions.settings") version "0.61.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "YkQuickDev"
include(":app")
include(":module_config")
include(":module_network")
include(":module_proxy")
include(":module_extension")
include(":module_permission")
include(":module_compose")
