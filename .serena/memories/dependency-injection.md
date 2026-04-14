# 依赖注入 (Koin Annotations 4.2.1)

## DI 模块
- `app/.../di/AppModule.kt` — 顶层聚合 (includes AppFeatureModule + NetworkModule)
- `app/.../di/AppFeatureModule.kt` — @ComponentScan("com.yikwing.ykquickdev") 自动扫描
- `app/.../di/AppCoreModule.kt` — ApplicationScope (CoroutineScope)
- `app/.../di/AppNetworkModule.kt` — HttpClient + @BaseUrl/@DebugFlag Qualifier
- `app/.../di/DataModule.kt` — Room (UserDatabase) + DataStore (UserPreferences/AppSettings)

## 注解
- `@KoinViewModel` — ViewModel 注入
- `@InjectedParam` — ViewModel 运行时参数
- `@Single` / `@Factory` — 作用域
- `@Singleton` — 单例提供
- `@ComponentScan` — 包扫描
- `@Qualifier` — 类型区分 (@BaseUrl, @DebugFlag, @ApplicationScope)

## 初始化
`MainApplication.kt` 使用 `@KoinApplication` + `startKoin<MainApplication>`