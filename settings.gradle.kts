pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
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
include(":module_compress")
