package com.yikwing.ykmagic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 配置 Android 通用依赖
 */
internal fun Project.configureAndroidDependencies() {
    dependencies {
        // AndroidX 核心库
        add("implementation", libs.findLibrary("core-ktx").get())
        add("implementation", libs.findLibrary("appcompat").get())

        // 协程
        add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())

        // 测试
        add("testImplementation", libs.findBundle("testBundle").get())
        add("androidTestImplementation", libs.findBundle("androidTestBundle").get())
    }
}

/**
 * 配置 Compose 依赖（已废弃，使用 configureAndroidCompose）
 */
@Deprecated("使用 configureAndroidCompose 替代")
internal fun Project.configureComposeDependencies() {
    dependencies {
        val composeBom = libs.findLibrary("compose-bom").get()
        add("implementation", platform(composeBom))
        add("implementation", libs.findLibrary("compose-material3").get())
        add("implementation", libs.findLibrary("ui-tooling-preview").get())

        add("androidTestImplementation", platform(composeBom))
        add("androidTestImplementation", libs.findLibrary("ui-test-junit4").get())
        add("debugImplementation", libs.findLibrary("ui-tooling").get())
        add("debugImplementation", libs.findLibrary("ui-test-manifest").get())
    }
}