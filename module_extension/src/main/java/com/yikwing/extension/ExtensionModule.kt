package com.yikwing.extension

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * 扩展模块 Koin 配置
 *
 * 收集本库对外提供的容器托管组件（如 [com.yikwing.extension.network.NetConnectManager]）。
 * 扫描范围为本库根包：ExtensionModule 是本库唯一的 Koin 模块，不存在需要区分的内部模块边界。
 *
 * 本模块不自动注册，由使用方在自己的聚合根中显式 `includes`，以保证加载顺序与覆盖语义可控。
 */
@Module
@ComponentScan("com.yikwing.extension")
object ExtensionModule
