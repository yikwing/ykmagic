package com.yikwing.ykquickdev.di

import com.yikwing.extension.ExtensionModule
import org.koin.core.annotation.Module

/**
 * 应用聚合根模块
 *
 * 唯一被 [com.yikwing.ykquickdev.MainApplication] 加载的模块，其余模块全部经 `includes` 显式组合。
 * 声明顺序即加载顺序（Koin 为 last-wins），按「基础设施 → 数据 → 业务」排列，
 * 保证业务层可覆盖上游库模块的默认实现。
 */
@Module(
    includes = [
        AppCoreModule::class,
        ExtensionModule::class,
        AppNetworkModule::class,
        DataModule::class,
        AppFeatureModule::class,
    ],
)
object AppModule
