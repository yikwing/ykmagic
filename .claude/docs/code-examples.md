# 代码示例

项目常用代码模式的完整示例。

---

## 网络请求

### UI 交互（requestStateFlow）

```kotlin
// ViewModel
@KoinViewModel
class UserViewModel(private val api: ApiService) : ViewModel() {
    private val _state = MutableStateFlow<RequestState<User>>(RequestState.Loading)
    val state = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            requestStateFlow { api.getUserInfo(userId) }
                .collect { _state.value = it }
        }
    }
}

// Composable
@Composable
fun UserScreen(vm: UserViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    when (state) {
        is RequestState.Loading -> CircularProgressIndicator()
        is RequestState.Success -> UserContent(state.value)
        is RequestState.Error -> ErrorMessage(state.throwable.message)
    }
}
```

### 后台操作（requestResult）

```kotlin
// Repository
class UserRepository(private val api: ApiService) {
    suspend fun sync(userId: String): Result<User?> {
        return requestResult { api.getUserInfo(userId) }
    }
}

// ViewModel 使用
@KoinViewModel
class SyncViewModel(private val repo: UserRepository) : ViewModel() {
    fun sync(userId: String) {
        viewModelScope.launch {
            repo.sync(userId)
                .onSuccess { user -> /* 处理成功 */ }
                .onFailure { error -> /* 处理失败 */ }
        }
    }
}

// Initializer 使用（在 AppInitializer 链中执行的任务）
class DataSyncTask(private val repo: UserRepository) : Initializer<Unit> {
    override fun create(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.sync("default_user")
        }
    }
    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(ConfigInjectInitTask::class.java)
}
```

### 定义 API

```kotlin
interface ApiService {
    suspend fun getUserInfo(userId: String): HttpResponse
    suspend fun createUser(user: User): HttpResponse
    suspend fun updateUser(userId: String, user: User): HttpResponse
    suspend fun deleteUser(userId: String): HttpResponse
}

class ApiServiceImpl(private val client: HttpClient) : ApiService {
    override suspend fun getUserInfo(userId: String): HttpResponse {
        return client.get("users/$userId")
    }

    override suspend fun createUser(user: User): HttpResponse {
        return client.post("users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }
    }
}
```

---

## 依赖注入

### 基础注入

```kotlin
// Repository
class UserRepository(private val api: ApiService) {
    suspend fun getUser(id: String) = requestResult { api.getUserInfo(id) }
}

// ViewModel
@KoinViewModel
class SimpleViewModel(private val repo: UserRepository) : ViewModel()

// Composable
@Composable
fun SimpleScreen() {
    val vm: SimpleViewModel = koinViewModel()
}
```

### 带参数注入

```kotlin
// ViewModel
@KoinViewModel
class DetailViewModel(
    @InjectedParam private val userId: String,
    private val repo: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow<RequestState<User>>(RequestState.Loading)
    val state = _state.asStateFlow()

    init { loadUser() }

    private fun loadUser() {
        viewModelScope.launch {
            requestStateFlow { repo.getUser(userId) }
                .collect { _state.value = it }
        }
    }
}

// Composable
@Composable
fun DetailScreen(userId: String) {
    val vm: DetailViewModel = koinViewModel { parametersOf(userId) }
    val state by vm.state.collectAsStateWithLifecycle()
}

// Navigation 3.x (类型安全)
@Serializable
data object ListScreen : NavKey

@Serializable
data class DetailScreen(val userId: String) : NavKey

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(ListScreen)

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<ListScreen> {
                ListScreen(
                    onItemClick = { userId ->
                        backStack.navigate(DetailScreen(userId))
                    }
                )
            }
            entry<DetailScreen> { detail ->
                DetailScreen(userId = detail.userId)
            }
        }
    )
}
```

### Repository 注入

```kotlin
// Repository
class UserRepository(
    private val api: ApiService,
    private val dataStore: DataStore<UserSettings>
) {
    suspend fun getUser(id: String) = requestResult { api.getUserInfo(id) }
    suspend fun saveSettings(settings: UserSettings) {
        dataStore.updateData { settings }
    }
}

// Koin 模块
@Module
@ComponentScan("com.yikwing.repository")
class RepositoryModule

// ViewModel 自动注入
@KoinViewModel
class MyViewModel(private val repo: UserRepository) : ViewModel()
```

---

## 配置管理

```kotlin
// 读取配置
val baseUrl = YkConfigManager.config.baseUrl
val apiKey = YkConfigManager.config.apiKey
val timeout = YkConfigManager.config.timeout

// ViewModel 中使用
@KoinViewModel
class ConfigViewModel : ViewModel() {
    val config = YkConfigManager.config

    fun printConfig() {
        Log.d("Config", "Base URL: ${config.baseUrl}")
        Log.d("Config", "API Key: ${config.apiKey}")
    }
}
```

