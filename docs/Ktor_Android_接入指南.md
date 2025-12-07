# Ktor Android 接入指南

> 基于项目实践整理的 Ktor + Hilt + kotlinx.serialization 网络层方案

## 一、技术选型

### 1.1 Ktor vs Retrofit

| 特性 | Ktor | Retrofit |
|------|------|----------|
| **语言** | Kotlin-first，原生协程 | Java 设计，Kotlin 适配 |
| **跨平台** | 支持 KMP (iOS/Android/Desktop/Web) | 仅 Android/JVM |
| **配置方式** | DSL 配置，灵活 | 注解驱动，声明式 |
| **依赖** | 轻量，按需引入 | 依赖 OkHttp |

### 1.2 核心优势

- **Kotlin 原生协程** - 无需 Call Adapter
- **跨平台能力** - 一套代码，多端复用
- **插件化架构** - 按需安装功能模块
- **DSL 配置** - 代码即配置，易读易维护

## 二、依赖配置

### 2.1 Version Catalog (libs.versions.toml)

```toml
[versions]
ktor = "3.3.3"
kotlinx-serialization = "1.7.3"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

[bundles]
network-ktor = [
    "ktor-client-core",
    "ktor-client-okhttp",
    "ktor-client-content-negotiation",
    "ktor-client-logging",
    "ktor-serialization-kotlinx-json"
]

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### 2.2 build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.bundles.network.ktor)
    implementation(libs.kotlinx.serialization.json)
}
```

## 三、Hilt 依赖注入配置

### 3.1 Qualifier 定义 (module_network)

```kotlin
package com.yikwing.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationInterceptors

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkInterceptors
```

### 3.2 NetworkModule (module_network)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TIMEOUT_MS = 30_000L
    private const val CONNECT_TIMEOUT_MS = 15_000L

    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationInterceptors applicationInterceptors: List<@JvmSuppressWildcards Interceptor>,
        @NetworkInterceptors networkInterceptors: List<@JvmSuppressWildcards Interceptor>,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .apply {
                addInterceptor(OkLogInterceptor())
                applicationInterceptors.forEach { addInterceptor(it) }
                addInterceptor(RetryInterceptor())
                networkInterceptors.forEach { addNetworkInterceptor(it) }
                callTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            }.build()

    @Singleton
    @Provides
    fun provideJson(): Json =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

    @Singleton
    @Provides
    fun provideHttpClient(
        okHttpClient: OkHttpClient,
        json: Json,
        @BaseUrl baseUrl: String,
    ): HttpClient =
        HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            install(ContentNegotiation) { json(json) }
            install(DefaultRequest) {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
}
```

### 3.3 AppNetworkModule (app 层)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    @Singleton
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String =
        YkConfigManager.getConfig(NetworkConfig::class.java).baseUrl

    @Singleton
    @Provides
    @ApplicationInterceptors
    fun provideApplicationInterceptors(application: Application): List<Interceptor> =
        listOf(
            ChuckerInterceptor(application),
            HeaderInterceptor(),
        )

    @Singleton
    @Provides
    @NetworkInterceptors
    fun provideNetworkInterceptors(): List<Interceptor> = emptyList()
}
```

## 四、数据模型定义

### 4.1 使用 kotlinx.serialization

```kotlin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)
```

### 4.2 Moshi 迁移对照

| Moshi | kotlinx.serialization |
|-------|----------------------|
| `@JsonClass(generateAdapter = true)` | `@Serializable` |
| `@Json(name = "xxx")` | `@SerialName("xxx")` |

## 五、API 服务实现

### 5.1 基础用法

```kotlin
class Repo @Inject constructor(
    private val httpClient: HttpClient,
) {
    // 使用默认 BaseUrl
    suspend fun getChapters(): BaseHttpResult<List<ChapterBean>> =
        httpClient.get { url("wxarticle/chapters/json") }.body()

    // 覆盖 BaseUrl
    suspend fun binGet(): HttpBinHeaders =
        httpClient.get {
            url {
                takeFrom("https://httpbin.org")
                appendPathSegments("get")
            }
        }.body()

    // 带查询参数
    suspend fun search(keyword: String): ApiResponse<List<User>> =
        httpClient.get {
            url("search") {
                parameters.append("q", keyword)
                parameters.append("page", "1")
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

### 5.2 请求构建模式

```kotlin
// 1. 使用默认 BaseUrl
httpClient.get { url("path/to/endpoint") }

// 2. 覆盖 BaseUrl
httpClient.get {
    url {
        takeFrom("https://other-api.com")
        appendPathSegments("v1", "users")
    }
}

