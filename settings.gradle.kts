pluginManagement {
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
include(":module_logger")
include(":module_datastore")
