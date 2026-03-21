import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.yikwing.ykquickdev.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "ykmagic.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "ykmagic.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kotlinAndroid") {
            id = "ykmagic.kotlin.android"
            implementationClass = "KotlinAndroidConventionPlugin"
        }
        register("androidCompose") {
            id = "ykmagic.android.compose"
            implementationClass = "ComposeConventionPlugin"
        }
    }
}
