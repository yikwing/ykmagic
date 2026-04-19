# 测试指南

## 项目测试约定

- **测试框架**: JUnit 4（`@Test` / `@Before` / `@After`）
- **Mock 工具**: MockK（Kotlin 生态首选）
- **断言库**: Hamcrest（`assertThat` + matchers）
- **测试命名**: Kotlin 反引号方法名描述意图（如 `` `register should add activity to stack` ``）
- **测试文件**: `<ClassName>Test`，放在与源码对应的 test 包下

## 测试依赖

Convention Plugin 自动添加测试依赖（`testBundle` + `androidTestBundle`）：

| Bundle | 包含 | 用途 |
|--------|------|------|
| testBundle | hamcrest 3.0, hamcrest-library 3.0, junit 4.13.2, mockk 1.14.9 | 本地单元测试 |
| androidTestBundle | androidx-test-ext-junit 1.3.0, espresso-core 3.7.0 | Android 仪器测试 |

## MockK 使用模式

### Setup / Teardown

```kotlin
class ActivityHierarchyManagerTest {
    @Before
    fun setup() {
        // Mock Android 框架类
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        // 清理所有 mock
        clearAllMocks()
        unmockkStatic(Log::class)
    }
}
```

### 创建 Mock

```kotlin
// Relaxed mock：方法返回默认值，无需逐个配置
val mockActivity = mockk<Activity>(relaxed = true)

// 普通 mock：必须显式配置每个方法
val mockCallback = mockk<(Result<Data>) -> Unit>()
```

### 配置行为

```kotlin
// 返回值
every { mockActivity.isFinishing } returns false
every { mockActivity.isDestroyed } returns false

// 抛异常
every { mockService.getData() } throws IOException("network error")

// 多次调用返回不同值（依次返回列表中的值，最后一个无限重复）
every { mockRepository.fetch() } returnsMany listOf(data1, data2)
```

### 验证调用

```kotlin
// 验证调用发生
verify { mockActivity.finish() }

// 验证调用次数
verify(exactly = 0) { mockActivity.finish() }  // 未发生
verify(exactly = 1) { mockActivity.finish() }
```

## 断言模式

```kotlin
// 相等
assertThat(result, `is`(expected))

// Null 检查
assertThat(result, `is`(nullValue()))
assertThat(result, notNullValue())

// 集合
assertThat(list, hasItem("expected"))
assertThat(list, hasSize(3))

// 组合
assertThat(result, allOf(notNullValue(), `is`(expected)))
```

## 运行测试

```bash
# 运行所有单元测试
./gradlew test

# 运行指定模块测试
./gradlew :module_config:test
./gradlew :module_proxy:test

# 运行单个测试类
./gradlew test --tests "com.yikwing.config.YkConfigManagerTest"
./gradlew test --tests "com.yikwing.proxy.util.ActivityHierarchyManagerTest"

# 运行 Android 仪器测试
./gradlew connectedDebugAndroidTest
```

## 项目测试示例

- `module_config/.../YkConfigManagerTest.kt` — 纯逻辑测试（JSON 解析、初始化状态）
- `module_proxy/.../ActivityHierarchyManagerTest.kt` — Mock + 行为验证（Activity 栈管理）

## 陷阱

### 不要 mockkStatic java.time 类

JDK17+ 默认不对 `java.base` 模块开放反射，`mockkStatic(LocalDate::class)` /
`LocalDateTime::class` 等会让**所有**静态调用抛 `IllegalAccessException`，
包括未显式 stub 的 `LocalDate.of(...)` / `LocalDateTime.of(...)`。

测"相对今天"的谓词（`isToday` / `isPast` / `isFuture`）用 `LocalDate.now() ± N`
作基准，而非伪造 `now()`：

```kotlin
@Test
fun `isPast should be true for yesterday`() {
    assertThat(LocalDate.now().minusDays(1).isPast(), `is`(true))
}
```

可 mock 的时间源：`android.os.SystemClock`（见 `CacheManagerTest` / `FlowExtensionsTest`）。
