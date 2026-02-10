package com.yikwing.ykquickdev.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/**
 * 应用功能模块
 *
 * 提供业务功能相关的依赖：
 * - ViewModel：业务视图模型（通过 @ComponentScan 自动扫描）
 * - Repository：数据仓库（通过 @ComponentScan 自动扫描）
 * - UseCase：业务用例（如需要）
 *
 * 使用 @ComponentScan 自动扫描 com.yikwing.ykquickdev 包下的所有 Koin 注解类
 */
@Module
@Configuration
@ComponentScan("com.yikwing.ykquickdev")
object AppFeatureModule
