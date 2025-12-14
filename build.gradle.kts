import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotzilla) apply false

    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.wire) apply false

    alias(libs.plugins.ksp) apply false
}

// 强制制定依赖
allprojects {
    configurations.configureEach {
        resolutionStrategy {
            force(libs.activity)
            force(libs.kotlinx.coroutines.core)
        }
    }
}

// 全局 Android 配置
val compileSdkVersion =
    libs.versions.compileSdk
        .get()
        .toInt()
val targetSdkVersion =
    libs.versions.targetSdk
        .get()
        .toInt()
val minSdkVersion =
    libs.versions.minSdk
        .get()
        .toInt()

fun CommonExtension<*, *, *, *, *, *>.configureAndroidCommon() {
    compileSdk = compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

subprojects {
    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            configureAndroidCommon()
            defaultConfig {
                targetSdk = targetSdkVersion
            }
        }
    }

    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            configureAndroidCommon()
        }
    }

    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(17)
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
