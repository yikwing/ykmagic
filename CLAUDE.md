# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

YkQuickDev 是一个 Android 快速开发框架库,提供了多个可独立使用的功能模块,用于加速 Android 应用开发。

**技术栈**:
- Kotlin 2.3.0 + Coroutines 1.10.2
- Gradle 8.13.2 + 版本目录 (libs.versions.toml) 统一管理依赖
- JDK 17, compileSdk 36, minSdk 26
- 依赖注入: Koin 4.1.1 + Koin Annotations 2.3.1 (KSP 2.3.4)
- 网络层: Ktor 3.3.3 + OkHttp 5.3.2
- UI: Jetpack Compose (BOM 2025.12.01) + Material3
- 序列化: kotlinx.serialization 1.9.0
- 发布: JitPack (com.github.yikwing.ykmagic:模块名:版本号)

## 详细文档索引

按需查阅以下专题文档:

| 文档 | 内容 | 适用场景 |
|------|------|---------|
| [network.md](.claude/docs/network.md) | Ktor 网络请求、suspend vs Flow、依赖注入 | 编写 API、处理响应 |
| [dependency-injection.md](.claude/docs/dependency-injection.md) | Koin 依赖注入、ViewModel 注入 | 添加依赖、配置 DI |
| [datastore.md](.claude/docs/datastore.md) | Proto DataStore、Flow 操作 | 存储用户偏好 |
| [modules.md](.claude/docs/modules.md) | 模块化设计、AppInitializer、CacheManager | 了解项目结构 |
| [build-publish.md](.claude/docs/build-publish.md) | 构建命令、环境配置、模块发布 | 构建 APK、发布 |
| [patterns.md](.claude/docs/patterns.md) | Event Wrapper、Flow 生命周期、Compose 技巧 | 开发模式参考 |

## 快速参考

### 常用构建命令
```bash
./android_build.sh dev         # Debug 构建
./android_build.sh build       # Release 构建
./android_build.sh all         # 清理+构建+安装
./android_build.sh dependency  # 检查依赖更新
./gradlew test                 # 运行所有单元测试
./gradlew :module_config:test  # 运行指定模块测试
./gradlew test --tests "com.yikwing.config.ReturnsTest"  # 运行单个测试类
./gradlew connectedDebugAndroidTest  # 运行 Instrumented 测试
./gradlew lint                 # 运行 Lint 检查
./gradlew lintFix              # 自动修复 Lint 问题
```

### 模块概览
| 模块 | 功能 |
|------|------|
| module_config | 配置注入 (@YkConfigNode) |
| module_network | Ktor Client 网络请求 |
| module_extension | 扩展函数、CacheManager |
| module_datastore | Proto DataStore 封装 |
| module_permission | 权限请求 |
| module_logger | 日志组件 |
| module_proxy | BaseActivity、AppInitializer |
| module_component | UI 组件 |

### 必需配置文件
- `android_env.json` - 应用配置 (base_url 等)
- `keystore.properties` - 签名配置

### KSP 注解
- `@YkConfigNode` / `@YkConfigValue` - 配置注入
- `@Serializable` - JSON 序列化
- `@KoinViewModel` / `@Inject` - 依赖注入
- `@Entity` / `@Dao` / `@Database` - Room 数据库

### Debug 工具 (仅 Debug 版本)
- Chucker - 网络抓包
- LeakCanary - 内存泄漏检测
- Glance - 性能监控

## 核心架构设计

### 配置注入流程
`android_env.json` → BuildConfig.YK_CONFIG (构建时) → YkConfigManager (运行时) → KSP 生成代码

使用方式:
```kotlin
@YkConfigNode
@Serializable
data class NetworkConfig(@YkConfigValue(path = "base_url") val baseUrl: String)

// 初始化 (Application.onCreate)
YkConfigManager.setUp(BuildConfig.YK_CONFIG)

// 获取配置
val config = YkConfigManager.getConfig(NetworkConfig::class.java)
```

### 模块初始化机制 (AppInitializer)
基于拓扑排序的初始化框架，支持依赖声明和循环检测。

位置: `module_proxy/src/main/java/com/yikwing/proxy/startup/AppInitializer.kt`

使用方式:
```kotlin
// Application.onCreate()
AppInitializer.getInstance(this)
    .addTask(ConfigInjectInitTask())
    .addTask(LoggerInitTask())
    .addTask(NetworkInitTask())
    .addTask(DataStoreInitTask())
    .build(debug = true)
```

### 网络层设计原则
**技术栈**: Ktor Client + OkHttp Engine (已从 Retrofit 迁移)

**设计原则**: "动词用 suspend，名词用 Flow"
- UI 交互 → `requestStateFlow()` 返回 `Flow<RequestState<T>>`
- 后台操作 → `requestResult()` 返回 `Result<T?>`

拦截器架构:
- HeaderInterceptor: 自定义请求头
- RetryInterceptor: 重试逻辑
- LogInterceptor: 日志记录
- Chucker: Debug 网络抓包 (仅 Debug 版本)

### Activity 生命周期管理 (ActivityHierarchyManager)
全局 Activity 栈管理器，线程安全 + 内存安全 (WeakReference)

位置: `module_proxy/src/main/java/com/yikwing/proxy/util/ActivityHierarchyManager.kt`

核心 API:
- `getTopActivity()` - 获取栈顶 Activity
- `finishTopActivities(count)` - 关闭栈顶 N 个
- `finishAllExcept(activityClass)` - 关闭除指定外的所有
- `finishUntil(activityClass, inclusive)` - 关闭到指定 Activity

### 内存缓存 (CacheManager)
LRU 淘汰 (默认 256 条) + TTL 过期机制

位置: `module_extension/src/main/java/com/yikwing/extension/util/CacheManager.kt`

```kotlin
CacheManager.put("user", userObj)  // 永不过期
CacheManager.put("token", "abc123", ttlMillis = 5 * 60 * 1000L)  // 5 分钟过期
val config = CacheManager.getOrPut("config", ttlMillis = 60_000L) { loadConfigFromDisk() }
```

### Koin 依赖注入
- 使用 Koin Annotations + KSP 自动生成代码
- Kotzilla SDK 1.4.2 监控 (15 秒刷新率)
- Application 需添加 `@KoinApplication` 注解
- ViewModel 使用 `@KoinViewModel` 注解