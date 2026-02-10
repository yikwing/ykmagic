import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotzilla) apply false

    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.wire) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.koin.compiler) apply false
}

// 强制指定依赖版本
allprojects {
    configurations.configureEach {
        resolutionStrategy {
            force(libs.activity)
            force(libs.kotlinx.coroutines.core)
        }
    }
}

val compileSdkVersion = 36
val targetSdkVersion = 36
val minSdkVersion = 26
val javaVersion = JavaVersion.VERSION_17
val jvmTargetVersion = JvmTarget.JVM_17

/**
 * 为 Application 模块配置通用 Android 属性
 */
fun ApplicationExtension.configureAndroidCommon() {
    compileSdk = compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

/**
 * 为 Library 模块配置通用 Android 属性
 */
fun LibraryExtension.configureAndroidCommon() {
    compileSdk = compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

// ============================================================================
// 子项目配置
// ============================================================================

subprojects {
    // Android Application 配置
    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            configureAndroidCommon()
            defaultConfig {
                targetSdk = targetSdkVersion
            }
        }
    }

    // Android Library 配置
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            configureAndroidCommon()
        }
    }

    // Kotlin 配置
    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(17)
            compilerOptions {
                jvmTarget.set(jvmTargetVersion)
                // 启用 Kotlin 2.3.0 的 Explicit Backing Fields 特性
                // 用于简化 ViewModel 中 StateFlow 的声明
                freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
            }
        }
    }
}
