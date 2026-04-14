# Build Logic 构建逻辑

本项目采用 **Now in Android** 风格的 Convention Plugins 模式管理构建配置。

## 目录结构

```
build-logic/
├── settings.gradle.kts          # build-logic 模块配置
├── build.gradle.kts             # 顶层构建文件
└── convention/                  # Convention Plugins 模块
    ├── build.gradle.kts         # 插件定义和注册
    └── src/main/kotlin/
        ├── AndroidApplicationConventionPlugin.kt
        ├── AndroidLibraryConventionPlugin.kt
        ├── ComposeConventionPlugin.kt
        ├── AndroidRoomConventionPlugin.kt
        ├── AndroidKoinConventionPlugin.kt
        ├── AndroidWireConventionPlugin.kt
        ├── ProjectConfig.kt
        └── com/yikwing/ykmagic/   # 内部辅助函数
            ├── AndroidCompose.kt
            ├── Dependencies.kt
            ├── KotlinAndroid.kt
            └── ProjectExtensions.kt
```

## Convention Plugins

### 1. `ykmagic.android.application`
**用途**: 配置 Android Application 模块

**自动应用**:
- `com.android.application` 插件

**配置内容**:
- `compileSdk = 36`
- `minSdk = 26`
- `targetSdk = 36`
- Java 17 兼容性

**自动添加依赖**:
- `core-ktx`
- `appcompat`
- `lifecycle-runtime-ktx`
- `kotlinx-coroutines-android`
- `testBundle` (测试依赖)
- `androidTestBundle` (Android 测试依赖)

**使用示例**:
```kotlin
plugins {
    id("ykmagic.android.application")
    // ... 其他插件
}
```

### 2. `ykmagic.android.library`
**用途**: 配置 Android Library 模块

**自动应用**:
- `com.android.library` 插件

**配置内容**:
- `compileSdk = 36`
- `minSdk = 26`
- Java 17 兼容性

**自动添加依赖**:
- `core-ktx`
- `appcompat`
- `lifecycle-runtime-ktx`
- `kotlinx-coroutines-android`
- `testBundle` (测试依赖)
- `androidTestBundle` (Android 测试依赖)

**使用示例**:
```kotlin
plugins {
    id("ykmagic.android.library")
    // ... 其他插件
}
```

### 3. `ykmagic.android.compose`
**用途**: 配置 Jetpack Compose

**自动应用**:
- `org.jetbrains.kotlin.plugin.compose` 插件

**配置内容**:
- 启用 Compose 构建特性

**自动添加依赖**:
- `compose-bom` (platform)
- `compose-material3`
- `ui-tooling-preview`
- `ui-test-junit4` (androidTest)
- `ui-tooling` (debug)
- `ui-test-manifest` (debug)

**使用示例**:
```kotlin
plugins {
    id("ykmagic.android.compose")
    // ... 其他插件
}
```

### 4. `ykmagic.android.koin`
**用途**: 配置 Koin 依赖注入

**自动应用**:
- `io.insert-koin.compiler.plugin` 插件（Koin 编译时依赖检查）

**配置内容**:
- 启用 Koin 用户日志（`userLogs.set(true)`）

**自动添加依赖**:
- `koin-bom` (platform)
- `koin-compose`
- `koin-annotations`

**使用示例**:
```kotlin
plugins {
    id("ykmagic.android.koin")
    // ... 其他插件
}
```

### 5. `ykmagic.android.room`
**用途**: 配置 Room 数据库

**自动应用**:
- `androidx.room3` 插件
- `com.google.devtools.ksp` 插件

**配置内容**:
- Schema 导出目录: `$projectDir/schemas`

**自动添加依赖**:
- `room-ktx` (implementation)
- `room-compiler` (ksp)

**使用示例**:
```kotlin
plugins {
    id("ykmagic.android.room")
    // ... 其他插件
}
```

### 6. `ykmagic.android.wire`
**用途**: 配置 Wire Protobuf

**自动应用**:
- `com.squareup.wire` 插件

**配置内容**:
- Proto 源文件目录: `src/main/protos`
- `android = false`（Wire 消息用于 DataStore，不需要 Parcelable）

**使用示例**:
```kotlin
plugins {
    id("ykmagic.android.wire")
    // ... 其他插件
}
```

> **注意**: Kotlin 编译选项（JVM Toolchain 17）由 Application/Library 插件内部通过 `KotlinAndroid.kt` 辅助函数配置，无需单独的 `ykmagic.kotlin.android` 插件。

## 使用指南

### App 模块示例

```kotlin
plugins {
    // Convention Plugins
    id("ykmagic.android.application")
    id("ykmagic.android.compose")
    id("ykmagic.android.koin")
    id("ykmagic.android.room")
    id("ykmagic.android.wire")

    // 其他插件
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

// 不需要再配置 compileSdk, minSdk, targetSdk 等
// Convention Plugins 已经自动配置
```

### Library 模块示例

```kotlin
plugins {
    // Convention Plugins
    id("ykmagic.android.library")
    id("ykmagic.android.koin")

    // 其他插件
    id("maven-publish")
}

android {
    namespace = "com.yikwing.yourmodule"

    // 其他特定配置
    buildFeatures {
        buildConfig = true
    }
}
```

## 修改配置

| 修改项 | 文件位置 |
|--------|---------|
| SDK 版本 | `build-logic/convention/src/main/kotlin/ProjectConfig.kt` → `COMPILE_SDK` / `MIN_SDK` / `TARGET_SDK` |
| Java/JVM 版本 | `build-logic/convention/src/main/kotlin/com/yikwing/ykmagic/KotlinAndroid.kt` → `jvmToolchain()` |
| 添加新插件 | 在 `src/main/kotlin/` 创建插件类 + 在 `build.gradle.kts` 的 `gradlePlugin.plugins` 中注册 |

## 注意事项

1. **插件顺序**: Convention Plugins 应该放在 `plugins` 块的最前面
2. **不要重复应用**: Convention Plugins 已经应用了基础插件（如 `android.application`），不要再次声明
3. **Gradle 同步**: 修改 build-logic 后需要重新同步 Gradle
4. **缓存清理**: 如果遇到问题，尝试 `./gradlew clean --no-daemon`