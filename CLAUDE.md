# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

YkQuickDev 是一个 Android 快速开发框架库,提供了多个可独立使用的功能模块,用于加速 Android 应用开发。

**技术栈**:
- Kotlin 2.2.21 + Coroutines 1.10.2
- Gradle 版本目录 (libs.versions.toml) 统一管理依赖
- JDK 21, compileSdk 36, minSdk 26
- 依赖注入: Koin 4.1.1 + Koin Annotations 2.3.1
- KSP 2.3.3 注解处理
- Jetpack Compose UI
- 支持通过 JitPack (com.github.yikwing.ykmagic:模块名:版本号) 或本地模块依赖

## 详细文档索引

按需查阅以下专题文档:

| 文档 | 内容 | 适用场景 |
|------|------|---------|
| [network.md](.claude/docs/network.md) | Ktor 网络请求、suspend vs Flow、依赖注入 | 编写 API、处理响应 |
| [dependency-injection.md](.claude/docs/dependency-injection.md) | Koin 依赖注入、ViewModel 注入 | 添加依赖、配置 DI |
| [datastore.md](.claude/docs/datastore.md) | Proto DataStore、Flow 操作 | 存储用户偏好 |
| [modules.md](.claude/docs/modules.md) | 模块化设计、AppInitializer、CacheManager | 了解项目结构 |
| [build-publish.md](.claude/docs/build-publish.md) | 构建命令、环境配置、模块发布 | 构建 APK、发布 |

## 快速参考

### 常用构建命令
```bash
./android_build.sh dev      # Debug 构建
./android_build.sh build    # Release 构建
./android_build.sh all      # 清理+构建+安装
./gradlew test              # 运行测试
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