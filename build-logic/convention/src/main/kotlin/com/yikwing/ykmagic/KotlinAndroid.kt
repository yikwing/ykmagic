package com.yikwing.ykmagic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * 配置 Kotlin Android 基础选项
 *
 * 功能：
 * - 配置编译 SDK 和最低 SDK
 * - 可选启用 BuildConfig 生成
 * - 配置 Java 17 兼容性
 * - 配置 Kotlin 编译选项
 *
 * @param commonExtension Android 通用扩展
 * @param enableBuildConfig 是否启用 BuildConfig（默认 false）
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    enableBuildConfig: Boolean = false,
) {
    commonExtension.apply {
        compileSdk = ProjectConfig.COMPILE_SDK

        defaultConfig {
            minSdk = ProjectConfig.MIN_SDK
        }

        // 条件启用 BuildConfig
        if (enableBuildConfig) {
            buildFeatures.buildConfig = true
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    configureKotlin()
}

/**
 * 配置 Kotlin 编译选项
 *
 * 功能：
 * - JVM 目标版本设置
 * - 启用实验性 API
 * - 启用 Explicit Backing Fields
 */
private fun Project.configureKotlin() {
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(17)

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)

            // 使用 ProjectConfig 中的编译器参数
            freeCompilerArgs.addAll(ProjectConfig.CompilerOptions.FREE_COMPILER_ARGS)
        }
    }
}
