# 代码示例

项目常用代码模式的完整示例。

---

## 网络请求

### UI 交互（requestStateFlow）

```kotlin
// ViewModel
@KoinViewModel
class UserViewModel(private val api: ApiService) : ViewModel() {
    val state = field: MutableStateFlow<RequestState<User>>(RequestState.Idle)

    fun load(userId: String) {
        viewModelScope.launch {
            api.getUserInfo(userId)
                .requestStateFlow()
                .collect { state.value = it }
        }
    }
}

// Composable
@Composable
fun UserScreen(vm: UserViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    when (state) {
        is RequestState.Idle -> Text("初始状态")
        is RequestState.Loading -> CircularProgressIndicator()
        is RequestState.Success -> UserContent(state.data)
        is RequestState.Error -> ErrorMessage(state.message)
    }
}
```

### 后台操作（requestResult）

```kotlin
// Repository
class UserRepository(private val api: ApiService) {
    suspend fun sync(userId: String): Result<User?> {
        return api.getUserInfo(userId).requestResult()
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

// InitTask 使用
class DataSyncTask(private val repo: UserRepository) : InitTask {
    override fun process(app: Application) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.sync("default_user")
        }
    }
    override fun dependencies() = listOf(LoggerInitTask::class.java)
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
    suspend fun getUser(id: String) = api.getUserInfo(id).requestResult()
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
    val state = field: MutableStateFlow<RequestState<User>>(RequestState.Idle)

    init { loadUser() }

    private fun loadUser() {
        viewModelScope.launch {
            repo.getUser(userId)
                .requestStateFlow()
                .collect { state.value = it }
        }
    }
}

// Composable
@Composable
fun DetailScreen(userId: String) {
    val vm: DetailViewModel = koinViewModel { parametersOf(userId) }
    val state by vm.state.collectAsStateWithLifecycle()
}

// Navigation
@Composable
fun AppNavigation() {
    NavHost(navController = rememberNavController(), startDestination = "list") {
        composable("list") { ListScreen() }
        composable(
            route = "detail/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            DetailScreen(userId)
        }
    }
}
```

### Repository 注入

```kotlin
// Repository
class UserRepository(
    private val api: ApiService,
    private val dataStore: DataStore<UserSettings>
) {
    suspend fun getUser(id: String) = api.getUserInfo(id).requestResult()
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

### Proto 文件

```protobuf
// src/main/proto/user_settings.proto
syntax = "proto3";
option java_package = "com.yikwing.datastore";
option java_multiple_files = true;

message UserSettings {
  bool dark_mode = 1;
  string language = 2;
  int32 font_size = 3;
  bool notifications_enabled = 4;
}
```

### 使用 DataStore

```kotlin
// DataStore 管理类
class UserPreferences(private val dataStore: DataStore<UserSettings>) {
    val settings: Flow<UserSettings> = dataStore.data

    suspend fun updateTheme(isDark: Boolean) {
        dataStore.updateData { it.toBuilder().setDarkMode(isDark).build() }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.updateData { it.toBuilder().setLanguage(language).build() }
    }

    suspend fun updateSettings(
        darkMode: Boolean? = null,
        language: String? = null,
        fontSize: Int? = null
    ) {
        dataStore.updateData { settings ->
            val builder = settings.toBuilder()
            darkMode?.let { builder.setDarkMode(it) }
            language?.let { builder.setLanguage(it) }
            fontSize?.let { builder.setFontSize(it) }
            builder.build()
        }
    }
}

// ViewModel
@KoinViewModel
class SettingsViewModel(private val prefs: UserPreferences) : ViewModel() {
    val settings = prefs.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings.getDefaultInstance()
    )

    fun toggleDarkMode() {
        viewModelScope.launch {
            prefs.updateTheme(!settings.value.darkMode)
        }
    }
}

// Composable
@Composable
fun SettingsScreen(vm: SettingsViewModel = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    Switch(
        checked = settings.darkMode,
        onCheckedChange = { vm.toggleDarkMode() }
    )
}
```

---

## ViewModel 状态管理

### Explicit Backing Fields

```kotlin
@KoinViewModel
class MyViewModel : ViewModel() {
    // 旧方式（仍有效）
    private val _oldState = MutableStateFlow("initial")
    val oldState: StateFlow<String> = _oldState.asStateFlow()

    // 新方式（推荐）- Kotlin 2.3.0+
    val newState = field: MutableStateFlow("initial")
    // 编译器自动生成 backing field，外部只读

    fun update(value: String) {
        newState.value = value  // 内部可修改
    }
}

// Composable
@Composable
fun MyScreen(vm: MyViewModel = koinViewModel()) {
    val state by vm.newState.collectAsStateWithLifecycle()
    // state 只读，无法修改
}
```

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
    val uiState = field: MutableStateFlow(UserUiState())

    fun loadUser(userId: String) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)

            repo.getUser(userId)
                .onSuccess { user ->
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
                .onFailure { error ->
                    uiState.value = uiState.value.copy(
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

### 实现 InitTask

```kotlin
// 基础初始化任务
class MyInitTask : InitTask {
    override fun dependencies() = listOf(LoggerInitTask::class.java)

    override fun process(app: Application) {
        Log.d("Init", "MyInitTask started")
        ThirdPartySDK.init(app)
        Log.d("Init", "MyInitTask completed")
    }
}

// Application 注册
@KoinApplication
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(/* 模块列表 */)
        }

        AppInitializer.init(
            this,
            LoggerInitTask(),
            MyInitTask(),
            NetworkInitTask()
        )
    }
}
```

### 依赖链

```kotlin
// 无依赖
class LoggerInitTask : InitTask {
    override fun dependencies() = emptyList()
    override fun process(app: Application) {
        Logger.init(app)
    }
}

// 依赖日志
class NetworkInitTask : InitTask {
    override fun dependencies() = listOf(LoggerInitTask::class.java)
    override fun process(app: Application) {
        NetworkManager.init(app)
    }
}

// 依赖日志和网络
class DatabaseInitTask : InitTask {
    override fun dependencies() = listOf(
        LoggerInitTask::class.java,
        NetworkInitTask::class.java
    )
    override fun process(app: Application) {
        DatabaseManager.init(app)
    }
}

// 执行顺序: LoggerInitTask → NetworkInitTask → DatabaseInitTask
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
