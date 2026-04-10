# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

YkQuickDev - Android 快速开发框架库

**技术栈**: Kotlin 2.3.20 | Koin 4.2.1 | Ktor 3.4.2 | Compose BOM 2026.03.01 | Room 3.0.0-alpha03 | Nav3 1.1.0 | Coil 3.4.0

**版本信息**: 以 `gradle/libs.versions.toml` 为准

---

## 📁 快速定位

| 类型 | 位置 | 说明 |
|------|------|------|
| Application | `app/.../MainApplication.kt` | Koin 初始化 |
| 配置管理 | `module_config/.../YkConfigManager.kt` | 读取配置 |
| 网络模块 | `module_network/.../` | Ktor Client |
| 应用配置 | `android_env.json` | 运行时配置（必需） |
| 签名配置 | `keystore.properties` | Release 构建（必需） |
| 版本管理 | `gradle/libs.versions.toml` | 依赖版本 |
| 导航 | `app/.../app/AppNavGraph.kt` | Navigation3 路由（见 [patterns.md](.claude/docs/patterns.md) 三层命名规范） |
| 数据库 | `app/.../db/UserDatabase.kt` | Room3 (androidx.room3) |
| DI 模块 | `app/.../di/AppModule.kt` | Koin 模块聚合（Core/Feature/Network/Data） |
| 初始化任务 | `app/.../task/` | AppInitializer 的 Initializer<T> 实现 |
| Compose 组件 | `module_compose/` | 通用组件（Loading/Error/Image/Center/Debounce/SystemBars） |
| Compose 屏幕 | `app/.../ui/screen/` | 页面级 Composable |
| Compose 主题 | `app/.../ui/theme/` | Material3 主题/颜色/字体 |
| API 定义 | `app/.../api/apiserver/HttpApi.kt` | Ktor 接口定义 |
| API 实体 | `app/.../api/entity/` | 请求/响应数据类（@Serializable） |
| Proto 定义 | `app/src/main/protos/` | DataStore Proto 消息定义 |
| DataStore 扩展 | `app/.../DataStoreExtensions.kt` | `getLatest()` / `updateAndGet()` / `select()` |
| 后台任务 | `app/.../work/` | WorkManager 任务实现 |

**模块**: config(配置) | network(网络) | extension(工具) | proxy(框架) | compose(Compose组件) | permission(权限)

---

## 🎯 任务指南

| 任务 | 文档 | 核心 API |
|------|------|---------|
| 网络请求 | [network.md](.claude/docs/network.md) | `requestStateFlow()` / `requestResult()` |
| 依赖注入 | [dependency-injection.md](.claude/docs/dependency-injection.md) | `@KoinViewModel` / `@InjectedParam` |
| 数据存储 | [datastore.md](.claude/docs/datastore.md) | `DataStore<T>` |
| 构建逻辑 | [build-logic.md](.claude/docs/build-logic.md) | Convention Plugins |
| 代码示例 | [code-examples.md](.claude/docs/code-examples.md) | 完整代码 |
| 构建发布 | [build-publish.md](.claude/docs/build-publish.md) | `./android_build.sh` |
| 开发模式 | [patterns.md](.claude/docs/patterns.md) | 最佳实践 |
| 测试 | [testing.md](.claude/docs/testing.md) | `./gradlew test` |

---

## 🤔 决策树

```
网络请求: 需要UI状态? 是→requestStateFlow() 否→requestResult()
数据缓存: 临时数据? 是→CacheManager 否→DataStore
ViewModel: 需要参数? 是→@InjectedParam 否→构造注入
Compose动画: 改变视觉? 是→drawBehind/graphicsLayer 否→改变位置?→offset { }
Compose回调: 参数匹配? 是→函数引用 否→需要缓存?→是→remember+Lambda 否→Lambda
后台任务: 需要持久化? 是→WorkManager(CoroutineWorker) 否→协程/viewModelScope
UI组件: 页面级? 是→ui/screen/ 否→通用复用?→是→components/ 否→ui/widget/
```

---

## 📝 代码速查

