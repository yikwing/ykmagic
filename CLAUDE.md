# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

YkQuickDev 是一个 Android 快速开发框架库,提供了多个可独立使用的功能模块,用于加速 Android 应用开发。

**技术栈**:
- Kotlin 2.3.0 + Coroutines 1.10.2
- Gradle 9.3.0 + AGP 8.13.2 + 版本目录 (libs.versions.toml) 统一管理依赖
- JDK 17, compileSdk 36, minSdk 26
- 依赖注入: Koin 4.1.1 + Koin Annotations 2.3.1 (KSP 2.3.4)
- 网络层: Ktor 3.3.3 + OkHttp 5.3.2
- UI: Jetpack Compose (BOM 2026.01.00) + Material3
- 序列化: kotlinx.serialization 1.9.0
- 测试: JUnit 4.13.2 + Hamcrest 3.0 + MockK 1.14.7
- 发布: JitPack (com.github.yikwing.ykmagic:模块名:版本号)

## 详细文档索引

按需查阅以下专题文档:

| 文档 | 内容 | 适用场景 |
|------|------|---------|
| [network.md](.claude/docs/network.md) | Ktor 网络请求、suspend vs Flow、依赖注入 | 编写 API、处理响应 |
| [dependency-injection.md](.claude/docs/dependency-injection.md) | Koin 依赖注入、ViewModel 注入 | 添加依赖、配置 DI |
| [datastore.md](.claude/docs/datastore.md) | Proto DataStore、Flow 操作 | 存储用户偏好 |
| [modules.md](.claude/docs/modules.md) | 模块化设计、AppInitializer、CacheManager | 了解项目结构 |
| [build-publish.md](.claude/docs/build-publish.md) | 构建命令、环境配置、模块发布 | 构建 APK、发布 |
| [patterns.md](.claude/docs/patterns.md) | Event Wrapper、Flow 生命周期、Compose 技巧 | 开发模式参考 |
| [testing.md](.claude/docs/testing.md) | Hamcrest 匹配器、测试命令 | 编写和运行测试 |
| [android-studio-tips.md](.claude/docs/android-studio-tips.md) | 字体连字、IDE 配置 | IDE 优化 |

## 快速参考

### 常用构建命令
```bash
# 开发构建
./android_build.sh dev         # Debug 构建
./android_build.sh build       # Release 构建
./android_build.sh all         # 清理+构建+安装
./android_build.sh clean       # 清理构建产物
./android_build.sh install     # 安装 Release APK

# 依赖管理
./android_build.sh dependency  # 检查依赖更新
./gradlew dependencyUpdates    # 同上

# 测试
./gradlew test                 # 运行所有单元测试
./gradlew :module_config:test  # 运行指定模块测试
./gradlew test --tests "com.yikwing.config.ReturnsTest"  # 运行单个测试类
./gradlew connectedDebugAndroidTest  # 运行 Instrumented 测试

# 代码质量
./gradlew lint                 # 运行 Lint 检查
./gradlew lintFix              # 自动修复 Lint 问题
./gradlew signingReport        # 查看签名信息
```

### 模块概览
| 模块 | 功能 | 关键文件 |
|------|------|---------|
| module_config | 配置管理 (kotlinx.serialization) | YkConfigManager.kt |
| module_network | Ktor Client 网络请求 | NetworkModule.kt, RequestExtensions.kt |
| module_extension | 扩展函数、CacheManager、NetConnectManager | CacheManager.kt, AssetsExtensions.kt |
| module_datastore | Proto DataStore 封装 | DataStoreModule.kt |
| module_permission | 权限请求 | PermissionFragment.kt |
| module_logger | 日志组件 | LoggerInitTask.kt |
| module_proxy | BaseActivity、AppInitializer | AppInitializer.kt, ActivityHierarchyManager.kt |
| module_component | UI 组件 (RoundedImageView, ImBarWrapperView) | RoundedImageView.kt |

### 必需配置文件
- `android_env.json` - 应用配置 (base_url 等)
- `keystore.properties` - 签名配置

