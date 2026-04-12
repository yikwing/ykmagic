# 事件处理模式

> 参考：[Android 现代架构不需要事件总线](https://juejin.cn/post/7625074868355973147)

## 为什么不需要 EventBus

| EventBus 缺陷 | 现代替代方案 |
|--------------|-------------|
| 无类型安全（`Any` + 字符串 tag） | sealed class / sealed interface，编译期检查 |
| 不感知生命周期，需手动注册/反注册 | `repeatOnLifecycle` 自动管理，页面不可见时暂停 |
| 隐式耦合，事件流向不可追踪 | ViewModel 是显式依赖，通过构造注入可见 |
| `replay=1` 导致屏幕旋转重复触发 | `Channel`（不重放）精确控制交付语义 |
| 跨层调用，违反架构分层 | 通过 Koin 注入共享 Repository 传递数据 |

**结论**：EventBus 的所有使用场景均可被 `StateFlow` + `Channel` + `SharedFlow` 覆盖，无需引入第三方库。

## 方案选型

| 需求 | 方案 | 原因 |
|------|------|------|
| ViewModel → UI 状态（可重放） | `StateFlow` | 新订阅者立即获得当前值 |
| ViewModel → UI 一次性事件（Compose） | `Channel + receiveAsFlow` | 不重放，线程安全，不丢事件 |
| ViewModel → UI 一次性事件（View 体系） | `Channel + repeatOnLifecycle` | 生命周期安全 |
| 跨组件广播，所有订阅者都收到 | `SharedFlow(replay=0)` | 多播，无重放 |
| 跨组件广播，只消费一次 | `Channel`（竞争消费）或责任链 | 见下文 |

**核心原则**：`StateFlow` 管状态（可重放），`Channel` / `SharedFlow` 管事件（不重放或受控重放）。

---

## Channel 详解

### 四种容量模式

```kotlin
Channel<T>()                    // RENDEZVOUS：容量=0，send 挂起直到对方 receive
Channel<T>(Channel.BUFFERED)    // 容量=64，满了才挂起（事件场景首选）
Channel<T>(Channel.UNLIMITED)   // 无限容量，send 永不挂起，小心 OOM
Channel<T>(Channel.CONFLATED)   // 容量=1，新值覆盖旧值，只保留最新
```

### send vs trySend

```kotlin
// send：挂起版，需在协程中调用
viewModelScope.launch { channel.send(event) }

// trySend：非挂起版，立即返回，缓冲区满时失败
val result = channel.trySend(event)
if (result.isFailure) { /* 缓冲区满或 Channel 已关闭 */ }
```

### receiveAsFlow vs consumeAsFlow

```kotlin
// receiveAsFlow()：多个收集者共享 Channel，竞争消费（ViewModel 暴露给 UI 用这个）
val events = _channel.receiveAsFlow()

// consumeAsFlow()：只允许一个收集者，多次 collect 崩溃，慎用
```

---

## Compose 推荐：`Channel` + `receiveAsFlow()`

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

## 跨组件通信（替代 EventBus 的场景）

### 同一 Activity 的 Fragment 间通信 → `activityViewModels()`

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

### 真正跨页面的全局事件 → Koin 单例 Repository 中的 `SharedFlow`

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

### 广播给所有活跃订阅者 → `SharedFlow(replay=0)`

```kotlin
// replay=0：不重放，无订阅者时丢弃（有时效性的事件）
private val _events = MutableSharedFlow<AppEvent>(
    replay = 0,
    extraBufferCapacity = 16,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)
```

### 多订阅者但只消费一次

**情况1：同时只有一个活跃（页面栈场景）**

直接用 `Channel`——后台页面协程已停止，事件自然只被前台页面消费。

**情况2：多个都活跃，需指定优先级处理 → 责任链**

参考 `OnBackPressedDispatcher` 设计——持有有序处理链，第一个返回 `true` 的处理器消费事件，后续跳过。将处理链注入到需要的组件中（通过 Koin 单例），而非 `object` 全局单例，以保持可测试性。

此场景极少见；大多数情况下 `Channel` 已足够。

---

## 场景速查

| 场景 | 方案 |
|------|------|
| ViewModel → 当前页面（Toast/导航） | `Channel` |
| 多订阅者，同时只有一个活跃 | `Channel` |
| 多订阅者都活跃，全部收到 | `SharedFlow(replay=0)` |
| 多订阅者都活跃，优先级处理 | 责任链 |
| 同一 Activity 下 Fragment 间通信 | `activityViewModels()` 共享 ViewModel |
| 真正的全局跨页面事件 | Koin 单例 Repository 中的 `SharedFlow` |
