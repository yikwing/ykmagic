package com.yikwing.ykmagic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Project 扩展属性，简化版本目录访问
 *
 * 使用方式：
 * ```kotlin
 * val version = libs.findVersion("someVersion").get()
 * val library = libs.findLibrary("someLibrary").get()
 * ```
 */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