### KSP 注解
- `@Serializable` / `@SerialName` - JSON 序列化
- `@KoinViewModel` / `@Inject` - 依赖注入
- `@Entity` / `@Dao` / `@Database` - Room 数据库

### Debug 工具 (仅 Debug 版本)
- Chucker - 网络抓包
- LeakCanary - 内存泄漏检测
- Glance - 性能监控

## 核心架构设计

### 配置管理 (YkConfigManager)
**流程**: `android_env.json` → BuildConfig.YK_CONFIG (构建时) → YkConfigManager (运行时)

**位置**: `module_config/src/main/java/com/yikwing/config/YkConfigManager.kt`

使用方式:
```kotlin
// 定义配置类
@Serializable
data class AppConfig(@SerialName("base_url") val baseUrl: String)

// 初始化 (Application.onCreate)
YkConfigManager.setUp(BuildConfig.YK_CONFIG)

// 获取配置
val baseUrl = YkConfigManager.config.baseUrl
```

### 模块初始化机制 (AppInitializer)
基于拓扑排序的初始化框架，支持依赖声明和循环检测。

**位置**: `module_proxy/src/main/java/com/yikwing/proxy/startup/AppInitializer.kt`

**核心特性**:
- 自动根据依赖关系排序执行（Kahn's 拓扑排序算法）
- 循环依赖检测（会抛出 "存在回环依赖" 错误）
- 支持 debug 模式输出初始化日志

使用方式:
```kotlin
// Application.onCreate()
AppInitializer.getInstance(this)
    .addTask(ConfigInjectInitTask())
    .addTask(LoggerInitTask())
    .addTask(NetworkInitTask())
    .addTask(DataStoreInitTask())
    .build(debug = true)

// 创建初始化任务
class ConfigInjectInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        // 初始化代码
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(
        LoggerInitTask::class.java  // 声明依赖
    )
}
```

### 网络层设计原则
**技术栈**: Ktor Client + OkHttp Engine (已从 Retrofit 迁移)

**位置**: `module_network/src/main/java/com/yikwing/network/`

**设计原则**: "动词用 suspend，名词用 Flow"
- UI 交互 → `requestStateFlow()` 返回 `Flow<RequestState<T>>`
- 后台操作 → `requestResult()` 返回 `Result<T?>`

**拦截器架构**:
- HeaderInterceptor: 自定义请求头
- RetryInterceptor: 重试逻辑
- LogInterceptor: 日志记录
- Chucker: Debug 网络抓包 (仅 Debug 版本)

**使用示例**:
```kotlin
// Repository 层
class Repo @Inject constructor(private val httpClient: HttpClient) {
    suspend fun getChapters(): BaseHttpResult<List<ChapterBean>> =
        httpClient.get { url("wxarticle/chapters/json") }.body()
}

// ViewModel 层 - UI 交互
class ChapterViewModel(private val repo: Repo) : ViewModel() {
    val state: StateFlow<RequestState<List<ChapterBean>?>>
        field = MutableStateFlow(RequestState.Loading)

    fun fetch() = viewModelScope.launch {
        requestStateFlow { repo.getChapters() }.collect { state.value = it }
    }
}

// ViewModel 层 - 后台操作
fun uploadLog() = viewModelScope.launch {
    requestResult { repo.uploadLog() }
        .onSuccess { Log.d("Upload", "成功") }
        .onFailure { Log.e("Upload", "失败: ${it.message}") }
}
```

### Activity 生命周期管理 (ActivityHierarchyManager)
全局 Activity 栈管理器，线程安全 + 内存安全 (WeakReference)

**位置**: `module_proxy/src/main/java/com/yikwing/proxy/util/ActivityHierarchyManager.kt`

**核心特性**:
- 线程安全: 使用 `CopyOnWriteArrayList` 保证并发安全
- 内存安全: 使用 `WeakReference` 避免内存泄漏
- 自动清理: 自动过滤已销毁的 Activity 引用