// 3. 添加 Header
httpClient.post {
    url("posts")
    bearerAuth(token)
    header("X-Request-Id", UUID.randomUUID().toString())
    setBody(request)
}
```

## 六、Repository + Flow 封装

### 6.1 RequestState 定义

```kotlin
sealed interface RequestState<out T> {
    data object Loading : RequestState<Nothing>
    data class Success<T>(val data: T) : RequestState<T>
    data class Error(val error: ApiException) : RequestState<Nothing>
}
```

### 6.2 Repository 实现

```kotlin
class OtherRepository @Inject constructor(
    private val repo: Repo,
) {
    fun initHttpBinData(): Flow<RequestState<Headers>> =
        flow {
            emit(RequestState.Loading)
            val data = repo.binGet().headers
            emit(RequestState.Success(data))
        }.flowOn(Dispatchers.IO)
         .catch { exception ->
            if (exception is CancellationException) throw exception
            val apiException = exception as? ApiException
                ?: ApiException.createDefault(exception.message, exception)
            emit(RequestState.Error(apiException))
        }
}
```

**注意**: `CancellationException` 必须重新抛出，避免破坏协程取消机制。

## 七、ViewModel + Compose 使用

### 7.1 ViewModel

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<RequestState<User>>(RequestState.Loading)
    val userState: StateFlow<RequestState<User>> = _userState.asStateFlow()

    fun fetchUser(userId: Int) {
        viewModelScope.launch {
            repository.getUser(userId).collect { state ->
                _userState.value = state
            }
        }
    }
}
```

### 7.2 Compose UI

```kotlin
@Composable
fun UserScreen(viewModel: UserViewModel = hiltViewModel()) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchUser(1)
    }

    when (val state = userState) {
        is RequestState.Loading -> CircularProgressIndicator()
        is RequestState.Success -> UserContent(user = state.data)
        is RequestState.Error -> ErrorContent(
            error = state.error,
            onRetry = { viewModel.fetchUser(1) }
        )
    }
}
```

## 八、高级配置

### 8.1 共享 OkHttpClient (与 Retrofit 共存)

```kotlin
val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

// Ktor 使用共享的 OkHttpClient
val ktorClient: HttpClient = HttpClient(OkHttp) {
    engine { preconfigured = okHttpClient }
    install(ContentNegotiation) { json() }
}

// Retrofit 使用共享的 OkHttpClient
val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient)
    .build()
```

### 8.2 Bearer Token 认证

```kotlin
install(Auth) {
    bearer {
        loadTokens {
            BearerTokens(
                accessToken = tokenStorage.getAccessToken(),
                refreshToken = tokenStorage.getRefreshToken()
            )
        }
        refreshTokens {
            val newTokens = refreshTokenApi()
            BearerTokens(newTokens.access, newTokens.refresh)
        }
    }
}
```

### 8.3 非注入环境获取 HttpClient

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NetworkEntryPoint {
    fun httpClient(): HttpClient
}

// 使用
val entryPoint = EntryPointAccessors.fromApplication(context, NetworkEntryPoint::class.java)
val httpClient = entryPoint.httpClient()
```

## 九、注意事项

### 9.1 性能优化

```kotlin
// 正确：HttpClient 应该复用
object ApiClient {
    val client = HttpClient(OkHttp) { ... }
}

// 错误：每次请求都创建
suspend fun getUser(): User {
    val client = HttpClient(OkHttp) { ... }
    return client.get("...").body()
}
```

### 9.2 线程安全

- `HttpClient` 是线程安全的，可以在多协程中使用
- 使用 `flowOn(Dispatchers.IO)` 执行网络请求
- 在 ViewModel 中使用 `viewModelScope` 管理协程生命周期

### 9.3 序列化注意事项

```kotlin
// 必须配置：忽略未知字段
Json { ignoreUnknownKeys = true }

// 字段名映射
@Serializable
data class User(
    @SerialName("user_name") val userName: String
)

// 可空字段处理
@Serializable
data class User(
    val name: String,
    val email: String? = null  // 可空，有默认值
)
```

## 十、常见问题

### Q1: 如何处理文件下载？

```kotlin
suspend fun downloadFile(url: String, file: File) {
    client.prepareGet(url).execute { response ->
        val channel = response.bodyAsChannel()
        file.writeChannel().use { output ->
            channel.copyTo(output)
        }
    }
}
```

### Q2: 如何取消请求？

```kotlin
val job = viewModelScope.launch {
    client.get("...").body()
}
job.cancel()
```

### Q3: 如何添加全局 Header？

```kotlin
install(DefaultRequest) {
    header("X-Api-Key", "your-api-key")
    header("Accept-Language", "zh-CN")
}
```

---

## 参考资源

- [Ktor 官方文档](https://ktor.io/docs/welcome.html)
- [kotlinx.serialization 文档](https://kotlinlang.org/docs/serialization.html)
- [Hilt 依赖注入指南](https://developer.android.com/training/dependency-injection/hilt-android)