# 测试指南

## 项目测试约定

- **测试框架**: JUnit 4(`@Test` / `@Before` / `@After`)
- **Mock 工具**: MockK(Kotlin 生态首选)
- **断言库**: Hamcrest(`assertThat` + matchers)
- **测试文件命名**: `<ClassName>Test`,放在与源码对应的 test 包下
- **测试方法命名**: Kotlin 反引号 + 英文自然语言,形如 `` `should do X when Y` ``,与项目现有风格一致
- **分组**: 类内用 `// ==================== 分组名 ====================` 分节(见 `CollectionExtensionsTest.kt`)
- **不用**: Robolectric、Turbine、`runTest` + `TestDispatcher`、`Dispatchers.setMain`(保持 `testBundle` 精简,所有协程/Flow 都用 `runBlocking` 直接测)

## 测试依赖

Convention Plugin 自动添加测试依赖(`testBundle` + `androidTestBundle`):

| Bundle | 包含 | 用途 |
|--------|------|------|
| testBundle | hamcrest 3.0, hamcrest-library 3.0, junit 4.13.2, mockk 1.14.9 | 本地单元测试 |
| androidTestBundle | androidx-test-ext-junit 1.3.0, espresso-core 3.7.0 | Android 仪器测试 |

## 测试分层策略

按被测对象依赖面,选最轻量的工具。项目刻意不引 Robolectric:凡是 Android 依赖都走 `mockkStatic` + `mockk<T>(relaxed = true)`,把 Android SDK 抠到"存根不被实际调用"为止。

| 层 | 示例 | 套路 |
|----|------|------|
| 纯逻辑 | `CollectionExtensions` / `RequestState` | 普通 `@Test`,无 setup |
| Flow | `FlowExtensions` / `requestStateFlow` / `collectState` | `runBlocking { flow.toList() }`,对状态序列做列表断言 |
| 协程(同步路径) | `requestResult` / suspend 扩展 | `runBlocking { ... }`,try/catch 断言 `CancellationException` 重抛 |
| 时间相关 | `CacheManager` / `throttleFirst` | `mockkStatic(SystemClock::class)` 伪造 `elapsedRealtime`;测相对今天用 `LocalDate.now() ± N` |
| Android 类 | `ActivityHierarchyManager` / `AppInitializer` | `mockkStatic(Log::class)` + `mockk<Activity/Application>(relaxed = true)` |
| 单例 | `CacheManager` / `ActivityHierarchyManager` | setup/teardown 里 `clear`,避免跨用例污染 |

## 项目沉淀的套路

### 套路 1 — `SystemClock.elapsedRealtime()` 时间轴

用 `answers { fakeTime }` 把时钟挂成可变字段,省掉 `delay`,测试确定性。参考 `FlowExtensionsTest.kt` / `CacheManagerTest.kt`:

```kotlin
private var fakeTime = 0L

@Before
fun setup() {
    mockkStatic(SystemClock::class)
    every { SystemClock.elapsedRealtime() } answers { fakeTime }
    fakeTime = 1_000L
}

@After
fun teardown() {
    unmockkStatic(SystemClock::class)
}
```

### 套路 2 — `Log` 静态 mock

任何产品代码里的 `Log.d/e` 在单测里都会抛 "Method xxx not mocked"。提前 stub 即可。参考 `AppInitializerTest.kt:29-42`:

```kotlin
@Before
fun setup() {
    mockkStatic(Log::class)
    every { Log.d(any(), any<String>()) } returns 0
    every { Log.e(any(), any<String>()) } returns 0
    every { Log.e(any(), any<String>(), any()) } returns 0
}

@After
fun teardown() {
    unmockkStatic(Log::class)
}
```

### 套路 3 — Fake + 记录顺序

测拓扑/分派等"顺序敏感"逻辑时,用 private fake class + 共享 `MutableList<String>` 记录调用顺序,比 mock 每次调用更直观。参考 `AppInitializerTest.kt:140-210`:

```kotlin
private class TaskA(private val order: MutableList<String>) : Initializer<Unit> {
    override fun create(context: Context) { order.add("A") }
    override fun dependencies() = emptySet<Class<out Initializer<*>>>()
}
```

### 套路 4 — Relaxed mock + 选择性 stub

`mockk<T>(relaxed = true)` 对所有方法返回默认值,再对真正关心的字段显式 stub。参考 `ActivityHierarchyManagerTest.kt:37-48`:

```kotlin
mockActivity1 = mockk<Activity>(relaxed = true)
every { mockActivity1.isFinishing } returns false
every { mockActivity1.isDestroyed } returns false
```

### 套路 5 — 单例清理

项目里 `CacheManager` / `ActivityHierarchyManager` 是进程级单例,setup 先 `clear` 避免前置状态,teardown 再 `clear` 避免影响下一个用例。对 `@Volatile` 全局开关(如 `ApiConfig.errorCodeChecker`),setup 抓基线,teardown 还原(见 `ApiTransformTest.kt`)。

### 套路 6 — Flow Error 路径的警告

`.flowOn(Dispatchers.IO)` 的 `ChannelFlow` 在上游抛异常时,**可能丢弃 buffered 的 Loading**(Kotlin 协程已知行为)。测 Error 分支不要断言 `states[0] is Loading`,改用 `states.filterIsInstance<RequestState.Error>().single()`。Loading 的存在性由 Success 路径保证(参考 `ApiTransformTest.kt`)。

### 套路 7 — `CancellationException` 必须重抛

任何包装 `requestXxx { ... }` 的代码都要让 `CancellationException` 原样向上抛,否则会吞协程取消信号。单测用 try/catch 直接断言:

```kotlin
val caught = try {
    requestResult<Int> { throw CancellationException("cancel") }
    null
} catch (e: CancellationException) { e }
assertThat(caught, notNullValue())
```

## MockK 使用模式

### 创建 Mock

```kotlin
val mockActivity = mockk<Activity>(relaxed = true)    // 所有方法返回默认值
val mockCallback = mockk<(Result<Data>) -> Unit>()    // 必须显式配置每次调用
```

### 配置行为

```kotlin
every { mockActivity.isFinishing } returns false
every { mockService.getData() } throws IOException("network error")
every { mockRepository.fetch() } returnsMany listOf(data1, data2)  // 依次返回,最后一个无限重复
every { SystemClock.elapsedRealtime() } answers { fakeTime }       // 动态答复
```

### 验证调用

```kotlin
verify { mockActivity.finish() }
verify(exactly = 0) { mockActivity.finish() }
verify(exactly = 1) { mockActivity.finish() }
```

## 断言模式

```kotlin
assertThat(result, `is`(expected))
assertThat(result, nullValue())
assertThat(result, notNullValue())
assertThat(list, hasItem("expected"))
assertThat(list, hasSize(3))
assertThat(result, instanceOf(RequestState.Error::class.java))
assertThat(result, sameInstance(other))   // 引用相等;警惕 coroutines stack-trace recovery 会 clone 异常
```

## 运行测试

```bash
# 全部单元测试
./gradlew test

# 指定模块
./gradlew :module_network:test
./gradlew :module_proxy:test

# 单个测试类
./gradlew test --tests "com.yikwing.network.ApiTransformTest"

# 仪器测试(需连接设备)
./gradlew connectedDebugAndroidTest
```

## 测试矩阵

当前单测分布(已剔除 AS 模板占位):

| 模块 | 测试类 | 备注 |
|------|--------|------|
| module_config | `YkConfigManagerTest` | JSON 解析 + 初始化状态 |
| module_extension | `BooleanExtensionsTest` / `CollectionExtensionsTest` / `FlowExtensionsTest` / `LocalDateUtilsTest` / `NotNullSingleVarTest` / `CacheManagerTest` / `ColorExtensionsTest` / `DigestUtilsTest` / `InitStateTest` | 扩展函数、Flow、时间、LRU、摘要等 |
| module_network | `RequestStateTest` / `ApiTransformTest` / `ApiExceptionTest` | 三态 DSL、`requestStateFlow` / `requestResult` 全路径、异常包装 |
| module_proxy | `AppInitializerTest` / `ActivityHierarchyManagerTest` | Kahn 拓扑、Activity 栈 |
| module_compose / module_permission / app | 仅占位 | 对应类以 Compose 声明 / Android Framework 包装为主,纯单测价值低 |

新增 Network/Extension/Proxy 的 public API 时,默认在对应 `*Test.kt` 补用例;只涉及 Compose 声明的改动可不补单测,改用手动验证。

## 何时不需要写测试

- **纯 data class** — 仅字段聚合,无逻辑
- **纯 Compose UI(声明式)** — 视觉/交互测试走手动或仪器测试,不适合纯单测
- **Convention Plugin / Gradle 配置**
- **单行包装器** — 如 `Ref<T>(var value: T)`、`noOpDelegate`,一行逻辑 + 构造,测试成本高于收益
- **生成代码** — Proto、Room Dao、Koin 注解产物
- **极薄 Repository/UseCase 桥** — 只是转发调用,测下游已覆盖

## 陷阱

### 不要 mockkStatic java.time 类

JDK17+ 默认不对 `java.base` 模块开放反射。`mockkStatic(LocalDate::class)` / `LocalDateTime::class` 会让**所有**静态调用抛 `IllegalAccessException`,包括未显式 stub 的 `LocalDate.of(...)` / `LocalDateTime.of(...)`。

测"相对今天"的谓词(`isToday` / `isPast` / `isFuture`)用 `LocalDate.now() ± N` 作基准,而不是伪造 `now()`:

```kotlin
@Test
fun `isPast should be true for yesterday`() {
    assertThat(LocalDate.now().minusDays(1).isPast(), `is`(true))
}
```

可 mock 的时间源:`android.os.SystemClock`(见 `CacheManagerTest` / `FlowExtensionsTest`)。

### `sameInstance` 和 coroutines stack-trace recovery

kotlinx-coroutines 会对有 `(String, Throwable)` 构造的异常(如 `IOException`)做 `recoverStackTrace`,把原异常 clone 再抛。导致 `sameInstance(original)` 失败。改用类型 + message 断言:

```kotlin
assertThat(thrown, instanceOf(IOException::class.java))
assertThat(thrown.message, `is`("network"))
```

`ApiException` 没有 `(Throwable)` 或 `(String, Throwable)` 构造,不会被 clone,可以用 `sameInstance` 验证透传。

### Android framework 类的纯单测代价

`SparseIntArray` / `Bundle` / `TextUtils` 等类在 android.jar stub 下方法体为空。若要测它们的行为:

- 方法少且逻辑简单:`mockk<SparseIntArray>()` + 手动 stub `indexOfKey` / `valueAt` / `put`
- 方法多/需要完整行为:放到仪器测试(`androidTest/`)
- 项目现状是两者都不做,涉及 Android 容器的扩展函数默认不在纯单测里覆盖

### `Log` 重载参数类型匹配

`Log.e` 有多个重载(2 参 / 3 参)。`mockkStatic` 后要分别 stub:

```kotlin
every { Log.e(any(), any<String>()) } returns 0
every { Log.e(any(), any<String>(), any()) } returns 0
```

漏 stub 其中一个会导致运行时抛 "Method e not mocked"。