```kotlin
// 网络请求 (UI)
@KoinViewModel
class MyViewModel(private val api: ApiService) : ViewModel() {
    private val _state = MutableStateFlow<RequestState<Data>>(RequestState.Loading)
    val state = _state.asStateFlow()

    fun load() = viewModelScope.launch {
        api.getData().requestStateFlow().collect { _state.value = it }
    }
}

// 网络请求 (后台)
suspend fun sync() = api.getData().requestResult()

// 依赖注入 (基础)
@KoinViewModel
class MyViewModel(private val repo: Repository) : ViewModel()

// 依赖注入 (带参数)
@KoinViewModel
class DetailViewModel(
    @InjectedParam private val id: String,
    private val repo: Repository
) : ViewModel()

// 使用
val vm: DetailViewModel = koinViewModel { parametersOf(id) }

// 配置读取
val baseUrl = YkConfigManager.config.baseUrl

// Compose 性能优化 (避免过度重组)
// ❌ 差：组合阶段读取动画状态，每帧重组
.background(animatedColor)
.offset(x = animatedX, y = animatedY)

// ✅ 好：延迟到绘制/布局阶段，仅重绘
.drawBehind { drawRect(animatedColor) }
.offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }

// Compose 回调优化 (函数引用 vs Lambda)
// ✅ 好：参数签名匹配，用函数引用（稳定，不触发重组）
Button(onClick = viewModel::onButtonClick)

// ✅ 好：需要传递参数，用 Lambda
Button(onClick = { viewModel.onItemClick(item.id) })

// ✅ 好：复杂逻辑，用 remember 缓存
val onClick = remember(item.id) { { viewModel.onItemClick(item.id) } }
Button(onClick = onClick)
```

**详细示例**: [code-examples.md](.claude/docs/code-examples.md)

---

## 🔧 环境配置

**android_env.json** (必需)
```json
{
  "base_url": "https://api.example.com",
  "api_key": "your_key",
  "timeout": 30000
}
```

**keystore.properties** (Release 必需)
```properties
storeFile=path/to/keystore.jks
storePassword=your_password
keyAlias=your_alias
keyPassword=your_key_password
```

**检查**: `ls -la android_env.json keystore.properties`

---

## ⚠️ 常见问题

| 错误 | 解决 |
|------|------|
| `YK_CONFIG 未定义` | 创建 android_env.json |
| `Keystore not found` | 配置 keystore.properties |
| KSP 生成失败 | `./android_build.sh clean` |
| `No Koin context` | 检查 `@KoinApplication` 注解 |
| 网络请求失败 | 检查 android_env.json 中的 base_url |
| ViewModel 注入失败 | 添加 `@KoinViewModel` 注解 |
| build-logic 修改不生效 | 运行 `./gradlew clean --no-daemon` 清理缓存 |
| Convention Plugin 编译错误 | 检查 `VersionCatalogsExtension` 访问方式，确保 build-logic/settings.gradle.kts 正确配置 |

---

## 🚀 常用命令

```bash
# 构建
./android_build.sh all         # 清理、构建并安装 Release APK
./android_build.sh dev         # Debug
./android_build.sh build       # Release
./android_build.sh install     # 安装 Release APK
./android_build.sh clean       # 清理

# build-logic 修改后需要清理
./gradlew clean --no-daemon    # 清理所有模块（包括 build-logic）
./gradlew :build-logic:convention:build --no-daemon  # 单独验证 build-logic

# 批量修改模块配置
for file in module_*/build.gradle.kts; do sed -i '' '/pattern/d' "$file"; done

# 测试
./gradlew test                 # 单元测试
./gradlew :module_name:test    # 指定模块

# 调试
adb logcat | grep "YkQuickDev"  # 查看应用日志
adb install -r app/build/outputs/apk/debug/app-debug.apk  # 安装 Debug APK
adb uninstall <package_name>    # 卸载应用

# 质量
./gradlew lint                 # Lint 检查
./gradlew lintFix              # 自动修复

# 依赖
./android_build.sh dependency  # 检查更新
cat gradle/libs.versions.toml  # 查看版本
```

---

## 🏗️ 核心架构

**配置管理**: `android_env.json` → BuildConfig → YkConfigManager → `config.baseUrl`

**网络层**: Ktor Client | "动词 suspend，名词 Flow" | 拦截器: Header/Retry/Log/Chucker

**构建逻辑**: Convention Plugins (build-logic/) | 配置常量 (ProjectConfig.kt) | 依赖自动管理 | Hotswan compiler 插件 (Compose 注解处理)

**BuildConfig 策略**: Application 默认启用 | Library 按需启用 | 避免过度生成

