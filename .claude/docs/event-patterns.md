# Flow 事件流选型与参数指南

**速记**：

- **状态** → `StateFlow`
- **广播事件** → `SharedFlow`
- **单消费者命令** → `Channel`
- `SingleLiveEvent` 属于旧方案，新代码不再引入

生命周期收集方式见：[flow-collect-best-practices.md](flow-collect-best-practices.md)

---

## 1. 类型对照

| 类型 | 语义 | 适用场景 |
|---|---|---|
| `StateFlow` | 持有当前值的状态容器 | 页面状态、缓存状态、登录态 |
| `SharedFlow` | 多订阅者广播流 | 一次性通知、事件总线、跨组件广播 |
| `Channel` | 单消费者队列 | 命令派发、导航指令、串行任务 |
| `SingleLiveEvent` | 基于 LiveData 的单次事件 | 旧代码兼容，不扩散 |

核心区别：

- `StateFlow`：始终持有当前值，新订阅者立即获得当前值
- `SharedFlow`：广播语义，当前订阅者均可收到，是否回放由 `replay` 控制
- `Channel`：单播语义，一条消息只会被一个消费者取走

---

## 2. 快速选型

| 场景 | 推荐 | 原因 |
|---|---|---|
| 页面 UI 状态 | `MutableStateFlow` | 有当前值，重建后立即恢复 |
| Toast / 导航 / 弹窗 | `MutableSharedFlow(replay = 0, extraBufferCapacity = 1)` | 一次性广播，不回放旧事件 |
| 后来的订阅者也需要获取最近一次结果 | `MutableSharedFlow(replay = 1)` | 新订阅者可补收最近一条 |
| 一个命令只给一个消费者 | `Channel(Channel.BUFFERED)` | 单播语义明确 |
| 旧的 LiveData 单次事件 | 逐步迁移至 `SharedFlow` / `Channel` | 语义更清晰，可组合性更好 |

---

## 3. `SingleLiveEvent` 的 Flow 替代方案

目标语义：发送一次，当前存活的观察者接收一次，不在重建后重复触发。

### 广播型单次事件 → `SharedFlow`

适用条件：当前所有监听者均应收到，后来者不补发。

```kotlin
private val _event = MutableSharedFlow<UiEvent>(
    replay = 0,
    extraBufferCapacity = 1,
)
val event = _event.asSharedFlow()
```

### 严格单消费者事件 → `Channel`

适用条件：事件只允许一个消费者处理，不广播。

```kotlin
private val _event = Channel<UiEvent>(Channel.BUFFERED)
val event = _event.receiveAsFlow()
```

### `SingleLiveEvent` 的局限

- 基于 `LiveData`，与 Flow 体系不统一
- 多观察者时消费顺序不确定
- 缺乏 Flow 的组合、变换、合并能力

---

## 4. `MutableSharedFlow` 参数详解

```kotlin
MutableSharedFlow<T>(
    replay = 0,
    extraBufferCapacity = 0,
    onBufferOverflow = BufferOverflow.SUSPEND,
)
```

### 4.1 `replay`

新订阅者进入时，自动补发最近 N 条历史消息。

| 值 | 行为 | 适用场景 |
|---|---|---|
| `0` | 不回放 | Toast、导航、点击事件 |
| `1` | 回放最近 1 条 | 粘性事件、最近一次查询结果 |
| `>1` | 回放最近 N 条 | 极少见，慎用 |

### 4.2 `extraBufferCapacity`

为**当前**订阅者提供额外缓冲，防止慢消费者阻塞发送方。

注意：`extraBufferCapacity` 不影响新订阅者的历史回放，历史回放由 `replay` 控制。

| 值 | 含义 | 适用场景 |
|---|---|---|
| `0` | 无额外缓冲 | 默认 |
| `1` | 允许积压 1 条 | UI 一次性事件 |
| `>1` | 允许积压多条 | 高吞吐事件流 |

### 4.3 `onBufferOverflow`

缓冲区满时的处理策略，仅在 `replay + extraBufferCapacity > 0` 时生效。

| 策略 | 行为 |
|---|---|
| `SUSPEND` | 发送方挂起等待 |
| `DROP_OLDEST` | 丢弃最旧的一条 |
| `DROP_LATEST` | 丢弃当前这条 |

### 4.4 `emit` 与 `tryEmit`

| API | 行为 | 适用场景 |
|---|---|---|
| `emit` | 挂起，遵守背压规则 | 协程内，需保证发送成功 |
| `tryEmit` | 非挂起，返回 `Boolean` | UI 事件、回调桥接 |

### 4.5 常用配置

**配置 A：一次性 UI 事件**（Toast、导航、刷新完成）

```kotlin
private val _event = MutableSharedFlow<UiEvent>(
    replay = 0,
    extraBufferCapacity = 1,
)
```

**配置 B：粘性结果流**（新订阅者需要最近一次结果）

