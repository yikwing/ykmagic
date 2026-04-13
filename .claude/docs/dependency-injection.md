# 依赖注入架构

项目使用 **Koin Annotations** 进行依赖注入，提供声明式 DI 体验。

## Application 初始化

位置: `app/.../MainApplication.kt`

```kotlin
@KoinApplication
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<MainApplication> {
            androidContext(this@MainApplication)
        }
    }
}
```

## 模块结构

项目将 Koin 模块按职责拆分：

### AppModule（顶层聚合）

```kotlin
// AppModule.kt - 顶层聚合，通过 includes 组合子模块
@Module(
    includes = [
        AppFeatureModule::class,
        NetworkModule::class,
    ],
)
@Configuration
object AppModule
```

### AppFeatureModule（业务自动扫描）

```kotlin
// AppFeatureModule.kt - 用 @ComponentScan 自动扫描业务类（ViewModel/Repository）
@Module
@Configuration
@ComponentScan("com.yikwing.ykquickdev")
object AppFeatureModule
```

`@ComponentScan` 自动扫描 `com.yikwing.ykquickdev` 包下所有带 Koin 注解的类（`@KoinViewModel`、`@Single`、`@Factory` 等）。

### AppCoreModule（应用核心基础设施）

```kotlin
// AppCoreModule.kt - 应用级核心依赖
@Module
@Configuration
object AppCoreModule {
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
```

通过 `@ComponentScan` 被自动扫描注册，提供应用级 `CoroutineScope`。

### DataModule（数据层基础设施）

```kotlin
// DataModule.kt - 手动提供数据库、DataStore
@Module
@Configuration
object DataModule {
    @Singleton
    fun provideDataBase(context: Context): UserDatabase =
        Room.databaseBuilder(context.applicationContext, UserDatabase::class.java, "Users.db").build()

    @Singleton
    fun provideUserDao(userDatabase: UserDatabase): UserDao = userDatabase.getUserDao()

    @Singleton
    fun provideChapterDao(userDatabase: UserDatabase): ChapterDao = userDatabase.getChapterDao()

    @Singleton
    fun provideUserPreferencesDataStore(context: Context): DataStore<UserPreferences> =
        context.userPreferencesStore

    @Singleton
    fun provideAppSettingsDataStore(context: Context): DataStore<AppSettings> =
        context.appSettingsStore
}
```

通过 `@ComponentScan` 被自动扫描注册。DataStore 扩展参见 [datastore.md](datastore.md)。

### 网络层 DI

两层配置：

```kotlin
// module_network/NetworkModule.kt - 提供 Json 配置
@Module
@Configuration
@ComponentScan("com.yikwing.network")
object NetworkModule {
    @Singleton
    fun provideJson(): Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
}
```

```kotlin
// app/di/AppNetworkModule.kt - 构建 HttpClient
@Module
@Configuration
object AppNetworkModule {
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
    ): HttpClient = HttpClient {
        install(HttpTimeout) { /* ... */ }
        install(ContentNegotiation) { json(json) }
        install(DefaultRequest) { url(baseUrl); /* ... */ }
        install(HttpRequestRetry) { maxRetries = 3; /* ... */ }
        install(Logging) { level = if (debug) LogLevel.ALL else LogLevel.NONE }
    }
}
```

网络层使用参见 [network.md](network.md)。

### 模块依赖关系

```
AppModule (includes)
├── AppFeatureModule (@ComponentScan 扫描业务类)
└── NetworkModule (Json 基础组件)

自动扫描注册（@ComponentScan "com.yikwing.ykquickdev"）:
├── AppCoreModule (ApplicationScope)
├── DataModule (Room + DataStore)
└── AppNetworkModule (HttpClient + Qualifier)
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

Repository 使用 `@Single`（或 `@Factory`）声明作用域，Koin Annotations 自动完成注入：

```kotlin
// 接口 + 实现分离
interface NetRepository { ... }

@Single
class NetRepositoryImpl(
    private val api: HttpBinApi,
    private val dao: ChapterDao,
) : NetRepository { ... }
```

## 非 Android 组件注入

`object` 单例无法使用构造注入，用 `KoinJavaComponent.inject` 委托：

```kotlin
import org.koin.java.KoinJavaComponent.inject

object UserManager {
    private val json: Json by inject(Json::class.java)
}
```

## Qualifier 支持

区分同类型依赖（`@BaseUrl` 和 `@DebugFlag` 定义在 `module_network/NetworkQualifiers.kt`）：

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

## 核心特性

- 使用 `@Module` + `@Configuration` + `@ComponentScan` 自动扫描依赖
- 使用 `@KoinViewModel` 标记 ViewModel，无需手动注册
- 使用 `@InjectedParam` 传递运行时参数（替代 Hilt 的 AssistedInject）
- 使用 `@Singleton` / `@Factory` 控制依赖作用域
- 完整支持 ViewModel + Repository + Koin 的依赖链
