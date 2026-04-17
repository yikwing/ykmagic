package com.yikwing.ykmagic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * @param enableBuildConfig 是否启用 BuildConfig（Application 默认 true，Library 默认 false）
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
    enableBuildConfig: Boolean = false,
) {
    commonExtension.apply {
        compileSdk = ProjectConfig.COMPILE_SDK

        buildFeatures.buildConfig = enableBuildConfig
    }

    configureKotlin()
}

private fun Project.configureKotlin() {
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(21)
    }
}
