package com.yikwing.ykmagic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 配置 Android 通用依赖
 */
internal fun Project.configureAndroidDependencies() {
    dependencies {
        // AndroidX 核心库
        add("implementation", libs.findLibrary("androidx-core-ktx").get())
        add("implementation", libs.findLibrary("androidx-appcompat").get())

        // Lifecycle
        add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())

        // 协程
        add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())

        // 测试
        add("testImplementation", libs.findBundle("testBundle").get())
        add("androidTestImplementation", libs.findBundle("androidTestBundle").get())
    }
}
