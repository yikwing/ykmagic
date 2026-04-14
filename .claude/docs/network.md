# 网络请求指南

> 基于 Ktor + Koin + kotlinx.serialization 的网络层方案

## 设计原则: `suspend` vs `Flow`

**核心原则**: 数据层保持 `suspend` 函数，ViewModel 层决定如何处理状态流。

**记忆口诀**: **动词用 `suspend`，名词用 `Flow`**

| 特性 | suspend | Flow |
|------|---------|------|
| 数据性质 | 静态、一次性 | 动态、持续性 |
| 结果数量 | 单个 | 多个 |
| 生命周期 | 执行后结束 | 取消前持续 |
| 典型场景 | 发布、删除、点赞、上传 | 动态列表、实时通知 |

## 依赖配置

网络依赖通过 `libs.versions.toml` 的 `network-ktor` bundle 管理，构建配置详见 [build-logic.md](build-logic.md)。序列化需在模块 `build.gradle.kts` 中应用 `kotlin.serialization` 插件。

## API 类定义

用 `@Singleton` 注解，Koin Annotations 自动将其注入 Repository：

```kotlin
// 默认 BaseUrl（来自 DefaultRequest 配置）
@Singleton
class WanAndroidApi(
    private val httpClient: HttpClient,
) {
    suspend fun getChapters(): BaseHttpResult<List<ChapterBean>> =
        httpClient.get { url("wxarticle/chapters/json") }.body()
}

// 覆盖 BaseUrl（takeFrom 覆盖 DefaultRequest 设置的 baseUrl）
@Singleton
class HttpBinApi(
    private val httpClient: HttpClient,
) {
    suspend fun getHeaders(): HttpBinHeaders =
        httpClient.get {
            url {
                takeFrom("https://httpbin.org")
                appendPathSegments("get")
                parameters.append("token", "abc123")
            }
        }.body()

    suspend fun postData(): HttpBinPostResult =
        httpClient.post {
            url {
                takeFrom("https://httpbin.org")
                appendPathSegments("post")
            }
            setBody(buildJsonObject { put("key", "value") })
        }.body()
}
```

## 数据模型

```kotlin
@Serializable
data class User(
    val id: Int,
    val name: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

// 同时支持 Parcelize
@Serializable
@Parcelize
data class HttpBinHeaders(
    val headers: Headers,
) : Parcelable
```

## 请求封装

### `requestStateFlow` - UI 交互场景

`RequestState<T>` 是 `sealed interface`，三个子类的字段：
- `RequestState.Success` → `value: T`
- `RequestState.Error` → `throwable: ApiException`（含 `code`、`message`）
- `RequestState.Loading` — 无字段

```kotlin
@KoinViewModel
class ChapterViewModel(private val api: WanAndroidApi) : ViewModel() {
    private val _state = MutableStateFlow<RequestState<List<ChapterBean>?>>(RequestState.Loading)
    val state: StateFlow<RequestState<List<ChapterBean>?>> = _state

    fun fetch() = viewModelScope.launch {
        requestStateFlow { api.getChapters() }.collect { _state.value = it }
    }
}

// Compose UI（用 collectAsStateWithLifecycle 替代 collectAsState）
val state by viewModel.state.collectAsStateWithLifecycle()
when (state) {
    is RequestState.Loading -> CircularProgressIndicator()
    is RequestState.Success -> ChapterList(state.value)      // .value
    is RequestState.Error -> ErrorView(state.throwable.message) // .throwable
}
```

> View 体系可用 `collectState` DSL（`onLoading` / `onSuccess` / `onFailure`）替代 `when` 块，需配合 `repeatOnLifecycle(STARTED)`。Compose 中直接用 `collectAsStateWithLifecycle()` + `when`。

### `requestResult` - 后台操作

```kotlin
// 适合上传日志、文件上传等一次性操作
requestResult { api.postData() }
    .onSuccess { Log.d("Upload", "成功: $it") }
    .onFailure { Log.e("Upload", "失败: ${it.message}") }

// 或使用 fold
val result = requestResult { api.getHeaders() }.fold(
    onSuccess = { "Host: ${it.headers.host}" },
    onFailure = { "失败: ${it.message}" }
)
```

| 场景 | 推荐 | 原因 |
|------|------|------|
| 列表/详情加载 | `requestStateFlow` | 需要 Loading 状态 |
| 上传/提交/同步 | `requestResult` | 一次性后台操作 |

### `ApiConfig` - 全局错误码策略

默认 `errorCode != 0` 表示失败。如果业务 API 使用不同的成功码，在 Application 初始化时覆盖：

```kotlin
// 默认行为（errorCode != 0 视为失败）
ApiConfig.errorCodeChecker = { it != 0 }

// 示例：某些 API 用 200 表示成功
ApiConfig.errorCodeChecker = { it != 200 }

// 示例：0 和 1 都表示成功
ApiConfig.errorCodeChecker = { it !in setOf(0, 1) }
```

## Flow 收集约定

| 场景 | 方式 |
|------|------|
| Compose | `collectAsStateWithLifecycle()`（内部使用 `STARTED`） |
| Fragment | `repeatOnLifecycle(Lifecycle.State.STARTED)` |

默认使用 `STARTED`（页面可见即收集）；仅在需要独占资源（相机、麦克风）时使用 `RESUMED`。

## 网络层 DI

HttpClient 通过 Koin 注入（插件：HttpTimeout / ContentNegotiation / DefaultRequest / HttpRequestRetry / Logging），完整配置参见 [dependency-injection.md](dependency-injection.md) 的"网络层 DI"章节。

## 注意事项

1. **HttpClient 复用**: 单例使用，不要每次请求都创建
2. **线程安全**: HttpClient 是线程安全的
3. **序列化配置**: 必须设置 `ignoreUnknownKeys = true`
