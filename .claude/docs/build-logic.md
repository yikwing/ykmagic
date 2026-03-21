# Build Logic 构建逻辑

本项目采用 **Now in Android** 风格的 Convention Plugins 模式管理构建配置。

## 📁 目录结构

```
build-logic/
├── settings.gradle.kts          # build-logic 模块配置
├── build.gradle.kts             # 顶层构建文件
└── convention/                  # Convention Plugins 模块
    ├── build.gradle.kts         # 插件定义和注册
    └── src/main/kotlin/
        ├── AndroidApplicationConventionPlugin.kt
        ├── AndroidLibraryConventionPlugin.kt
        ├── KotlinAndroidConventionPlugin.kt
        └── ComposeConventionPlugin.kt
```

## 🎯 Convention Plugins

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

### 3. `ykmagic.kotlin.android`
**用途**: 配置 Kotlin Android 编译选项

**自动应用**:
- `org.jetbrains.kotlin.android` 插件

**配置内容**:
- JVM Toolchain 17
- JVM Target 17

**使用示例**:
```kotlin
plugins {
    id("ykmagic.kotlin.android")
    // ... 其他插件
}
```

### 4. `ykmagic.android.compose`
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

## 📝 使用指南

### App 模块示例

```kotlin
plugins {
    // Convention Plugins
    id("ykmagic.android.application")
    id("ykmagic.kotlin.android")
    id("ykmagic.android.compose")

    // 其他插件
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
    alias(libs.plugins.koin.compiler)
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
    id("ykmagic.kotlin.android")

    // 其他插件
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
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

## ✅ 优势

1. **集中管理**: 所有通用配置集中在 build-logic 中
2. **避免重复**: 不需要在每个模块重复配置 SDK 版本、Java 版本等
3. **类型安全**: 使用 Kotlin DSL，编译时检查
4. **易于维护**: 修改一处，所有模块生效
5. **模块化**: 每个 Convention Plugin 职责单一
6. **可测试**: Convention Plugins 可以独立测试

## 🔧 修改配置

### 修改 SDK 版本

编辑 `build-logic/convention/src/main/kotlin/ProjectConfig.kt`:

```kotlin
object ProjectConfig {
    const val COMPILE_SDK = 37  // 修改这里
    const val MIN_SDK = 26
    const val TARGET_SDK = 37   // 修改这里

    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}
```

所有 Convention Plugins 会自动使用新的配置。

### 修改 Java 版本

编辑 `ProjectConfig.kt`：

```kotlin
object ProjectConfig {
    // ...
    val JAVA_VERSION = JavaVersion.VERSION_21
    val JVM_TARGET = JvmTarget.JVM_21
}
```

### 添加新的 Convention Plugin

1. 在 `build-logic/convention/src/main/kotlin/` 创建新的插件文件
2. 在 `build-logic/convention/build.gradle.kts` 注册插件：

```kotlin
gradlePlugin {
    plugins {
        register("yourPlugin") {
            id = "ykmagic.your.plugin"
            implementationClass = "YourConventionPlugin"
        }
    }
}
```

## 📚 参考

- [Now in Android - build-logic](https://github.com/android/nowinandroid/tree/main/build-logic)
- [Gradle Convention Plugins](https://docs.gradle.org/current/samples/sample_convention_plugins.html)
- [Sharing Build Logic](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)

## ⚠️ 注意事项

1. **插件顺序**: Convention Plugins 应该放在 `plugins` 块的最前面
2. **不要重复应用**: Convention Plugins 已经应用了基础插件（如 `android.application`），不要再次声明
3. **Gradle 同步**: 修改 build-logic 后需要重新同步 Gradle
4. **缓存清理**: 如果遇到问题，尝试 `./gradlew clean --no-daemon`