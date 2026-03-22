package com.yikwing.ykmagic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 配置 Android Compose 相关设置
 *
 * 功能：
 * - 启用 Compose 构建功能
 * - 添加 Compose BOM 和核心依赖
 * - 配置调试和测试工具
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.buildFeatures.compose = true

    dependencies {
        val composeBom = libs.findLibrary("compose-bom").get()
        add("implementation", platform(composeBom))
        add("implementation", libs.findLibrary("compose-material3").get())
        add("implementation", libs.findLibrary("ui-tooling-preview").get())

        // 调试工具
        add("debugImplementation", libs.findLibrary("ui-tooling").get())
        add("debugImplementation", libs.findLibrary("ui-test-manifest").get())

        // 测试依赖
        add("androidTestImplementation", platform(composeBom))
        add("androidTestImplementation", libs.findLibrary("ui-test-junit4").get())
    }
}