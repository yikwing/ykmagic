# YkMagic

Android 快速开发框架库，提供网络请求、配置管理、权限处理等常用模块的封装。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 2.3.20 |
| 构建 | Gradle + KSP | 9.3.0 |
| UI | Jetpack Compose | BOM 2026.03.00 |
| 网络 | Ktor Client | 3.4.1 |
| DI | Koin | 4.2.0 |
| 数据库 | Room | 3.0.0-alpha01 |
| 导航 | Navigation 3 | 1.0.1 |
| 序列化 | Kotlinx Serialization | 1.10.0 |

> 以 `gradle/libs.versions.toml` 为准

## 模块

| 模块 | 功能 |
|------|------|
| **module_config** | JSON 配置注入，维护 Application Context |
| **module_network** | Ktor Client 封装，拦截器注入，Debug 网络抓包 |
| **module_extension** | Kotlin 扩展函数 / 工具类集合 |
| **module_datastore** | DataStore 扩展封装 |
| **module_permission** | 基于 Fragment 的统一权限请求 |
| **module_proxy** | BaseActivity / BaseFragment / AppInitializer |
| **module_component** | 自定义 View 组件 |

## 使用

1. 添加 JitPack 仓库

   ```kotlin
   maven { url = uri("https://jitpack.io") }
   ```

2. 按需引入依赖

   ```kotlin
   val ykmagicVersion = "latest"

   implementation("com.github.yikwing.ykmagic:config:$ykmagicVersion")
   implementation("com.github.yikwing.ykmagic:network:$ykmagicVersion")
   implementation("com.github.yikwing.ykmagic:extension:$ykmagicVersion")
   implementation("com.github.yikwing.ykmagic:datastore:$ykmagicVersion")
   implementation("com.github.yikwing.ykmagic:permission:$ykmagicVersion")
   implementation("com.github.yikwing.ykmagic:proxy:$ykmagicVersion")
   implementation("com.github.yikwing.ykmagic:component:$ykmagicVersion")
   ```

## 构建

```bash
./android_build.sh dev         # Debug 构建
./android_build.sh build       # Release 构建
./android_build.sh clean       # 清理
./gradlew test                 # 单元测试
```

## Tips

- `sh/` 目录配置了签名 / 校验签名脚本
- 必需配置文件：`android_env.json`（运行时配置）、`keystore.properties`（Release 签名）

## 鸣谢

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个在各个方面都最大程度地提高开发人员的生产力的
> IDE，适用于 JVM 平台语言。

特别感谢 [JetBrains](https://www.jetbrains.com/?from=campus)
为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea) 等 IDE 的授权
[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="200"/>](https://www.jetbrains.com)
