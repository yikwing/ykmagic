# UI 事件处理指南（遵循 Google 官方架构指南）

依据：[Android Developers - UI events](https://developer.android.com/topic/architecture/ui-layer/events) ·
[ViewModel: One-off event antipatterns](https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95)

**速记**：

- **用户事件、无业务逻辑**（展开、跳转）→ UI 直接处理，导航用 `dropUnlessResumed`
- **ViewModel 事件**（Toast、Snackbar、校验后跳转）→ **转成 UI State**，UI 处理完回调 ViewModel 清除
- **跨页面 / 全局状态**（登录态）→ Repository 作为单一数据源（SSOT）暴露 `Flow`，ViewModel 用 `stateIn` 转成 `StateFlow`
- **跨模块通信** → 依赖倒置：公共模块定义接口，DI 注入
- **禁止**：ViewModel 用 `Channel` / `SharedFlow` 向 UI 发一次性事件；全局事件总线（EventBus / FlowBus）
- `SingleLiveEvent` 属于旧方案，新代码不再引入

生命周期收集方式见：[flow-collect-best-practices.md](flow-collect-best-practices.md)

---

## 1. 核心原则

> UI actions that originate from the ViewModel—ViewModel events—should always result in a UI state update.

- **单向数据流（UDF）**：状态向下流向 UI，事件向上流向 ViewModel
- **状态驱动而非事件驱动**：不去想"UI 要做什么动作"，而是想"这个动作如何影响 UI State"
- **为什么不用 Channel / SharedFlow 发事件**：ViewModel 的生命周期比 UI 长，在配置变更或进入后台的间隙，UI 没有在收集，事件可能丢失或者没被处理，界面因此进入不一致状态。State 在重建后可以重现，不会丢

---

## 2. 快速选型

| 场景 | 推荐做法 | 原因 |
|---|---|---|
| 点击跳转、展开收起（无业务逻辑） | UI 直接处理，导航包一层 `dropUnlessResumed` | 不经过 ViewModel；防止离开页面后的重复点击 |
| Toast / Snackbar 提示 | UI State 中的 `userMessage` 字段加 `userMessageShown()` 回调 | 配置变更后不丢失，消费后清除也不会重复弹出 |
| 校验通过后跳转（如登录成功） | UI State 中的 `isUserLoggedIn` 等结果字段 | 跳转条件是状态，可重现 |
| 弹窗显示 / 隐藏 | UI State 中的 `showXxxDialog: Boolean` | 弹窗本身就是界面状态 |
| 登录态、用户信息等全局状态 | Repository 暴露 `Flow`，ViewModel 用 `stateIn` | 单一数据源，多个页面各自观察 |
| 模块 A 需要感知模块 B 的状态 | 公共模块定义接口（暴露 `Flow`），实现模块提供，Koin 注入 | 依赖显式可见，Find Usages 可追踪 |

---

## 3. 用户事件：UI 直接处理

不涉及业务逻辑的事件不经过 ViewModel：

```kotlin
Button(onClick = dropUnlessResumed { navigator.navigate(DetailRoute) }) { ... }
```

- `dropUnlessResumed` 只在 `RESUMED` 状态执行回调，防止跳转动画期间重复点击导致重复导航
- 需要参数的回调使用 `module_compose` 中的带参版本 `com.yikwing.compose.lifecycle.dropUnlessResumed`

---

## 4. ViewModel 事件 → UI State

### 4.1 提示消息（Toast / Snackbar）

```kotlin
data class MyUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
)

class MyViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            if (!hasNetwork()) {
                _uiState.update { it.copy(userMessage = "No Internet connection") }
                return@launch
            }
            // ...
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
```

```kotlin
@Composable
fun MyScreen(vm: MyViewModel = koinViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.userMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            vm.userMessageShown()
        }
    }
    // ...
}
```

**关键点**：
- 消息是瞬时的，但 UI State 仍然如实反映每一时刻屏幕上显示的内容
- UI **处理完之后**必须回调 ViewModel 清除，否则重建后会再次显示
- 需要连续显示多条消息时，改用 `userMessages: List<UserMessage>`（每条带唯一 `id`），按 `id` 逐条清除

### 4.2 校验后导航

ViewModel 只把结果写进 State，由 UI 决定何时导航：

```kotlin
data class LoginUiState(
    val isUserLoggedIn: Boolean = false,
    // ...
)
```

```kotlin
@Composable
fun LoginScreen(vm: LoginViewModel = koinViewModel(), onUserLogIn: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnUserLogIn by rememberUpdatedState(onUserLogIn)

    LaunchedEffect(vm, lifecycle) {
        vm.uiState
            .filter { it.isUserLoggedIn }
            .flowWithLifecycle(lifecycle)
            .collect { currentOnUserLogIn() }
    }
    // ...
}
```

**关键点**：
- 如果当前页在导航后仍留在返回栈中，返回时状态依然是 `isUserLoggedIn = true`，会再次触发跳转。这种情况需要额外的状态（如 `validationInProgress`），或者在导航后回调 ViewModel 重置状态
- 用 `setRoot` 等方式把当前页移出返回栈时，不存在这个问题

---

## 5. 跨页面 / 全局状态：SSOT

全局状态不靠广播，而是由 Repository 作为单一数据源对外暴露只读的 `Flow`：

```kotlin
class UserRepository(...) {
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it.token.isNotEmpty() }
}

class ProfileViewModel(userRepository: UserRepository) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = userRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
```

- `WhileSubscribed(5_000)`：UI 不可见超过 5 秒后停止上游；在 5 秒内的配置变更不会重启上游
- 登录、登出只修改数据源，所有观察方会自动收到新状态，不需要发送"登录成功"事件

---

## 6. 跨模块通信：依赖倒置

```kotlin
// :common
interface AccountObserver {
    val isLoggedIn: Flow<Boolean>
}

// :account（实现，并在 Koin 模块中绑定为 AccountObserver）
class AccountObserverImpl(repo: UserRepository) : AccountObserver {
    override val isLoggedIn = repo.isLoggedIn
}

// :shop（只依赖 :common 中的接口）
class CartViewModel(accountObserver: AccountObserver) : ViewModel() { ... }
```

依赖关系在编译期就是显式的，不用字符串或 Map 盲发事件。

---

## 7. 常见误区

**误区 1：ViewModel 用 `Channel` / `SharedFlow` 发 Toast、导航**

UI 没在收集时（配置变更、进入后台），事件可能丢失或者不被处理。改为写入 UI State（见第 4 节）。

**误区 2：全局事件总线（EventBus / RxBus / FlowBus）**

把 `EventBus.post()` 换成 `MutableSharedFlow.emit()` 只是换了 API，仍然是隐式的多对多通信：数据流向变成网状，无法追踪谁在何时发送了事件。改为 SSOT（第 5 节）或依赖倒置（第 6 节）。

**误区 3：State 中的一次性字段处理后不清除**

`userMessage`、`isUserLoggedIn` 等字段在 UI 处理完后没有回调 ViewModel 重置，重建或返回时会重复触发。

**误区 4：无业务逻辑的点击也绕道 ViewModel**

纯 UI 行为（跳转、展开）直接在 UI 层处理，不需要"UI → ViewModel → UI"兜一圈。

**误区 5：收集 Flow 时不绑定生命周期**

Compose 用 `collectAsStateWithLifecycle`，其他收集方式见：[flow-collect-best-practices.md](flow-collect-best-practices.md)

---

## 8. 选型原则

| 语义 | 做法 |
|---|---|
| 页面状态（含一次性提示、导航结果） | `StateFlow<UiState>` |
| 无业务逻辑的用户事件 | UI 直接处理 |
| 全局 / 跨页面状态 | Repository `Flow` + `stateIn` |
| 跨模块 | 接口 + DI |
| 旧代码单次事件 | `SingleLiveEvent`（维持，不扩散），逐步迁移为 UI State |

决策起点：先问"这个动作如何影响 UI State"，而不是"UI 需要执行什么动作"。

---

## 附录：`SharedFlow` / `Channel` 参数参考

二者仍可用于**数据层或非 UI 内部**的热流，例如 `callbackFlow` 桥接、`shareIn` 共享上游、串行任务队列。**不用于** ViewModel 向 UI 发一次性事件。

### A. `MutableSharedFlow`

```kotlin
MutableSharedFlow<T>(
    replay = 0,
    extraBufferCapacity = 0,
    onBufferOverflow = BufferOverflow.SUSPEND,
)
```

| 参数 | 作用 |
|---|---|
| `replay` | 新订阅者进入时补发最近 N 条；`0` 不回放，`1` 粘性 |
| `extraBufferCapacity` | 为**当前**订阅者提供额外缓冲，不影响历史回放 |
| `onBufferOverflow` | 缓冲区满时的策略：`SUSPEND` / `DROP_OLDEST` / `DROP_LATEST`；仅在 `replay + extraBufferCapacity > 0` 时生效 |

| API | 行为 |
|---|---|
| `emit` | 挂起，遵守背压规则 |
| `tryEmit` | 非挂起，返回 `Boolean`；无缓冲且无订阅者时直接返回 `false` |

### B. `Channel`

| 写法 | 含义 |
|---|---|
| `Channel(0)` | 无缓冲，发送接收必须同步 |
| `Channel(n)` | 缓冲 n 条 |
| `Channel(Channel.BUFFERED)` | 系统默认缓冲（64） |
| `Channel(Channel.CONFLATED)` | 只保留最新值 |
| `Channel(Channel.UNLIMITED)` | 无限缓冲（谨慎使用） |

| API | 行为 |
|---|---|
| `send` | 可能挂起，等待缓冲区空闲 |
| `trySend` | 非挂起，立即返回 `ChannelResult` |

- 对外暴露时用 `receiveAsFlow()` 隐藏实现
- 单播语义：多个收集者**竞争消费**，每条消息只被其中一个收到；需要广播时用 `SharedFlow`

### C. `StateFlow` 特点

- 始终持有当前值，新订阅者立即获得当前值
- 基于 `equals` 去重，值相同时不触发更新
