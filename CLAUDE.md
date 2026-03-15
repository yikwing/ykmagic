# CLAUDE.md

YkQuickDev - Android 快速开发框架库

**技术栈**: Kotlin 2.3.20-RC3 | Koin 4.2.0-RC2 | Ktor 3.4.1 | Compose BOM 2026.03.00

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

**模块**: config(配置) | network(网络) | extension(工具) | proxy(框架) | datastore(存储) | logger(日志) | permission(权限) | component(UI)

---

## 🎯 任务指南

| 任务 | 文档 | 核心 API |
|------|------|---------|
| 网络请求 | [network.md](.claude/docs/network.md) | `requestStateFlow()` / `requestResult()` |
| 依赖注入 | [dependency-injection.md](.claude/docs/dependency-injection.md) | `@KoinViewModel` / `@InjectedParam` |
| 数据存储 | [datastore.md](.claude/docs/datastore.md) | `DataStore<T>` |
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

---

## 🚀 常用命令

```bash
# 构建
./android_build.sh dev         # Debug
./android_build.sh build       # Release
./android_build.sh clean       # 清理

# 测试
./gradlew test                 # 单元测试
./gradlew :module_name:test    # 指定模块

# 调试
adb logcat | grep "YkQuickDev"  # 查看应用日志
adb install -r app/build/outputs/apk/debug/app-debug.apk  # 安装 Debug
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

**依赖注入**: Koin | `@KoinApplication` `@KoinViewModel` `@InjectedParam` | Kotzilla 监控

**模块初始化**: AppInitializer | 拓扑排序 | 循环检测 | 实现 `InitTask`

**Activity 管理**: ActivityHierarchyManager | 线程安全 | WeakReference | `getTopActivity()`

**内存缓存**: CacheManager | LRU(256) + TTL | 线程安全

**详细文档**: [modules.md](.claude/docs/modules.md)

---

## 📋 版本要求

**最低**: Android Studio Ladybug 2024.2.1+ | JDK 17+ | Gradle 9.3.0+ | Kotlin 2.3.0+

**查询**: `cat gradle/libs.versions.toml` | `./android_build.sh dependency`

**锁定**: `activity` 和 `kotlinx-coroutines-core` 已强制统一

**兼容性**: Kotlin<2.3.0 不支持 Explicit Backing Fields | AGP<8.13.0 KSP 可能失败 | Koin 4.2.x 暂不升级

---

## 📚 项目约定

**构建**: 通用配置在根 `build.gradle.kts` | 启用 `-XXLanguage:+ExplicitBackingFields`

**依赖**: `gradle/libs.versions.toml` 统一管理 | 避免硬编码 | Debug 工具仅 Debug 版本

**代码**: 协程和 Flow | "动词 suspend，名词 Flow" | Explicit Backing Fields | `@Serializable`

**Compose 性能**: 避免组合阶段读取高频状态 | 用 Lambda 延迟状态读取 | `drawBehind` 替代 `background` | `offset { }` 替代 `offset()` | 参数匹配用函数引用 | 参数转换用 Lambda | 复杂逻辑用 `remember` 缓存

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
| [modules.md](.claude/docs/modules.md) | 模块化设计 |
| [build-publish.md](.claude/docs/build-publish.md) | 构建发布 |
| [patterns.md](.claude/docs/patterns.md) | 开发模式 |
| [testing.md](.claude/docs/testing.md) | 测试指南 |
| [android-studio-tips.md](.claude/docs/android-studio-tips.md) | IDE 优化 |