**核心 API**:
- `getTopActivity()` - 获取栈顶 Activity
- `finishTopActivities(count)` - 关闭栈顶 N 个
- `finishAllExcept(activityClass)` - 关闭除指定外的所有
- `finishUntil(activityClass, inclusive)` - 关闭到指定 Activity

**使用示例**:
```kotlin
// 在 Application 中注册
class AppActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks by noOpDelegate() {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ActivityHierarchyManager.register(activity)
    }
    override fun onActivityDestroyed(activity: Activity) {
        ActivityHierarchyManager.unregister(activity)
    }
}

// 关闭栈顶 2 个 Activity
ActivityHierarchyManager.finishTopActivities(2)

// 关闭除了 MainActivity 外的所有 Activity
ActivityHierarchyManager.finishAllExcept(MainActivity::class.java)
```

### 内存缓存 (CacheManager)
LRU 淘汰 (默认 256 条) + TTL 过期机制

**位置**: `module_extension/src/main/java/com/yikwing/extension/util/CacheManager.kt`

**核心特性**:
- LRU 淘汰策略（默认 256 条上限）
- TTL 过期机制（基于单调时钟）
- 线程安全（synchronized 保护）

**使用示例**:
```kotlin
// 写入（永不过期）
CacheManager.put("user", userObj)

// 写入（5 分钟过期）
CacheManager.put("token", "abc123", ttlMillis = 5 * 60 * 1000L)

// 读取
val user: User? = CacheManager.get("user")

// 获取或计算
val config = CacheManager.getOrPut("config", ttlMillis = 60_000L) {
    loadConfigFromDisk()
}

// 清除
CacheManager.remove("token")
CacheManager.clear()
```

### Koin 依赖注入
**位置**: 各模块的 `di/` 目录

**核心特性**:
- 使用 Koin Annotations + KSP 自动生成代码
- Kotzilla SDK 1.4.2 监控 (15 秒刷新率)
- Application 需添加 `@KoinApplication` 注解
- ViewModel 使用 `@KoinViewModel` 注解
- 支持 `@InjectedParam` 传递运行时参数

**使用示例**:
```kotlin
// Application 初始化
@KoinApplication
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
        }
    }
}

// 定义模块
@Module
object NetworkModule {
    @Singleton
    fun provideHttpClient(okHttpClient: OkHttpClient): HttpClient = ...
}

// ViewModel 注入
@KoinViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    @InjectedParam val userId: String  // 运行时参数
) : ViewModel()

// 在 Activity/Fragment 中使用
class MainActivity : BaseActivity() {
    private val vm: MyViewModel by viewModel { parametersOf("user123") }
}
```

### ViewModel 状态声明 (Explicit Backing Fields)
Kotlin 2.3.0+ 支持 explicit backing fields，简化 ViewModel 中 StateFlow 的声明。

**编译器配置**: 根 `build.gradle.kts` 中启用 `-XXLanguage:+ExplicitBackingFields`

**使用示例**:
```kotlin
@KoinViewModel
class MyViewModel : ViewModel() {
    // 传统写法需要两个属性
    // private val _state = MutableStateFlow(UiState())
    // val state: StateFlow<UiState> = _state

    // 使用 explicit backing fields 简化
    val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    fun updateState() {
        uiState.update { it.copy(loading = true) }
    }
}

// Compose UI 中使用
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    // 使用 state
}
```

### InitState 泛型状态类
用于表示"未初始化"和"已初始化"两种状态，适合延迟加载场景。

**位置**: `module_extension/src/main/java/com/yikwing/extension/util/InitState.kt`

**使用示例**:
```kotlin
sealed class InitState<out T> {
    data object Uninitialized : InitState<Nothing>()
    data class Value<out T>(val data: T) : InitState<T>()

    val isInitialized: Boolean get() = this is Value
    fun getOrNull(): T? = (this as? Value)?.data
}

// ViewModel 中使用
@KoinViewModel
class ConfigViewModel : ViewModel() {
    val configState: StateFlow<InitState<Config>>
        field = MutableStateFlow(InitState.Uninitialized)

    fun loadConfig() {
        viewModelScope.launch {
            val config = repository.getConfig()
            configState.value = InitState.Value(config)
        }
    }
}

// Compose UI 中判断
@Composable
fun ConfigScreen(viewModel: ConfigViewModel = koinViewModel()) {
    val state by viewModel.configState.collectAsState()

    when (state) {
        is InitState.Uninitialized -> LoadingView()
        is InitState.Value -> ConfigView((state as InitState.Value).data)
    }
}
```

