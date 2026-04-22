# CLAUDE.md

YkQuickDev - Android 快速开发框架库

**技术栈**: Kotlin 2.3.20 | Koin 4.2.1 | Ktor 3.4.2 | Compose BOM 2026.03.01 | Room 3.0.0-alpha03 | Nav3 1.1.0 | Coil 3.4.0

**版本信息**: 以 `gradle/libs.versions.toml` 为准

---

## 快速定位

| 类型 | 位置 | 说明 |
|------|------|------|
| Application | `app/.../MainApplication.kt` | Koin 初始化 |
| 配置管理 | `module_config/.../YkConfigManager.kt` | 读取配置 |
| 网络模块 | `module_network/.../` | Json/RequestState 等基础组件 |
| 应用配置 | `android_env.json` | 运行时配置（必需） |
| 签名配置 | `keystore.properties` | Release 构建（必需） |
| 版本管理 | `gradle/libs.versions.toml` | 依赖版本 |
| 导航 | `app/.../app/AppNavGraph.kt` | Navigation3 路由（见 [nav3-guide.md](.claude/docs/nav3-guide.md)） |
| 数据库 | `app/.../db/UserDatabase.kt` | Room3 (androidx.room3) |
| DI 模块 | `app/.../di/AppModule.kt` | Koin 聚合（AppFeatureModule + NetworkModule） |
| 初始化任务 | `app/.../task/` | AppInitializer 的 Initializer<T> 实现 |
| Compose 组件 | `module_compose/` | 通用组件（Loading/Error/Image/Center/Debounce/SystemBars） |
| Compose 屏幕 | `app/.../ui/screen/` | 页面级 Composable |
| Compose 主题 | `app/.../ui/theme/` | Material3 主题/颜色/字体 |
| API 定义 | `app/.../api/apiserver/` | HttpBinApi.kt, WanAndroidApi.kt |
| API 实体 | `app/.../api/entity/` | 请求/响应数据类（@Serializable） |
| Proto 定义 | `app/src/main/protos/` | UserPreferences / AppSettings |
| DataStore 扩展 | `app/.../DataStoreExtensions.kt` | `getLatest()` / `updateAndGet()` / `select()` |
| DataStore Serializer | `app/.../datastore/` | UserPreferencesSerializer / AppSettingsSerializer |
| 序列化工具 | `app/.../api/serializers/` | 自定义 KSerializer（如 BooleanAsInt） |
| 后台任务 | `app/.../work/` | WorkManager 任务实现 |
| UI 工具组件 | `app/.../ui/widget/` | 页面级非通用 Widget |

**模块**: config(配置) | network(网络) | extension(工具) | proxy(框架) | compose(Compose组件) | permission(权限)

---

## 任务指南

| 任务 | 文档 | 核心 API |
|------|------|---------|
| 网络请求 | [network.md](.claude/docs/network.md) | `requestStateFlow()` / `requestResult()` |
| 依赖注入 | [dependency-injection.md](.claude/docs/dependency-injection.md) | `@KoinViewModel` / `@InjectedParam` |
| 数据存储 | [datastore.md](.claude/docs/datastore.md) | `DataStore<T>` |
| 构建逻辑 | [build-logic.md](.claude/docs/build-logic.md) | Convention Plugins |
| 构建发布 | [build-publish.md](.claude/docs/build-publish.md) | `./android_build.sh` |
| 事件处理 | [event-patterns.md](.claude/docs/event-patterns.md) | Channel / SharedFlow / StateFlow |
| Flow 收集 | [flow-collect-best-practices.md](.claude/docs/flow-collect-best-practices.md) | `repeatOnLifecycle` / `launchWhenStarted` |
| 导航规范 | [nav3-guide.md](.claude/docs/nav3-guide.md) | Route / Entry / Screen |
| 错误处理 | [result-patterns.md](.claude/docs/result-patterns.md) | Result<T> |
| 测试 | [testing.md](.claude/docs/testing.md) | MockK / Hamcrest |
| 位操作 / @IntDef | [intdef-bitflags.md](.claude/docs/intdef-bitflags.md) | `@IntDef` / bit flags |
| 协程回调桥接 | [coroutine-bridge.md](.claude/docs/coroutine-bridge.md) | `suspendCancellableCoroutine` / `callbackFlow` |

---

## 决策树

```
网络请求: 需要UI状态? 是→requestStateFlow() 否→requestResult()
数据缓存: 临时数据? 是→CacheManager(module_network) 否→DataStore
ViewModel: 需要参数? 是→@InjectedParam 否→构造注入
Compose动画: 改变视觉? 是→drawBehind/graphicsLayer 否→改变位置?→offset { }
Compose回调: 参数匹配? 是→函数引用 否→需要缓存?→是→remember+Lambda 否→Lambda
后台任务: 需要持久化? 是→WorkManager(CoroutineWorker) 否→协程/viewModelScope
UI组件: 页面级? 是→ui/screen/ 否→通用复用?→是→module_compose/ 否→ui/widget/
```

---

## 常用命令

```bash
./gradlew :app:assembleDebug          # 构建 Debug
./gradlew :app:assembleRelease        # 构建 Release（需 keystore.properties）
./gradlew test                        # 单元测试
./gradlew :app:connectedAndroidTest   # 仪器测试
```

## 环境与构建

环境配置、构建命令、常见问题 → [build-publish.md](.claude/docs/build-publish.md)

架构详解、模块说明 → [modules.md](.claude/docs/modules.md)

---

## 项目约定

**构建**: Convention Plugins 管理通用配置（见 [build-logic.md](.claude/docs/build-logic.md)）

**依赖**: `gradle/libs.versions.toml` 统一管理 | 避免硬编码 | Debug 工具仅 Debug 版本

**自动依赖**: Convention Plugins 自动添加通用依赖（core-ktx, appcompat, coroutines, testBundle）| Compose 插件自动添加 Compose 依赖 | Koin 插件自动添加 Koin 依赖 | 模块只需声明特定依赖

**代码**: 协程和 Flow | "动词 suspend，名词 Flow" | Explicit Backing Fields | `@Serializable`

**Compose 性能**: 避免组合阶段读取高频状态 | 用 Lambda 延迟状态读取 | `drawBehind` 替代 `background` | `offset { }` 替代 `offset()` | 参数匹配用函数引用 | 参数转换用 Lambda | 复杂逻辑用 `remember` 缓存

**Compose 陷阱**: `LaunchedEffect` 放 Screen 顶层，不能嵌套在 Loading/Empty 等分支内 | `sealed class` 默认 @Stable，无需手动标注

**DI 陷阱**: 同一原始类型多实例（如多个 `DataStore<T>`、多个 `Flow<T>`）必须用自定义 `@Qualifier` 注解区分；仅靠泛型参数在 R8 release 下会塌缩成同一 key，导致运行时 `ClassCastException`

**测试**: Hamcrest + MockK | `<ClassName>Test` | 关键逻辑必须覆盖

**测试陷阱**: 禁止 `mockkStatic(LocalDate::class)` 等 `java.time.*`（JDK17+ `java.base` 反射未开放，非 stub 的静态调用也会抛 `IllegalAccessException`）；测相对今天用 `LocalDate.now() ± N`

**模块**: `settings.gradle.kts` 注册 | 避免循环依赖 | 公共功能放 `module_extension`

**Git**: `<type>(<scope>): <subject>` | feat/fix/docs/style/refactor/test/chore/build

