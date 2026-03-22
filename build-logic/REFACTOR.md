# build-logic 重构说明

## 📦 新的包结构

```
build-logic/convention/src/main/kotlin/
├── com.yikwing.ykmagic/          # 新增包结构
│   ├── ProjectExtensions.kt      # Project.libs 扩展属性
│   ├── KotlinAndroid.kt          # Kotlin Android 配置
│   ├── AndroidCompose.kt         # Compose 配置
│   └── Dependencies.kt           # 依赖管理
├── AndroidApplicationConventionPlugin.kt
├── AndroidLibraryConventionPlugin.kt
├── AndroidFeatureConventionPlugin.kt  # 新增
├── ComposeConventionPlugin.kt
├── KotlinAndroidConventionPlugin.kt   # 已废弃
└── ProjectConfig.kt
```

## ✨ 新增功能

### 1. Project.libs 扩展属性

简化版本目录访问：

```kotlin
// 之前
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val library = libs.findLibrary("someLibrary").get()

// 现在
val library = libs.findLibrary("someLibrary").get()
```

### 2. AndroidFeatureConventionPlugin

统一 Feature 模块配置：

```kotlin
plugins {
    id("ykmagic.android.feature")
}

// 自动应用：
// - ykmagic.android.library
// - ykmagic.android.compose
// - 通用模块依赖（extension, network, config）
// - Koin 依赖注入
// - Navigation3 导航
// - Lifecycle 组件
```

### 3. 统一配置函数

**configureKotlinAndroid**：
- 配置 compileSdk / minSdk
- 启用 BuildConfig
- 配置 Java 17
- 配置 Kotlin 编译选项（实验性 API、Explicit Backing Fields）

**configureAndroidCompose**：
- 启用 Compose
- 添加 Compose BOM
- 配置调试和测试工具

## 🔄 迁移指南

### 使用 Feature 插件

**之前**：
```kotlin
plugins {
    id("ykmagic.android.library")
    id("ykmagic.android.compose")
}

dependencies {
    implementation(project(":module_extension"))
    implementation(project(":module_network"))
    implementation(project(":module_config"))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    // ...
}
```

**现在**：
```kotlin
plugins {
    id("ykmagic.android.feature")
}

// 所有通用依赖自动添加
```

### 移除 kotlin.android 插件

`ykmagic.kotlin.android` 已删除，Kotlin 配置已集成到 `androidLibrary` 和 `androidApplication` 中。

**迁移步骤**：
```kotlin
// ✅ 已自动移除
// id("ykmagic.kotlin.android")

// 保留
id("ykmagic.android.library")
// 或
id("ykmagic.android.application")
```

### BuildConfig 配置

**Application 模块**：自动启用 BuildConfig
```kotlin
plugins {
    id("ykmagic.android.application")
}
// BuildConfig 自动启用
```

**Library 模块**：默认不启用，按需显式启用
```kotlin
plugins {
    id("ykmagic.android.library")
}

// 如果需要 BuildConfig
android {
    buildFeatures {
        buildConfig = true
    }
}
```

## 📋 可用插件

| 插件 ID | 用途 | BuildConfig | 自动配置 |
|---------|------|-------------|----------|
| `ykmagic.android.application` | 应用模块 | ✅ 默认启用 | Kotlin + 通用依赖 |
| `ykmagic.android.library` | 库模块 | ❌ 默认不启用 | Kotlin + 通用依赖 |
| `ykmagic.android.compose` | Compose UI | - | Compose BOM + 工具 |
| `ykmagic.android.feature` | Feature 模块 | ❌ 默认不启用 | Library + Compose + 框架依赖 |

## 🎯 最佳实践

### Feature 模块

```kotlin
// feature_home/build.gradle.kts
plugins {
    id("ykmagic.android.feature")
}

// 自动添加：Koin、Navigation3、Lifecycle

// 根据需要添加项目依赖
dependencies {
    implementation(project(":module_extension"))
    implementation(project(":module_network"))
    implementation(libs.coil.compose)
}
```

### 普通库模块

```kotlin
// module_utils/build.gradle.kts
plugins {
    id("ykmagic.android.library")
}

dependencies {
    // 特定依赖
}
```

### 应用模块

```kotlin
// app/build.gradle.kts
plugins {
    id("ykmagic.android.application")
    id("ykmagic.android.compose")
}
```

## 🔧 技术细节

### 编译选项

所有模块自动启用：
- Java 17 兼容性
- Kotlin JVM 17 目标
- 实验性协程 API (`-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi`)
- Explicit Backing Fields (`-XXLanguage:+ExplicitBackingFields`)

### BuildConfig 配置

- **Application 模块**：默认启用
- **Library 模块**：默认不启用，按需在模块中显式启用
- **Feature 模块**：默认不启用，按需在模块中显式启用

### 通用依赖

自动添加到所有 Android 模块：
- `core-ktx`
- `appcompat`
- `kotlinx-coroutines-android`
- 测试依赖（testBundle, androidTestBundle）

### Compose 依赖

应用 `android.compose` 插件自动添加：
- Compose BOM
- Material3
- UI Tooling
- 测试工具

### Feature 模块依赖

应用 `android.feature` 插件自动添加：
- Koin 依赖注入（android, compose, viewmodel）
- Navigation3 Compose
- Lifecycle Compose（runtime, viewmodel）

项目特定依赖需在各 Feature 模块自行添加。

## 📚 参考

- [build-logic.md](.claude/docs/build-logic.md) - 完整文档
- [CoolMallKotlin](https://github.com/joker-xii/CoolMallKotlin) - 参考项目
