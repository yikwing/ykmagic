# YkMagic 项目概述

## 基本信息
- **项目名称**: YkMagic (YkQuickDev)
- **类型**: Android 快速开发框架库
- **包名**: com.yikwing.ykquickdev
- **当前分支**: compose

## 技术栈
- **语言**: Kotlin 2.3.20
- **构建**: AGP 9.1.1 + Gradle 9.3.0 + KSP 2.3.6
- **JDK**: 17
- **SDK**: compileSdk 36, minSdk 26, targetSdk 36

## 核心依赖
| 类别 | 库 | 版本 |
|------|-----|------|
| UI | Jetpack Compose | BOM 2026.03.01 |
| 网络 | Ktor Client (CIO) | 3.4.2 |
| DI | Koin Annotations | BOM 4.2.1 |
| 协程 | Kotlinx Coroutines | 1.10.2 |
| 序列化 | Kotlinx Serialization | 1.11.0 |
| 数据库 | Room | 3.0.0-alpha03 (androidx.room3) |
| 导航 | Navigation 3 | 1.1.0 |
| 图片 | Coil | 3.4.0 |
| Proto | Wire | 6.2.0 |

## 模块
app | module_config | module_network | module_extension | module_compose | module_permission | module_proxy

## 文档
`.claude/docs/` 下 10 个专题文档，`CLAUDE.md` 为入口索引。