**配置文件** (`android_env.json`):
```json
{
  "base_url": "https://api.example.com",
  "api_key": "your_api_key",
  "timeout": 30000,
  "enable_log": true,
  "environment": "production"
}
```

**配置流程**: `android_env.json` → BuildConfig.YK_CONFIG → YkConfigManager.config

---

## DataStore 操作

### Proto 文件（Wire 风格）

```protobuf
// app/src/main/protos/UserPreferences.proto
syntax = "proto3";
option java_package = "com.yikwing.ykquickdev";
option java_multiple_files = true;

message UserPreferences {
    string name = 1;
    int32 age = 2;
    bool is_male = 3;
}
```

> Wire 插件将 `.proto` 文件编译为 Kotlin data class，使用 `.copy()` 修改字段（不是 protobuf-java 的 `.toBuilder().build()`）。

### 使用 DataStore

```kotlin
// Serializer（Wire 风格）
object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences =
        UserPreferences.ADAPTER.decode(input.source().buffer())

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) =
        t.adapter.encode(output.sink().buffer(), t)
}

val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_preferences.pb",
    serializer = UserPreferencesSerializer,
)

// DataStore 操作（Wire 使用 .copy()）
dataStore.updateData { current ->
    current.copy(name = "Alice", age = 25)
}
```

// ViewModel
@KoinViewModel
class SettingsViewModel(
    private val dataStore: DataStore<UserPreferences>  // Koin 注入
) : ViewModel() {
    val name: StateFlow<String> = dataStore.data
        .map { it.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateName(name: String) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(name = name) }
        }
    }
}

// Composable
@Composable
fun SettingsScreen(vm: SettingsViewModel = koinViewModel()) {
    val name by vm.name.collectAsStateWithLifecycle()
    Text(name)
}
```

---

## ViewModel 状态管理

### Explicit Backing Fields

```kotlin
@KoinViewModel
class MyViewModel : ViewModel() {
    // 标准方式（推荐）
    private val _state = MutableStateFlow("initial")
    val state: StateFlow<String> = _state.asStateFlow()

    fun update(value: String) {
        _state.value = value  // 内部可修改
    }
}

// Composable
@Composable
fun MyScreen(vm: MyViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    // state 只读，无法修改
}
```

**注意**: Kotlin 2.3.0+ 支持 Explicit Backing Fields 语法（`val state = field: MutableStateFlow(...)`），但项目当前使用标准方式。

### 复杂状态管理

```kotlin
// UI 状态
data class UserUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@KoinViewModel
class UserViewModel(private val repo: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repo.getUser(userId)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = null,
                        error = error.message
                    )
                }
        }
    }
}
```

---

## 模块初始化

### 实现 Initializer

```kotlin
// 实现 Initializer<T> 接口（在 module_proxy 中定义）
class MyInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        ThirdPartySDK.init(context)
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(ConfigInjectInitTask::class.java)
}

// Application 注册
@KoinApplication
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<MainApplication> {
            androidContext(this@MainApplication)
        }
        AppInitializer
            .getInstance(this)
            .addTask(ConfigInjectInitTask())
            .addTask(MyInitTask())
            .build(debug = true)
    }
}
```

### 依赖链

```kotlin
// 无依赖
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

---

## Activity 生命周期管理

```kotlin
// 获取栈顶 Activity
val topActivity = ActivityHierarchyManager.getTopActivity()
topActivity?.let {
    Toast.makeText(it, "Hello", Toast.LENGTH_SHORT).show()
}

// 关闭操作
ActivityHierarchyManager.finishTopActivities(1)  // 关闭栈顶
ActivityHierarchyManager.finishTopActivities(3)  // 关闭栈顶 3 个
ActivityHierarchyManager.finishAllExcept(MainActivity::class.java)  // 关闭除指定外的所有
ActivityHierarchyManager.finishUntil(LoginActivity::class.java, inclusive = false)  // 关闭到指定（不含）
ActivityHierarchyManager.finishUntil(LoginActivity::class.java, inclusive = true)   // 关闭到指定（含）

// 获取所有 Activity
val allActivities = ActivityHierarchyManager.getAllActivities()
Log.d("Activities", "Total: ${allActivities.size}")
```

### ViewModel 中使用

```kotlin
@KoinViewModel
class LogoutViewModel : ViewModel() {
    fun logout() {
        viewModelScope.launch {
            clearUserData()

            // 关闭除登录页外的所有页面
            ActivityHierarchyManager.finishAllExcept(LoginActivity::class.java)

            // 跳转到登录页
            val topActivity = ActivityHierarchyManager.getTopActivity()
            if (topActivity !is LoginActivity) {
                val intent = Intent(topActivity, LoginActivity::class.java)
                topActivity?.startActivity(intent)
            }
        }
    }
}
```

---

## 更多参考

- [network.md](network.md) - 网络请求详解
- [dependency-injection.md](dependency-injection.md) - 依赖注入详解
- [patterns.md](patterns.md) - 开发模式和技巧
- [testing.md](testing.md) - 测试示例
