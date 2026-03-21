# 依赖注入 (Koin)

## DI 文件位置
- `app/.../di/AppModule.kt` - 总模块（聚合 AppFeatureModule + NetworkModule）
- `app/.../di/AppCoreModule.kt` - 核心模块
- `app/.../di/AppFeatureModule.kt` - 功能模块
- `app/.../di/AppNetworkModule.kt` - 网络配置模块
- `app/.../di/DataModule.kt` - 数据层模块

## 注解
- `@KoinViewModel` - ViewModel 注入
- `@InjectedParam` - ViewModel 参数注入
- `@Single` / `@Factory` - 作用域
- `@ComponentScan` - 包扫描

## Koin 版本
- koin-bom: 4.2.0