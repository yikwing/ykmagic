# Android 开发模式与技巧

本文档收集 Android 开发中常用的设计模式、最佳实践和代码片段，方便后续查阅。

---

## 一次性事件处理

> 参考：[Android 现代架构不需要事件总线](https://juejin.cn/post/7625074868355973147)

### 为什么不需要 EventBus

| EventBus 缺陷 | 现代替代方案 |
|--------------|-------------|
| 无类型安全（`Any` + 字符串 tag） | sealed class / sealed interface，编译期检查 |
| 不感知生命周期，需手动注册/反注册 | `repeatOnLifecycle` 自动管理，页面不可见时暂停 |
| 隐式耦合，事件流向不可追踪 | ViewModel 是显式依赖，通过构造注入可见 |
| `replay=1` 导致屏幕旋转重复触发 | `Channel`（不重放）精确控制交付语义 |
| 跨层调用，违反架构分层 | 通过 Koin 注入共享 Repository 传递数据 |

**结论**：EventBus 的所有使用场景均可被 `StateFlow` + `Channel` + `SharedFlow` 覆盖，无需引入第三方库。

### 方案选型

| 需求 | 方案 | 原因 |
|------|------|------|
| ViewModel → UI 状态（可重放） | `StateFlow` | 新订阅者立即获得当前值 |
| ViewModel → UI 一次性事件（Compose） | `Channel + receiveAsFlow` | 不重放，线程安全，不丢事件 |
| ViewModel → UI 一次性事件（View 体系） | `Channel + repeatOnLifecycle` | 生命周期安全 |
| 跨组件广播，所有订阅者都收到 | `SharedFlow(replay=0)` | 多播，无重放 |
| 跨组件广播，只消费一次 | `Channel`（竞争消费）或责任链 | 见下文 |

**核心原则**：`StateFlow` 管状态（可重放），`Channel` / `SharedFlow` 管事件（不重放或受控重放）。

---

### Channel 详解

#### 四种容量模式

```kotlin
Channel<T>()                    // RENDEZVOUS：容量=0，send 挂起直到对方 receive
Channel<T>(Channel.BUFFERED)    // 容量=64，满了才挂起（事件场景首选）
Channel<T>(Channel.UNLIMITED)   // 无限容量，send 永不挂起，小心 OOM
Channel<T>(Channel.CONFLATED)   // 容量=1，新值覆盖旧值，只保留最新
```

#### send vs trySend

```kotlin
// send：挂起版，需在协程中调用
viewModelScope.launch { channel.send(event) }

// trySend：非挂起版，立即返回，缓冲区满时失败
val result = channel.trySend(event)
if (result.isFailure) { /* 缓冲区满或 Channel 已关闭 */ }
```

#### receiveAsFlow vs consumeAsFlow

```kotlin
// receiveAsFlow()：多个收集者共享 Channel，竞争消费（ViewModel 暴露给 UI 用这个）
val events = _channel.receiveAsFlow()

// consumeAsFlow()：只允许一个收集者，多次 collect 崩溃，慎用
```

---

### Compose 推荐：`Channel` + `receiveAsFlow()`

**ViewModel**：

```kotlin
sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data object NavigateToHome : UiEvent
}

@KoinViewModel
class MyViewModel : ViewModel() {
    // UI 状态 —— StateFlow（订阅时重放最新值）
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState = _uiState.asStateFlow()

    // 一次性事件 —— Channel（不重放）
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onLoginClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _events.send(UiEvent.ShowToast("登录成功"))
            _events.send(UiEvent.NavigateToHome)
        }
    }
}
```

**Compose UI**：

```kotlin
@Composable
fun MyScreen(vm: MyViewModel = koinViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {  // Unit 作 key，只启动一次
        vm.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                UiEvent.NavigateToHome -> { /* 导航 */ }
            }
        }
    }

    if (uiState.isLoading) CircularProgressIndicator()
}
```

**关键点**：
- `Channel.BUFFERED`：UI 未就绪时事件不丢失
- `LaunchedEffect(Unit)`：只启动一次，不随重组重复订阅
- 导航事件优先用回调 lambda 传入，无需放进 `events`
- ViewModel 销毁时 `viewModelScope` 取消，Channel 自动回收，无需手动 close

---

### 跨组件通信（替代 EventBus 的场景）

#### 同一 Activity 的 Fragment 间通信 → `activityViewModels()`

```kotlin
// 共享 ViewModel（Activity 级别）
@KoinViewModel
class SharedViewModel : ViewModel() {
    private val _event = MutableSharedFlow<AppEvent>(extraBufferCapacity = 16)
    val event: SharedFlow<AppEvent> = _event.asSharedFlow()

    fun send(event: AppEvent) { viewModelScope.launch { _event.emit(event) } }
}

// FragmentA 发送
val sharedVm: SharedViewModel by activityViewModels()
sharedVm.send(AppEvent.UserSelected(userId))

// FragmentB 接收
viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        sharedVm.event.collect { handleEvent(it) }
    }
}
```

#### 真正跨页面的全局事件 → Koin 单例 Repository 中的 `SharedFlow`

```kotlin
// 在 Repository 中持有 SharedFlow，通过 Koin 注入
@Single
class AppStateRepository {
    private val _event = MutableSharedFlow<AppEvent>(extraBufferCapacity = 16)
    val event: SharedFlow<AppEvent> = _event.asSharedFlow()

    fun send(event: AppEvent) { _event.tryEmit(event) }
}

// ViewModel 注入使用，无需全局单例 object
@KoinViewModel
class HomeViewModel(private val appState: AppStateRepository) : ViewModel() {
    val events = appState.event
}
```

#### 广播给所有活跃订阅者 → `SharedFlow(replay=0)`

```kotlin
// replay=0：不重放，无订阅者时丢弃（有时效性的事件）
private val _events = MutableSharedFlow<AppEvent>(
    replay = 0,
    extraBufferCapacity = 16,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)
```

#### 多订阅者但只消费一次

**情况1：同时只有一个活跃（页面栈场景）**

直接用 `Channel`——后台页面协程已停止，事件自然只被前台页面消费。

**情况2：多个都活跃，需指定优先级处理 → 责任链**

```kotlin
// 参考 Android OnBackPressedDispatcher 设计
object AppEventBus {
    private val handlers = ArrayDeque<(AppEvent) -> Boolean>()

    fun register(handler: (AppEvent) -> Boolean) = handlers.addFirst(handler)
    fun unregister(handler: (AppEvent) -> Boolean) = handlers.remove(handler)

    // 第一个返回 true 的处理，后续跳过
    fun post(event: AppEvent) = handlers.firstOrNull { it(event) }
}
```

---

### 场景速查

| 场景 | 方案 |
|------|------|
| ViewModel → 当前页面（Toast/导航） | `Channel` |
| 多订阅者，同时只有一个活跃 | `Channel` |
| 多订阅者都活跃，全部收到 | `SharedFlow(replay=0)` |
| 多订阅者都活跃，优先级处理 | 责任链 |
| 同一 Activity 下 Fragment 间通信 | `activityViewModels()` 共享 ViewModel |
| 真正的全局跨页面事件 | Koin 单例 Repository 中的 `SharedFlow` |

---

## HorizontalPager 无法滑动

### 问题现象

`HorizontalPager` 无法响应滑动手势，页面切换失效。

### 根本原因

Pager 的子项内容没有占满整个页面区域，导致手势检测区域太小。

### 解决方案

确保每个页面内容使用 `fillMaxSize()` 占满整个 Pager 区域：

```kotlin
HorizontalPager(state = pagerState) { page ->
    when (page) {
        0 -> HomeScreen(modifier = Modifier.fillMaxSize())
        1 -> ProfileScreen(modifier = Modifier.fillMaxSize())
    }
}
```

### 错误示例

```kotlin
// 错误：Box 没有尺寸约束，手势区域太小
@Composable
fun HomeScreen() {
    Box(contentAlignment = Alignment.Center) {
        Text("Home")
    }
}
```

### 正确示例

```kotlin
// 正确：fillMaxSize() 让内容占满整个页面
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Home")
    }
}
```

### 排查清单

1. 检查 Pager 子项是否有 `fillMaxSize()`
2. 检查是否有嵌套滚动冲突（如 LazyColumn 嵌套在 Pager 中）
3. 检查 `userScrollEnabled` 是否被设为 `false`

---

## Result 错误处理（Go 风格）

### 设计理念

类似 Go 语言的 `(value, error)` 返回模式，使用 Kotlin 标准库的 `Result<T>` 替代抛异常或返回可空类型。

### 为什么用 Result

| 方式 | 问题 |
|------|------|
| 抛异常 | 调用方容易忘记处理，运行时崩溃 |
| 返回 `T?` | 无法区分"无数据"和"出错"，丢失错误信息 |
| 自定义 `Pair<Boolean, T?>` | 冗余，类型不安全 |
| `Result<T>` | 强制处理成功/失败，保留异常信息 |

### 创建 Result

```kotlin
// 使用 runCatching 自动捕获异常
fun copyFile(src: File, dst: File): Result<File> = runCatching {
    src.copyTo(dst, overwrite = true)
}

// 手动创建
Result.success(file)
Result.failure(IOException("文件不存在"))
```

### 消费 Result

```kotlin
val result = copyAssetToCache(context, "config.json")

// 方式1: 链式处理（推荐）
result
    .onSuccess { file -> Log.d("Copy", "路径: ${file.absolutePath}") }
    .onFailure { e -> Log.e("Copy", "失败: ${e.message}") }

// 方式2: 获取值
val file = result.getOrNull()           // 失败返回 null
val fileOrDefault = result.getOrDefault(fallbackFile)
val fileOrThrow = result.getOrThrow()   // 失败抛异常

// 方式3: fold 模式匹配
val message = result.fold(
    onSuccess = { "保存到: ${it.absolutePath}" },
    onFailure = { "错误: ${it.message}" }
)

// 方式4: 检查状态
if (result.isSuccess) { /* ... */ }
if (result.isFailure) { /* ... */ }
result.exceptionOrNull()  // 获取异常，成功返回 null
```

### 链式转换

```kotlin
copyAssetToCache(context, "data.json")
    .map { file -> file.readText() }           // 成功时转换
    .mapCatching { json -> Json.parse(json) }  // 转换可能抛异常
    .recover { e -> "默认值" }                  // 失败时恢复
    .getOrNull()
```

### 适用场景

- IO 操作（文件读写、网络请求）
- 解析操作（JSON、XML）
- 任何可能失败的操作

### 项目示例

```kotlin
// module_extension/io/AssetsExtensions.kt
fun copyAssetToCache(context: Context, fileName: String): Result<File> =
    runCatching {
        val cacheFile = File(context.cacheDir, fileName)
        context.assets.open(fileName).source().buffer().use { source ->
            cacheFile.sink().buffer().use { sink ->
                sink.writeAll(source)
            }
        }
        cacheFile
    }
```

---

## Nav3 导航命名规范

### 三层职责分离

| 层 | 命名规则 | 后缀 | 职责 |
|---|---|---|---|
| Route | `XxxRoute` | `Route` | 路由定义（NavKey），描述"去哪" |
| Entry | `xxxEntry()` | `Entry` | 注册路由，连接 Route → Screen |
| Screen | `XxxScreen` | `Screen` | 纯 UI Composable，不关心导航 |

### 文件组织

- 文件名跟随主体 Composable：`XxxScreen.kt`
- Route、Entry、Screen 放在同一个文件中
- Route 定义在文件顶部（`@Serializable` 注解之后）

### 示例

```kotlin
// TextDebounceScreen.kt

@Serializable
data object TextDebounceRoute : NavKey          // Route: 路由定义

fun EntryProviderScope<NavKey>.textDebounceEntry() {  // Entry: 注册路由
    entry<TextDebounceRoute> {
        val navigator = LocalNavigator.current
        TextDebounceScreen(                     // Screen: 纯 UI
            navigationToPackInfo = dropUnlessResumed { navigator.navigate(PackageInfoRoute) },
        )
    }
}

@Composable
fun TextDebounceScreen(                         // Screen: 不感知导航细节
    navigationToPackInfo: () -> Unit,
) { /* UI */ }
```

### 带参数的 Route

```kotlin
@Serializable
data class ProductRoute(val id: String) : NavKey

fun EntryProviderScope<NavKey>.otherPageEntry() {
    entry<ProductRoute> { product ->
        OtherPageScreen(product.id)
    }
}
```

### 命名对照表

| Route (NavKey) | Entry 函数 | Screen (Composable) | 文件名 |
|---|---|---|---|
| `MainRoute` | `mainScreenEntry()` | `MainScreen()` | `MainScreen.kt` |
| `TextDebounceRoute` | `textDebounceEntry()` | `TextDebounceScreen()` | `TextDebounceScreen.kt` |
| `ProductRoute` | `otherPageEntry()` | `OtherPageScreen()` | `OtherPageScreen.kt` |
| `AuthLoginRoute` | `authLoginEntry()` | `AuthLoginScreen()` | `AuthLoginScreen.kt` |

---

## Nav3 Entry 生命周期行为

### Entry 与 Composition 的关系（重要！）

Nav3 导航时 entry **不是**保持在 STARTED，而是**被移出 Composition**，返回时重新进入。

```
操作              Composition          ViewModel/SavedState
─────────────────────────────────────────────────────────
进入 A            A 进入               A VM 创建
A → B（动画中）   A、B 同时存在        —
A → B（动画后）   A 移出，B 保留       A VM 存活（在 backStack）
B → 返回（动画中）A、B 同时存在        —
B → 返回（动画后）B 移出，A 重新进入   B VM 销毁
setRoot(C)        A 移出，C 进入       A VM 销毁
```

### 两个 Decorator 的分工

| Decorator | 保存内容 | 移除时机 |
|---|---|---|
| `rememberViewModelStoreNavEntryDecorator` | ViewModel 实例 | entry 从 backStack pop |
| `rememberSaveableStateHolderNavEntryDecorator` | `rememberSaveable` 值 | entry 从 backStack pop |

### 生命周期 Hook

由于 entry 离开时会被移出 Composition，**用 `DisposableEffect` 即可**，无需 `LifecycleResumeEffect`：

```kotlin
// 进入/返回时执行，离开时清理
DisposableEffect(Unit) {
    val listener = register()
    onDispose { listener.unregister() }  // 移出 Composition 后（动画结束后）触发
}
```

### WebView 在 Nav3 中的处理

由于 entry 移出 Composition 后 WebView 实例销毁，需通过 ViewModel 保存状态：

```kotlin
class WebViewModel : ViewModel() {
    val savedState = Bundle()  // 跟随 entry 存活，不随 Composition 销毁
}

AndroidView(
    factory = { context ->
        WebView(context).apply {
            if (!vm.savedState.isEmpty) restoreState(vm.savedState) else loadUrl(url)
        }
    },
    onRelease = { webView -> webView.saveState(vm.savedState) },
)
```

---

## （待补充更多模式）