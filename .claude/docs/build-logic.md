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

| 插件 | 用途 | 自动添加依赖 |
|------|------|------------|
| `ykmagic.android.application` | App 模块（compileSdk=36, minSdk=26, Java17） | core-ktx, appcompat, lifecycle-runtime-ktx, coroutines, testBundle, androidTestBundle |
| `ykmagic.android.library` | Library 模块（同 SDK 版本） | 同 application |
| `ykmagic.android.compose` | Jetpack Compose | compose-bom(platform), material3, ui-tooling-preview, ui-test-junit4, ui-tooling(debug) |
| `ykmagic.android.koin` | Koin 注入 + 编译期依赖检查（userLogs 开启） | koin-bom(platform), koin-compose, koin-annotations |
| `ykmagic.android.room` | Room 数据库（schema→`$projectDir/schemas`） | room3-runtime, room3-compiler(ksp) |
| `ykmagic.android.wire` | Wire Protobuf（proto→`src/main/protos`, `android=false`） | — |

> JVM Toolchain 17 由 Application/Library 插件内部配置，无需额外声明。

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