**依赖注入**: Koin | `@KoinApplication` `@KoinViewModel` `@InjectedParam` | Kotzilla 监控

**模块初始化**: AppInitializer | 拓扑排序 | 循环检测 | 实现 `Initializer<T>`

**Activity 管理**: ActivityHierarchyManager | 线程安全 | WeakReference | `getTopActivity()`

**内存缓存**: CacheManager | LRU(256) + TTL | 线程安全

**图片加载**: Coil 3.0+ | `AsyncImage()` Compose 组件 | 支持缓存/变换

**数据存储**: Proto DataStore | Wire `.copy()` 修改字段（非 protobuf `.toBuilder()`）| Serializer 用 Okio `.use {}` 自动关闭 buffer

**后台任务**: WorkManager | 实现 `CoroutineWorker` | Koin 注入

**详细文档**: [modules.md](.claude/docs/modules.md)

---

## 📋 版本要求

**最低**: Android Studio Ladybug 2024.2.1+ | JDK 17+ | Gradle 9.3.0+ | Kotlin 2.3.0+

**查询**: `cat gradle/libs.versions.toml` | `./android_build.sh dependency`

**锁定**: `activity` 和 `kotlinx-coroutines-core` 已强制统一

**兼容性**: Kotlin<2.3.0 不支持 Explicit Backing Fields | AGP<8.13.0 KSP 可能失败 | Room 3.0 命名空间为 `androidx.room3`（非 `androidx.room`）

**Convention Plugins**: `ykmagic.android.application` | `ykmagic.android.library` | `ykmagic.android.compose` | `ykmagic.android.koin` | `ykmagic.android.room` | `ykmagic.android.wire`

---

## 📚 项目约定

**构建**: Convention Plugins 管理通用配置（见 [build-logic.md](.claude/docs/build-logic.md)）

**依赖**: `gradle/libs.versions.toml` 统一管理 | 避免硬编码 | Debug 工具仅 Debug 版本

**自动依赖**: Convention Plugins 自动添加通用依赖（core-ktx, appcompat, coroutines, testBundle）| Compose 插件自动添加 Compose 依赖 | Koin 插件自动添加 Koin 依赖 | 模块只需声明特定依赖

**代码**: 协程和 Flow | "动词 suspend，名词 Flow" | Explicit Backing Fields | `@Serializable`

**Compose 性能**: 避免组合阶段读取高频状态 | 用 Lambda 延迟状态读取 | `drawBehind` 替代 `background` | `offset { }` 替代 `offset()` | 参数匹配用函数引用 | 参数转换用 Lambda | 复杂逻辑用 `remember` 缓存

**Compose 陷阱**: `LaunchedEffect` 放 Screen 顶层，不能嵌套在 Loading/Empty 等分支内 | `sealed class` 默认 @Stable，无需手动标注

**测试**: Hamcrest 匹配器 | `<ClassName>Test` | 关键逻辑必须覆盖

**模块**: `settings.gradle.kts` 注册 | 避免循环依赖 | 公共功能放 `module_extension`

**Git**: `<type>(<scope>): <subject>` | feat/fix/docs/style/refactor/test/chore/build

---

## 📖 完整文档

| 文档 | 内容 |
|------|------|
| [code-examples.md](.claude/docs/code-examples.md) | 完整代码示例 |
| [network.md](.claude/docs/network.md) | Ktor 网络详解 |
| [dependency-injection.md](.claude/docs/dependency-injection.md) | Koin 依赖注入 |
| [datastore.md](.claude/docs/datastore.md) | Proto DataStore |
| [build-logic.md](.claude/docs/build-logic.md) | Convention Plugins |
| [build-logic/OPTIMIZATION.md](build-logic/OPTIMIZATION.md) | build-logic 优化报告 |
| [build-logic/REFACTOR.md](build-logic/REFACTOR.md) | build-logic 重构说明 |
| [modules.md](.claude/docs/modules.md) | 模块化设计 |
| [build-publish.md](.claude/docs/build-publish.md) | 构建发布 |
| [patterns.md](.claude/docs/patterns.md) | 开发模式 |
| [testing.md](.claude/docs/testing.md) | 测试指南 |
| [android-studio-tips.md](.claude/docs/android-studio-tips.md) | IDE 优化 |

**参考项目**: [CoolMallKotlin](https://github.com/joker-xii/CoolMallKotlin) - build-logic 最佳实践参考
