# 依赖注入架构

项目使用 **Koin Annotations** 进行依赖注入，提供声明式 DI 体验。

## Application 初始化

位置: `app/.../MainApplication.kt`

```kotlin
@KoinApplication(modules = [AppModule::class])
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initSetup() // 必须先于 startKoin：provideBaseUrl() 读取 YkConfigManager.config
        startKoin<MainApplication> {
            androidContext(this@MainApplication)
        }
    }
}
```

`AppModule` 是唯一入口，其余模块全部经它的 `includes` 显式组合。

## 模块结构

模块均位于 `app/.../di/`（`ExtensionModule` 除外），按职责拆分：

### AppModule（聚合根）

```kotlin
// 声明顺序即加载顺序（Koin 为 last-wins）：基础设施 → 数据 → 业务
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
```

新增模块必须加入这里的 `includes`，否则不会被加载。

### AppCoreModule（应用核心基础设施）

```kotlin
@Module
object AppCoreModule {
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
```

### ExtensionModule（module_extension 库组件）

```kotlin
// module_extension/ExtensionModule.kt - 不自动注册，由使用方在聚合根中显式 includes
@Module
@ComponentScan("com.yikwing.extension")
object ExtensionModule
```

提供 `NetConnectManager` 等库级组件。

### AppNetworkModule（网络层）

```kotlin
@Module
object AppNetworkModule {
    @Singleton
    fun provideJson(): Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = YkConfigManager.config.baseUrl

    @Singleton
    @DebugFlag
    fun provideDebug(): Boolean = BuildConfig.DEBUG

    @Singleton
    fun provideHttpClient(
        json: Json,
        @BaseUrl baseUrl: String,
        @DebugFlag debug: Boolean,
    ): HttpClient = HttpClient(CIO) {
        install(HttpTimeout) { /* ... */ }
        install(ContentNegotiation) { json(json) }
        install(DefaultRequest) { url(baseUrl); /* ... */ }
        install(CommonHeaders) { headers = { mapOf("version" to BuildConfig.VERSION_NAME, /* ... */) } }
        install(HttpRequestRetry) { retryGetOnly() } // 仅 GET 重试；单接口可用 retry { } 整体覆盖
        install(Logging) { level = if (debug) LogLevel.ALL else LogLevel.NONE }
    }
}
```

`module_network` 只提供 RequestState/ApiException/ApiTransform/CommonHeaders 等基础组件，不依赖 Koin。网络层使用参见 [network.md](network.md)。

### DataModule（数据层基础设施）

```kotlin
@Module
object DataModule {
    @Singleton
    fun provideDataBase(context: Context): UserDatabase =
        Room.databaseBuilder(context.applicationContext, UserDatabase::class.java, "Users.db").build()

    @Singleton
    fun provideUserDao(userDatabase: UserDatabase): UserDao = userDatabase.getUserDao()

    @Singleton
    fun provideChapterDao(userDatabase: UserDatabase): ChapterDao = userDatabase.getChapterDao()

    @Singleton
    @UserPreferencesStore
    fun provideUserPreferencesDataStore(context: Context): DataStore<UserPreferences> =
        context.userPreferencesStore

    @Singleton
    @AppSettingsStore
    fun provideAppSettingsDataStore(context: Context): DataStore<AppSettings> =
        context.appSettingsStore
}
```

DataStore 必须带 Qualifier，原因见「多实例同类型注入」。DataStore 扩展参见 [datastore.md](datastore.md)。

### AppFeatureModule（业务自动扫描）

```kotlin
@Module
@ComponentScan(
    "com.yikwing.ykquickdev.api.apiserver",
    "com.yikwing.ykquickdev.manager",
    "com.yikwing.ykquickdev.repository",
    "com.yikwing.ykquickdev.viewmodel",
)
object AppFeatureModule
```

`@ComponentScan` 只扫描上述包内带 Koin 注解的类（`@KoinViewModel`、`@Singleton`、`@Factory` 等），而非 app 根包：避免任意注解类被静默吸入。在新包中添加注解类时，需把包名加入扫描列表。

### 模块依赖关系

```
AppModule (@KoinApplication 唯一入口, includes 按序组合)
├── AppCoreModule      (ApplicationScope)
├── ExtensionModule    (@ComponentScan com.yikwing.extension → NetConnectManager)
├── AppNetworkModule   (Json + BaseUrl/DebugFlag + HttpClient)
├── DataModule         (Room + DataStore)
└── AppFeatureModule   (@ComponentScan api.apiserver / manager / repository / viewmodel)
```

---

## ViewModel 注入

```kotlin
// 定义 ViewModel
@KoinViewModel
class MyViewModel(
    private val repository: Repository,
    @InjectedParam val name: String  // 运行时参数
) : ViewModel()

// Composable 中使用
@Composable
fun MyScreen(vm: MyViewModel = koinViewModel()) {
    // ...
}

// Composable 中使用（带参数）
@Composable
fun DetailScreen(name: String) {
    val vm: MyViewModel = koinViewModel { parametersOf(name) }
}
```

## 构造器注入

Repository 使用 `@Singleton`（或 `@Factory`）声明作用域，Koin Annotations 自动完成注入：

```kotlin
// 接口 + 实现分离
interface NetRepository { ... }

@Singleton
class NetRepositoryImpl(
    private val wanAndroidApi: WanAndroidApi,
    private val chapterDao: ChapterDao,
) : NetRepository { ... }
```

## 非 Android 组件注入

不要用 `object` + `KoinJavaComponent.inject`（Service Locator 只用于 UI 边界）；改为由容器托管的 class，构造注入依赖，所在包需在 `AppFeatureModule` 的 `@ComponentScan` 范围内：

```kotlin
@Singleton
class UserManager(
    private val json: Json,
)
```

## Qualifier 支持

区分同类型依赖（全部 Qualifier 定义在 `app/.../di/Qualifiers.kt`）：

```kotlin
// 定义 Qualifier
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugFlag

// 使用：提供方和消费方都加注解
@Singleton
@BaseUrl
fun provideBaseUrl(): String = YkConfigManager.config.baseUrl

@Singleton
fun provideHttpClient(json: Json, @BaseUrl baseUrl: String, @DebugFlag debug: Boolean): HttpClient = ...
```

## Koin 编译时检查

Koin 编译时依赖检查通过 `ykmagic.android.koin` Convention Plugin 自动配置（`AndroidKoinConventionPlugin`），无需手动设置。

## 多实例同类型注入（必读）

Koin 依据 `KClass + Qualifier` 索引 bean。JVM 泛型擦除 + R8 默认剥离 `Signature`，导致 `DataStore<UserPreferences>` 与 `DataStore<AppSettings>` 在 release 塌缩为同一 key，注入错乱。

解法：定义自定义 `@Qualifier` 注解（如 `@UserPreferencesStore`），provider 与注入点同时标注。优于 `@Named("xxx")`，字符串易拼错且无编译期校验。
