# DataStore 使用指南

项目使用 **Proto DataStore + Wire** 存储结构化数据。Wire 将 `.proto` 文件编译为 Kotlin data class（不是 protobuf-java），所以修改字段用 `.copy()`，不用 `.toBuilder().build()`。

DataStore 定义在 **app 模块**，包含两个 Store：
- **UserPreferences** — `app/src/main/protos/UserPreferences.proto` + `app/.../datastore/UserPreferencesSerializer.kt`
- **AppSettings** — `app/src/main/protos/AppSettings.proto` + `app/.../datastore/AppSettingsSerializer.kt`

没有单独的 `module_datastore`。

## 初始化

DataStore 通过 Koin 注入（`DataModule.kt`），以 Context 扩展属性暴露：

```kotlin
val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_preferences.pb",
    serializer = UserPreferencesSerializer,
)

val Context.appSettingsStore: DataStore<AppSettings> by dataStore(
    fileName = "app_settings.pb",
    serializer = AppSettingsSerializer,
)
```

## ViewModel 中使用 DataStore

```kotlin
@KoinViewModel
class MyViewModel(
    private val dataStore: DataStore<UserPreferences>  // 通过 Koin 注入
) : ViewModel() {

    // 持续观测 - stateIn 转为 StateFlow，Compose 用 collectAsStateWithLifecycle()
    val userName: StateFlow<String> = dataStore.select { it.name }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // 一次性读取
    suspend fun loadInitialName(): String = dataStore.getLatest().name

    // 写入数据
    fun updateName(name: String) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(name = name) }
        }
    }
}
```

## DataStore 注入配置

位置: `app/.../di/DataModule.kt`

```kotlin
@Singleton
fun provideUserPreferencesDataStore(context: Context): DataStore<UserPreferences> =
    context.userPreferencesStore

@Singleton
fun provideAppSettingsDataStore(context: Context): DataStore<AppSettings> =
    context.appSettingsStore
```

## DataStore 扩展函数

位置：`app/.../DataStoreExtensions.kt`，对 `DataStore<T>` 提供三个语义化扩展：

```kotlin
// 一次性读取当前快照（挂起）
suspend fun <T> DataStore<T>.getLatest(): T = data.first()

// 更新并返回更新后的值（挂起）
suspend fun <T> DataStore<T>.updateAndGet(transform: (T) -> T): T {
    updateData(transform)
    return data.first()
}

// 订阅特定字段，仅在该字段变化时发射（避免无关字段更新触发重组）
fun <T, R> DataStore<T>.select(selector: (T) -> R): Flow<R> =
    data.map(selector).distinctUntilChanged()
```

**使用示例**：

```kotlin
// 一次性读取
val prefs = userPreferencesStore.getLatest()

// 更新并获取新值
val updated = userPreferencesStore.updateAndGet { it.copy(name = "Alice") }

// ViewModel 中订阅单个字段
val nameFlow: Flow<String> = userPreferencesStore.select { it.name }

// Compose
val name by viewModel.nameFlow.collectAsStateWithLifecycle("")
```

## Flow 操作对比

| 操作 | 行为 | 适用场景 |
|------|------|---------|
| `.stateIn()` / `.collectAsState()` | 持续观测，数据变化自动更新 | UI 实时显示 |
| `.select { field }` | 订阅单字段，减少无效发射 | 精确订阅避免重组 |
| `.getLatest()` | 一次性获取当前快照 | 初始化、条件判断 |
| `.updateAndGet { }` | 更新并立即返回新值 | 需要确认更新结果 |
| `.first()` | 原始挂起读取 | 底层使用 |

## 注意事项

- `StateFlow.collectAsState()` 不需要 initial（StateFlow 自带初始值）
- `Flow.collectAsState(initial = ...)` 必须提供 initial
- initial 类型必须与 Flow 泛型类型一致，否则会推断为公共父类型