## 项目约定

### 构建配置
- 所有模块的通用 Android 配置在根 `build.gradle.kts` 中统一管理
- Application 和 Library 模块使用不同的配置函数，确保类型安全
- Kotlin 编译器启用 `-XXLanguage:+ExplicitBackingFields` 特性

### 依赖管理
- 使用 `gradle/libs.versions.toml` 统一管理所有依赖版本
- 避免在模块的 `build.gradle.kts` 中硬编码版本号
- Debug 工具（Chucker、LeakCanary、Glance）仅在 Debug 版本引入

### 代码风格
- 优先使用 Kotlin 协程和 Flow 处理异步操作
- 网络请求遵循"动词用 suspend，名词用 Flow"原则
- ViewModel 使用 Explicit Backing Fields 简化 StateFlow 声明
- 使用 `@Serializable` 而非反射进行 JSON 序列化

### 测试规范
- 单元测试使用 Hamcrest 匹配器提高可读性
- 测试类命名：`<ClassName>Test`（如 `YkConfigManagerTest`）
- 关键业务逻辑必须有对应的单元测试覆盖

---

## 常见开发场景

### 添加新的网络请求
1. 在 Repository 中定义 suspend 函数
2. 使用 `@Serializable` 定义响应数据类
3. ViewModel 中使用 `requestStateFlow` (UI) 或 `requestResult` (后台)
4. 参考: [network.md](.claude/docs/network.md)

### 添加新的配置项
1. 在 `android_env.json` 中添加配置
2. 在配置数据类中添加对应字段（使用 `@SerialName`）
3. 通过 `YkConfigManager.config` 访问
4. 参考: [modules.md](.claude/docs/modules.md#配置管理)

### 添加新的初始化任务
1. 实现 `Initializer<Unit>` 接口
2. 在 `dependencies()` 中声明依赖关系
3. 在 Application 中通过 `AppInitializer.addTask()` 注册
4. 参考: AppInitializer.kt:28

### 添加新的依赖
1. 在 `gradle/libs.versions.toml` 的 `[versions]` 添加版本号
2. 在 `[libraries]` 添加依赖声明
3. 在模块的 `build.gradle.kts` 中引用
4. 参考: [build-publish.md](.claude/docs/build-publish.md)

### 使用 Koin 注入依赖
1. 在模块中定义 `@Module` 对象
2. 使用 `@Singleton` / `@Factory` 标记提供函数
3. ViewModel 使用 `@KoinViewModel` + `@Inject` 构造器
4. 参考: [dependency-injection.md](.claude/docs/dependency-injection.md)

---

## 注意事项

### 构建相关
- 确保 `android_env.json` 和 `keystore.properties` 已正确配置
- Debug 工具（Chucker、LeakCanary、Glance）仅在 Debug 版本可用
- 版本号通过 Git commit 计数自动生成（基础值 4645）

### 代码规范
- 网络请求遵循"动词用 suspend，名词用 Flow"原则
- ViewModel 使用 Explicit Backing Fields 简化 StateFlow 声明
- 使用 `@Serializable` 而非反射进行 JSON 序列化
- 避免在模块的 `build.gradle.kts` 中硬编码版本号

### 依赖管理
- 所有依赖版本在 `gradle/libs.versions.toml` 中统一管理
- 根 `build.gradle.kts` 强制统一 `activity` 和 `kotlinx-coroutines-core` 版本
- 使用 Bundle 依赖配置简化多依赖引入

### 测试
- 单元测试使用 Hamcrest 匹配器（如 `assertThat(value, is(expected))`）
- 运行测试前确保相关模块已编译
- 参考: [testing.md](.claude/docs/testing.md)