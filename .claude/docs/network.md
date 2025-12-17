# 网络请求指南

> 基于 Ktor + Koin + kotlinx.serialization 的网络层方案

## 设计原则: `suspend` vs `Flow`

**核心原则**: 数据层保持 `suspend` 函数,ViewModel 层决定如何处理状态流。

**记忆口诀**: **动词用 `suspend`,名词用 `Flow`**

| 特性 | suspend | Flow |
|------|---------|------|
| 数据性质 | 静态、一次性 | 动态、持续性 |
| 结果数量 | 单个 | 多个 |
| 生命周期 | 执行后结束 | 取消前持续 |
| 典型场景 | 发布、删除、点赞、上传 | 动态列表、实时通知 |

## 依赖配置

### libs.versions.toml

```toml
[versions]
ktor = "3.3.3"
kotlinx-serialization = "1.7.3"

[bundles]
network-ktor = [
    "ktor-client-core",
    "ktor-client-okhttp",
    "ktor-client-content-negotiation",
    "ktor-client-logging",
    "ktor-serialization-kotlinx-json"
]
```

### build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.bundles.network.ktor)
    implementation(libs.kotlinx.serialization.json)
}
```

## Repository 定义

```kotlin
class Repo @Inject constructor(
    private val httpClient: HttpClient,
) {
    // GET 请求 - 使用默认 BaseUrl
    suspend fun getChapters(): BaseHttpResult<List<ChapterBean>> =
        httpClient.get { url("wxarticle/chapters/json") }.body()

    // GET 请求 - 覆盖 BaseUrl
    suspend fun binGet(): HttpBinHeaders =
        httpClient.get {
            url {
                takeFrom("https://httpbin.org")
                appendPathSegments("get")
                parameters.append("token", "abc123")
            }
        }.body()

    // POST 请求
    suspend fun createUser(user: User): ApiResponse<User> =
        httpClient.post {
            url("users")
            setBody(user)
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

**Moshi 迁移**: `@JsonClass` → `@Serializable`, `@Json` → `@SerialName`

## 请求封装

### `requestStateFlow` - UI 交互场景

```kotlin
class ChapterViewModel(private val repo: Repo) : ViewModel() {
    private val _state = MutableStateFlow<RequestState<List<ChapterBean>?>>(RequestState.Loading)
    val state: StateFlow<RequestState<List<ChapterBean>?>> = _state

    fun fetch() = viewModelScope.launch {
        requestStateFlow { repo.getChapters() }.collect { _state.value = it }
    }
}

// Compose UI
when (val state = viewModel.state.collectAsState().value) {
    is RequestState.Loading -> CircularProgressIndicator()
    is RequestState.Success -> ChapterList(state.data)
    is RequestState.Error -> ErrorView(state.error)
}
```

### `requestResult` - 后台操作

```kotlin
// 适合上传日志、文件上传等一次性操作
requestResult { repo.binPost() }
    .onSuccess { Log.d("Upload", "成功: $it") }
    .onFailure { Log.e("Upload", "失败: ${it.message}") }

// 或使用 fold
val result = requestResult { repo.binGet() }.fold(
    onSuccess = { "Host: ${it.headers.host}" },
    onFailure = { "失败: ${it.message}" }
)
```

| 场景 | 推荐 | 原因 |
|------|------|------|
| 列表/详情加载 | `requestStateFlow` | 需要 Loading 状态 |
| 上传/提交/同步 | `requestResult` | 一次性后台操作 |

## Koin 依赖注入

### NetworkModule

```kotlin
@Module
object NetworkModule {
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Singleton
    fun provideHttpClient(
        okHttpClient: OkHttpClient,
        json: Json,
        @BaseUrl baseUrl: String,
    ): HttpClient = HttpClient(OkHttp) {
        engine { preconfigured = okHttpClient }
        install(ContentNegotiation) { json(json) }
        install(DefaultRequest) {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }
}
```

## 高级用法

### Bearer Token 认证

```kotlin
install(Auth) {
    bearer {
        loadTokens { BearerTokens(accessToken, refreshToken) }
        refreshTokens { BearerTokens(newAccess, newRefresh) }
    }
}
```

### 文件下载

```kotlin
suspend fun downloadFile(url: String, file: File) {
    client.prepareGet(url).execute { response ->
        response.bodyAsChannel().copyTo(file.writeChannel())
    }
}
```

### 全局 Header

```kotlin
install(DefaultRequest) {
    header("X-Api-Key", "your-api-key")
    header("Accept-Language", "zh-CN")
}
```

## 注意事项

1. **HttpClient 复用** - 单例使用，不要每次请求都创建
2. **线程安全** - HttpClient 是线程安全的
3. **序列化配置** - 必须设置 `ignoreUnknownKeys = true`
4. **调试** - Debug 版本集成 Chucker 可视化抓包