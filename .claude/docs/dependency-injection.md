# 依赖注入架构

项目使用 **Koin Annotations** 进行依赖注入,提供声明式 DI 体验。

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

## 依赖模块定义

使用 Koin Annotations 定义模块:

```kotlin
@Module
@Configuration
@ComponentScan("com.yikwing.ykquickdev")
object AppModule

@Module
object DataModule {
    @Singleton
    fun provideDataBase(context: Context): UserDatabase = ...

    @Factory
    fun provideUserDao(userDatabase: UserDatabase): UserDao = ...
}
```

## ViewModel 注入

```kotlin
// 定义 ViewModel
@KoinViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    @InjectedParam val name: String  // 运行时参数
) : ViewModel()

// 在 Activity/Fragment 中使用
class MainActivity : BaseActivity() {
    private val vm: DataStoreViewModel by viewModel()
}
```

## 构造器注入

```kotlin
class Repository @Inject constructor(
    private val repo: Repo
)
```

## 非 Android 组件注入

如 Object 单例:

```kotlin
object UserManager {
    private val json: Json by inject(Json::class.java)
}
```

## Qualifier 支持

区分同类型依赖（项目定义了 `@BaseUrl` 和 `@DebugFlag`，位于 `module_network/NetworkQualifiers.kt`）:

```kotlin
@Singleton
@BaseUrl
fun provideBaseUrl(): String = YkConfigManager.config.baseUrl

@Singleton
@DebugFlag
fun provideDebug(): Boolean = BuildConfig.DEBUG

// 注入时使用同名注解
@Singleton
fun provideHttpClient(json: Json, @BaseUrl baseUrl: String, @DebugFlag debug: Boolean): HttpClient = ...
```

## KSP 配置

位置: app/build.gradle.kts

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("KOIN_CONFIG_CHECK", "true")  // 启用编译时依赖检查
}
```

## 核心特性

- 使用 `@Module` + `@Configuration` + `@ComponentScan` 自动扫描依赖
- 使用 `@KoinViewModel` 标记 ViewModel,无需手动注册
- 使用 `@InjectedParam` 传递运行时参数 (替代 Hilt 的 AssistedInject)
- 使用标准 `@Inject` 注解 (javax.inject),保持代码可移植性
- 使用 `@Singleton` / `@Factory` 控制依赖作用域
- 完整支持 ViewModel + Repository + Koin 的依赖链