```kotlin
private val _result = MutableSharedFlow<Result>(
    replay = 1,
)
```

---

## 5. `Channel` 参数详解

```kotlin
private val _event = Channel<UiEvent>(Channel.BUFFERED)
val event = _event.receiveAsFlow()
```

### 5.1 `capacity`

| 写法 | 含义 |
|---|---|
| `Channel(0)` | 无缓冲，发送接收必须同步 |
| `Channel(n)` | 缓冲 n 条 |
| `Channel(Channel.BUFFERED)` | 系统默认缓冲（64） |
| `Channel(Channel.CONFLATED)` | 只保留最新值 |
| `Channel(Channel.UNLIMITED)` | 无限缓冲（谨慎使用） |

### 5.2 `send` 与 `trySend`

| API | 行为 |
|---|---|
| `send` | 可能挂起，等待缓冲区空闲 |
| `trySend` | 非挂起，立即返回 `ChannelResult` |

### 5.3 `receiveAsFlow()`

对外暴露时推荐转为 Flow，隐藏 Channel 实现细节：

```kotlin
val event = channel.receiveAsFlow()
```

### 5.4 适用场景

- 导航命令只允许一个页面消费
- 弹窗命令只允许一个宿主处理
- 需要串行排队处理的任务派发

### 5.5 注意事项

多个收集者会**竞争消费**，每条消息只被其中一个收到。若需要广播语义，应使用 `SharedFlow`。

---

## 6. `StateFlow`

`StateFlow` 是状态容器，不是事件流。

特点：
- 始终持有当前值
- 新订阅者立即获得当前值（等价于 `replay = 1` 且去重）
- 值相同时不触发更新（基于 `equals` 去重）

适用场景：
- 登录态
- 列表数据
- 页面加载状态（Loading / Success / Error）

---

## 7. 推荐模板

### 状态：`StateFlow`

```kotlin
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState = _uiState.asStateFlow()
```

### 广播型一次事件：`SharedFlow`

```kotlin
private val _event = MutableSharedFlow<UiEvent>(
    replay = 0,
    extraBufferCapacity = 1,
)
val event = _event.asSharedFlow()

fun showToast(message: String) {
    _event.tryEmit(UiEvent.Toast(message))
}
```

### 单消费者命令：`Channel`

```kotlin
private val _command = Channel<UiCommand>(Channel.BUFFERED)
val command = _command.receiveAsFlow()

fun navigateToDetail(id: Int) {
    _command.trySend(UiCommand.OpenDetail(id))
}
```

### Compose UI 收集

```kotlin
@Composable
fun MyScreen(vm: MyViewModel = koinViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.command.collect { cmd ->
            when (cmd) {
                is UiCommand.ShowToast -> Toast.makeText(context, cmd.message, Toast.LENGTH_SHORT).show()
                is UiCommand.OpenDetail -> { /* 导航 */ }
            }
        }
    }
}
```

**关键点**：
- `LaunchedEffect(Unit)`：只启动一次，不随重组重复订阅
- `Channel.BUFFERED`：UI 未就绪时事件不丢失
- ViewModel 销毁时 `viewModelScope` 取消，Channel 自动回收，无需手动 close

---

## 8. 常见误区

**误区 1：用 `StateFlow` 发送一次性事件**

`StateFlow` 保留当前值，页面重建后会重新收到，导致 Toast 重复弹出、导航重复触发。

**误区 2：混淆 `replay` 与 `extraBufferCapacity` 的作用**

- `replay`：控制新订阅者能否补收历史
- `extraBufferCapacity`：为当前订阅者提供积压缓冲

两者职责不同，`extraBufferCapacity` 不影响历史回放。

**误区 3：导航 / 弹窗事件使用 `replay = 1`**

页面重建后重新订阅，会再次收到上一次的导航事件，触发重复跳转。导航类事件应使用 `replay = 0`。

**误区 4：需要广播时使用 `Channel`**

`Channel` 是单播，多个收集者会竞争消费。广播场景应使用 `SharedFlow`。

**误区 5：无缓冲 `SharedFlow` 在主线程同步 `emit`**

`replay = 0, extraBufferCapacity = 0` 时，若收集端未就绪，`emit` 会挂起。UI 事件场景推荐加 `extraBufferCapacity = 1` 后使用 `tryEmit`。

**误区 6：收集 Flow 时不绑定生命周期**

选型正确但收集姿势不当，仍会造成后台泄漏或重复订阅。收集方式见：[flow-collect-best-practices.md](flow-collect-best-practices.md)

---

## 9. 选型原则

| 语义 | 类型 |
|---|---|
| 当前状态 | `StateFlow` |
| 广播型事件 | `SharedFlow` |
| 单消费者命令 | `Channel` |
| 旧代码单次事件 | `SingleLiveEvent`（维持，不扩散） |

决策起点：先判断这是**状态**、**事件**还是**命令**，再选型。