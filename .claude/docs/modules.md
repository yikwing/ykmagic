# 模块化设计

项目采用多模块架构,每个模块独立负责特定功能,可以按需引入。

## 模块列表

### 1. module_config - 配置注入模块
- 通过 `android_env.json` 注入配置数据
- 维护全局 Application Context
- 使用 KSP 处理注解 `@YkConfigNode` 和 `@YkConfigValue`

### 2. module_network - 网络请求模块
- 基于 Ktor Client + OkHttp Engine 封装（已从 Retrofit 迁移）
- 提供统一的拦截器机制(HeaderInterceptor、RetryInterceptor、LogInterceptor)
- 支持 Debug 模式网络抓包视图(Chucker)
- 集成 kotlinx.serialization 进行 JSON 序列化
- 支持 IgnoreHttpResult 灵活解析响应
- 提供两种 API 请求方式:
  * `requestStateFlow` - 流式请求,返回 `Flow<RequestState<T>>`
  * `requestResult` - 单次请求,返回 `Result<T?>`

### 3. module_extension - 扩展方法模块
- 提供各类 Kotlin 扩展函数和工具类
- 包含协程工具、EventBus、图片压缩、日期处理等
- GlobalContextProvider 提供全局 Context 访问
- 资源扩展函数简化资源获取
- CacheManager 提供线程安全的内存缓存（LRU + TTL）

### 4. module_datastore - DataStore 封装
- 基于 Jetpack DataStore Preferences
- 提供属性委托方式的便捷访问

### 5. module_permission - 权限请求模块
- 基于 Fragment 封装统一的权限请求流程
- 简化权限申请逻辑

### 6. module_logger - 日志组件
- 基于 Logger 库的统一日志组件
- 支持自定义格式化策略

### 7. module_proxy - 基础组件模块
- 提供 BaseActivity、BaseFragment、LazyFragment
- ActivityHierarchyManager 管理 Activity 栈
- AppInitializer 支持模块化初始化

### 8. module_component - UI 组件模块
- RoundedImageView: 支持独立设置各角圆角半径
- ImBarWrapperView: 使用 WindowInsets 处理状态栏高度

## 初始化架构 (AppInitializer)

**核心机制**: 基于拓扑排序的模块初始化框架,支持声明依赖关系。

**使用方式**:
```kotlin
// 在 Application.onCreate() 中
AppInitializer.getInstance(this)
    .addTask(ConfigInjectInitTask())
    .addTask(LoggerInitTask())
    .addTask(NetworkInitTask())
    .addTask(DataStoreInitTask())
    .build(debug = true)
```

**创建初始化任务**:
```kotlin
class ConfigInjectInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        // 初始化代码
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(
        LoggerInitTask::class.java  // 声明依赖
    )
}
```

**关键特性**:
- 自动根据依赖关系排序执行
- 循环依赖检测(会抛出 "存在回环依赖" 错误)
- 位置: module_proxy/src/main/java/com/yikwing/proxy/startup/AppInitializer.kt:28

## 配置注入机制 (module_config)

**流程**: android_env.json → BuildConfig.YK_CONFIG → YkQuickManager.setUp() → KSP 生成代码

**使用步骤**:
1. 在根目录创建 `android_env.json` 配置文件
2. 使用注解标记配置类:
   ```kotlin
   @YkConfigNode
   @JsonClass(generateAdapter = true)
   data class NetworkConfig(
       @YkConfigValue(path = "base_url") val baseUrl: String
   )
   ```
3. KSP 会自动生成 YkQuickManager 和相关配置代码
4. 在 Application 中调用 `YkQuickManager.setUp(BuildConfig.YK_CONFIG)`

**构建时注入**: app/build.gradle.kts 中通过 `buildConfigField` 将 JSON 注入到 BuildConfig

## Activity 生命周期管理

**ActivityHierarchyManager**: 维护 Activity 栈的全局管理器
- 位置: module_proxy/src/main/java/com/yikwing/proxy/util/ActivityHierarchyManager.kt:6
- **线程安全**: 使用 `CopyOnWriteArrayList` 保证并发安全
- **内存安全**: 使用 `WeakReference` 避免内存泄漏
- **自动清理**: 自动过滤已销毁的 Activity 引用

**核心功能**:
- `register(activity)` / `unregister(activity)` - 注册/注销 Activity
- `getTopActivity()` - 获取栈顶 Activity
- `getActivityCount()` - 获取有效 Activity 数量
- `finishTopActivities(count)` - 从栈顶关闭指定数量的 Activity
- `finishUntil(activityClass, inclusive)` - 关闭到指定 Activity
- `finishAllExcept(activityClass)` - 关闭除指定外的所有 Activity
- `finishAllActivities()` - 关闭所有 Activity
- `contains(activity/activityClass)` - 检查是否包含指定 Activity

**使用示例**:
```kotlin
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

## 内存缓存 (CacheManager)

位置: module_extension/src/main/java/com/yikwing/extension/util/CacheManager.kt

**核心特性**:
- LRU 淘汰（默认 256 条上限）
- TTL 过期（基于单调时钟）
- 线程安全（synchronized 保护）

**使用示例**:
```kotlin
// 写入（永不过期）
CacheManager.put("user", userObj)

// 写入（5分钟过期）
CacheManager.put("token", "abc123", ttlMillis = 5 * 60 * 1000L)

// 读取
val user: User? = CacheManager.get("user")

// 获取或计算
val config = CacheManager.getOrPut("config", ttlMillis = 60_000L) {
    loadConfigFromDisk()
}
```