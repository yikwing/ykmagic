# Android 开发模式与技巧

本文档收集 Android 开发中常用的设计模式、最佳实践和代码片段，方便后续查阅。

---

## 一次性事件处理 (Event Wrapper)

### 问题背景

使用 LiveData 或 StateFlow 传递一次性事件（如 Toast、Snackbar、导航）时，配置变更（如屏幕旋转）会导致事件被重复消费。

### 解决方案

使用 Event 包装类，确保事件只被消费一次：

```kotlin
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
```

### 使用示例

**ViewModel 层**：

```kotlin
class MyViewModel : ViewModel() {
    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> = _toastEvent

    fun onSaveClick() {
        _toastEvent.value = Event("保存成功")
    }
}
```

**UI 层（View 体系）**：

```kotlin
viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
    event.getContentIfNotHandled()?.let { message ->
        showToast(message)
    }
}
```

**UI 层（Compose）**：

```kotlin
val toastEvent by viewModel.toastEvent.observeAsState()
LaunchedEffect(toastEvent) {
    toastEvent?.getContentIfNotHandled()?.let { message ->
        // 显示 Toast 或 Snackbar
    }
}
```

### 适用场景

- Toast / Snackbar 提示
- 导航事件
- 对话框显示
- 一次性错误提示

### 替代方案（Compose 推荐）

在纯 Compose 项目中，可使用 `Channel` + `Flow` 实现更优雅的一次性事件：

```kotlin
class MyViewModel : ViewModel() {
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun showToast(message: String) {
        viewModelScope.launch {
            _events.send(UiEvent.ShowToast(message))
        }
    }
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

// Compose UI
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is UiEvent.ShowToast -> { /* 显示 Toast */ }
            is UiEvent.Navigate -> { /* 导航 */ }
        }
    }
}
```

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