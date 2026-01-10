import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotzilla) apply false

    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.wire) apply false

    alias(libs.plugins.ksp) apply false
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

// 全局版本配置
val compileSdkVersion = 36
val targetSdkVersion = 36
val minSdkVersion = 26
val javaVersion = JavaVersion.VERSION_17
val jvmTargetVersion = JvmTarget.JVM_17

fun CommonExtension<*, *, *, *, *, *>.configureAndroidCommon() {
    compileSdk = compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
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
                jvmTarget.set(jvmTargetVersion)
                freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
            }
        }
    }
}
