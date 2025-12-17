# DataStore 使用指南

项目使用 Proto DataStore 存储结构化数据（如 UserPreferences），Wire 生成 Protobuf 类。

## Compose 中读取 DataStore

```kotlin
// 使用 collectAsState() 订阅 Flow（持续观测）
val context = LocalContext.current
val userName by context.userPreferencesStore.data
    .map { it.name }
    .collectAsState(initial = "")  // initial 类型必须匹配 map 输出类型
```

## ViewModel 中使用 DataStore（推荐）

```kotlin
@KoinViewModel
class MyViewModel(
    private val dataStore: DataStore<UserPreferences>  // 通过 Koin 注入
) : ViewModel() {

    // 持续观测 - 使用 stateIn 转换为 StateFlow
    val userName: StateFlow<String> = dataStore.data
        .map { it.name }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),  // 5秒超时处理配置变更
            initialValue = ""
        )

    // 一次性读取 - 使用 first()
    suspend fun getUserNameOnce(): String = dataStore.data.map { it.name }.first()

    // 写入数据 - 必须在协程中
    fun updateName(name: String) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(name = name) }
        }
    }
}
```

## DataStore 注入配置

位置: di/DataModule.kt

```kotlin
@Singleton
fun provideUserPreferencesDataStore(context: Context): DataStore<UserPreferences> =
    context.userPreferencesStore
```

## Flow 操作对比

| 操作 | 行为 | 适用场景 |
|------|------|---------|
| `.stateIn()` / `.collectAsState()` | 持续观测，数据变化自动更新 | UI 实时显示 |
| `.first()` | 一次性获取当前值 | 初始化、条件判断 |
| `.firstOrNull()` | 一次性获取，空流返回 null | 安全读取 |

## 注意事项

- `StateFlow.collectAsState()` 不需要 initial（StateFlow 自带初始值）
- `Flow.collectAsState(initial = ...)` 必须提供 initial
- initial 类型必须与 Flow 泛型类型一致，否则会推断为公共父类型