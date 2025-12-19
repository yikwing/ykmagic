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

## suspend vs Flow 设计原则

### 核心原则

数据层保持 `suspend` 函数，ViewModel 层决定如何处理状态流。

### 记忆口诀

**动词用 `suspend`，名词用 `Flow`**

| 特性 | suspend | Flow |
|------|---------|------|
| 数据性质 | 静态、一次性 | 动态、持续性 |
| 结果数量 | 单个 | 多个 |
| 生命周期 | 执行后结束 | 取消前持续 |
| 典型场景 | 发布、删除、点赞、上传 | 动态列表、实时通知 |

### 示例

```kotlin
// suspend - 一次性操作
suspend fun createUser(user: User): Result<User>
suspend fun deletePost(id: String): Result<Unit>
suspend fun uploadFile(file: File): Result<String>

// Flow - 持续观测
fun observeUsers(): Flow<List<User>>
fun observeNotifications(): Flow<Notification>
fun observeConnectionState(): Flow<ConnectionState>
```

---

## Flow 操作模式

### 操作对比

| 操作 | 行为 | 适用场景 |
|------|------|---------|
| `.stateIn()` / `.collectAsState()` | 持续观测，数据变化自动更新 | UI 实时显示 |
| `.first()` | 一次性获取当前值 | 初始化、条件判断 |
| `.firstOrNull()` | 一次性获取，空流返回 null | 安全读取 |

### ViewModel 中转换 Flow 为 StateFlow

```kotlin
val userName: StateFlow<String> = dataStore.data
    .map { it.name }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),  // 5秒超时处理配置变更
        initialValue = ""
    )
```

### Compose 中收集 Flow

```kotlin
// StateFlow - 不需要 initial（自带初始值）
val state by viewModel.uiState.collectAsState()

// 普通 Flow - 必须提供 initial
val userName by context.userPreferencesStore.data
    .map { it.name }
    .collectAsState(initial = "")  // initial 类型必须匹配
```

### 注意事项

- `StateFlow.collectAsState()` 不需要 initial
- `Flow.collectAsState(initial = ...)` 必须提供 initial
- initial 类型必须与 Flow 泛型类型一致

---

## Flow 生命周期收集

### repeatOnLifecycle 状态选择

#### Lifecycle.State.STARTED（推荐默认值）

| 属性 | 说明 |
|------|------|
| 生命周期范围 | `onStart` ↔ `onStop` |
| 开始条件 | 页面可见时（包括被半透明对话框遮挡、分屏模式） |
| 停止条件 | 页面完全不可见时（按 Home 键、跳转新页面） |
| 记忆口诀 | "只要眼睛能看到，就更新；看不到，就暂停" |

**适用场景**：
- 收集 `StateFlow` / `SharedFlow` 更新 UI（标准姿势）
- 解决内存泄漏和后台资源浪费问题
- 用户回到页面时数据立刻恢复更新

**结论**：如果不确定选哪个，闭眼选 `STARTED`。

#### Lifecycle.State.RESUMED（最严格）

| 属性 | 说明 |
|------|------|
| 生命周期范围 | `onResume` ↔ `onPause` |
| 开始条件 | 页面可见 **且** 拥有焦点（用户可交互） |
| 停止条件 | 失去焦点（系统弹窗、半透明 Activity 覆盖、分屏切换） |
| 记忆口诀 | "只有当你能实际操作这个页面时，才运行" |

**适用场景**：
- 独占资源：相机预览、麦克风录音
- 高频传感器：重力感应游戏
- 高耗能动画：复杂粒子动画

**结论**：仅在需要"用户必须处于交互状态"时使用。

### 代码示例

**Fragment 中收集 Flow**：

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            updateUI(state)
        }
    }
}
```

**Compose 中使用**：

```kotlin
// collectAsStateWithLifecycle 内部使用 STARTED
val state by viewModel.uiState.collectAsStateWithLifecycle()
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

## （待补充更多模式）