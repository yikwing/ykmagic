package com.yikwing.ykquickdev.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * 应用功能模块
 *
 * 提供业务功能相关的依赖：
 * - API：接口封装（通过 @ComponentScan 自动扫描）
 * - Repository：数据仓库（通过 @ComponentScan 自动扫描）
 * - ViewModel：业务视图模型（通过 @ComponentScan 自动扫描）
 *
 * 只扫描上述三层所在的包，而非 app 根包：避免新增的任意注解类被静默吸入本模块，
 * 保持模块边界显式可见。
 */
@Module
@ComponentScan(
    "com.yikwing.ykquickdev.api.apiserver",
    "com.yikwing.ykquickdev.repository",
    "com.yikwing.ykquickdev.viewmodel",
)
object AppFeatureModule
