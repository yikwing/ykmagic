# 模块化设计

项目采用多模块架构，每个模块独立负责特定功能，可以按需引入。

## 模块列表

### 1. module_config - 配置管理模块
- 通过 `android_env.json` 注入配置数据
- 使用 kotlinx.serialization 静态解析 JSON 配置
- 提供类型安全的配置访问

### 2. module_network - 网络请求模块
- 基于 Ktor Client + CIO Engine 封装
- 提供 `Json` 序列化配置（`NetworkModule`）
- 提供 `RequestState` / `ApiException` / `ApiTransform` 等基础组件
- HttpClient 在 app 层 `AppNetworkModule` 中构建
- 详见 [network.md](network.md)

### 3. module_extension - 扩展方法模块
- 提供各类 Kotlin 扩展函数和工具类
- 包含协程工具、图片压缩、日期处理等
- `GlobalContextProvider` 提供全局 Context 访问
- `CacheManager` 提供线程安全的内存缓存（LRU + TTL）
- `NetConnectManager` 响应式网络状态监听（StateFlow）
- `SessionManager` 会话时长记录（startSession / getSessionDuration / resetSession）
- `InitState<T>` 初始化状态密封类（`Uninitialized` / `Value<T>`，替代 `null` 表达"未初始化"）

### 4. module_compose - Compose UI 组件模块
- 提供可复用的 Compose 组件，避免在 app 层重复定义
- `layout/Center.kt` — 居中布局容器
- `state/LoadingWidget.kt` — 加载状态组件
- `state/NetWorkError.kt` — 网络错误状态组件（含重试按钮）
- `image/ImageWidget.kt` — 图片组件（`RoundedImage` / `CircleImage`）
- `window/SystemBarsStyle.kt` — 控制状态栏/导航栏图标颜色
- `interaction/DebounceClick.kt` — `rememberDebounceClick`，防抖点击
- `interaction/NoIndication.kt` — `NoIndication`，去除点击涟漪效果
- `lifecycle/AppLifecycleObserver.kt` — 宿主生命周期监听（前后台切换）
- `util/Ref.kt` — Compose 中可变引用持有器（`remember { Ref<T>() }`）

### 5. module_permission - 权限请求模块
- 基于 Fragment 封装统一的权限请求流程
- 简化权限申请逻辑

### 6. module_proxy - 基础组件模块
- 提供 BaseActivity、BaseFragment、LazyFragment
- ActivityHierarchyManager 管理 Activity 栈
- AppInitializer 支持模块化初始化

---

## 初始化架构 (AppInitializer)

位置: `module_proxy/.../startup/AppInitializer.kt`

### 核心机制

基于拓扑排序的模块初始化框架，支持声明依赖关系。

```kotlin
// 在 Application.onCreate() 中
AppInitializer.getInstance(this)
    .addTask(ConfigInjectInitTask())
    .addTask(NetworkInitTask())
    .build(debug = true)
```

### 创建初始化任务

```kotlin
class ConfigInjectInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        YkConfigManager.setUp(BuildConfig.YK_CONFIG)
    }
    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf()
}

// 依赖 Config
class NetworkInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        NetworkManager.init(context)
    }
    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(ConfigInjectInitTask::class.java)
}

// 执行顺序由拓扑排序自动决定: ConfigInjectInitTask → NetworkInitTask
```

### 关键特性

- 自动根据依赖关系排序执行
- 循环依赖检测（会抛出 "存在回环依赖" 错误）

---

## 配置管理 (module_config)

### 流程

`android_env.json` → `BuildConfig.YK_CONFIG`（构建时）→ `YkConfigManager.setUp()`（运行时）

### 使用步骤

1. 在根目录创建 `android_env.json` 配置文件:
   ```json
   {
     "base_url": "https://api.example.com"
   }
   ```
2. 定义配置类（使用 kotlinx.serialization）:
   ```kotlin
   @Serializable
   data class AppConfig(
       @SerialName("base_url") val baseUrl: String
   )
   ```
3. 在 Application 初始化时调用:
   ```kotlin
   YkConfigManager.setUp(BuildConfig.YK_CONFIG)
   ```
4. 获取配置:
   ```kotlin
   val baseUrl = YkConfigManager.config.baseUrl
   ```

### API

| 方法/属性 | 说明 |
|-----------|------|
| `setUp(json: String)` | 初始化配置 |
| `config` | 获取配置对象 |
| `isInitialized` | 检查是否已初始化 |

构建时注入：app/build.gradle.kts 中通过 `buildConfigField` 将 JSON 注入到 BuildConfig

---

## Activity 生命周期管理

位置: `module_proxy/.../util/ActivityHierarchyManager.kt`

线程安全（`CopyOnWriteArrayList`）+ 内存安全（`WeakReference`）的 Activity 栈管理器。
核心 API：`register` / `unregister` / `getTopActivity` / `finishTopActivities` / `finishAllExcept` / `finishAllActivities`。

使用时通过 `ActivityLifecycleCallbacks` 在 `onActivityCreated` / `onActivityDestroyed` 中调用 `register` / `unregister`。

---

## 内存缓存 (CacheManager)

位置: `module_extension/.../util/CacheManager.kt`

线程安全的 LRU 缓存（默认 256 条上限），支持 TTL 过期。核心 API：`put(key, value, ttlMillis?)` / `get<T>(key)` / `getOrPut(key, ttlMillis?) { }` / `remove(key)` / `clear()`。

---

## 网络状态管理 (NetConnectManager)

位置: `module_extension/.../network/NetConnectManager.kt`

响应式网络状态监听（StateFlow），支持 WIFI / CELLULAR / ETHERNET / VPN / NONE 五种类型和 VPN 检测。实现 `Closeable`。核心 API：`networkState: StateFlow` / `isCurrentlyConnected` / `isVpnActive`。
