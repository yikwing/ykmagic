# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

YkQuickDev 是一个 Android 快速开发框架库，提供了多个可独立使用的功能模块，用于加速 Android 应用开发。

**技术栈**:
- Kotlin + Coroutines
- Gradle 版本目录 (libs.versions.toml) 统一管理依赖
- JDK 21 / Kotlin 2.2.20
- Hilt 依赖注入 + KSP 注解处理
- Jetpack Compose UI
- 支持通过 JitPack (com.github.yikwing.ykmagic:模块名:版本号) 或本地模块依赖

## 构建和运行命令

### 基础构建命令
```bash
# 编译 Debug APK
./gradlew assembleDebug
# 或使用构建脚本
./android_build.sh dev

# 编译 Release APK
./gradlew assembleRelease
# 或使用构建脚本
./android_build.sh build

# 清理构建
./gradlew clean
# 或使用构建脚本
./android_build.sh clean

# 完整构建流程（清理、构建、安装）
./android_build.sh all

# 安装 Release APK
adb install app/build/outputs/apk/release/app-release.apk
# 或使用构建脚本
./android_build.sh install

# 查看构建脚本帮助
./android_build.sh help
```

### 依赖管理
```bash
# 检查依赖更新
./gradlew dependencyUpdates
# 或使用构建脚本
./android_build.sh dependency
```

### 测试相关
```bash
# 运行单元测试
./gradlew test

# 运行指定模块的单元测试
./gradlew :module_config:test

# 运行 Android 仪器测试
./gradlew connectedAndroidTest

# 运行单个测试类
./gradlew test --tests "com.yikwing.config.ReturnsTest"

# 打印证书签名信息
./gradlew signingReport
```

## 核心架构

### 模块化设计
项目采用多模块架构，每个模块独立负责特定功能，可以按需引入：

1. **module_config** - 配置注入模块
   - 通过 `android_env.json` 注入配置数据
   - 维护全局 Application Context
   - 使用 KSP 处理注解 `@YkConfigNode` 和 `@YkConfigValue`

2. **module_network** - 网络请求模块
   - 基于 OkHttp + Retrofit 封装
   - 提供统一的拦截器机制（HeaderInterceptor、RetryInterceptor、LogInterceptor）
   - 支持 Debug 模式网络抓包视图（Chucker）
   - 集成 Moshi 进行 JSON 序列化
   - 支持 IgnoreHttpResult 灵活解析响应

3. **module_extension** - 扩展方法模块
   - 提供各类 Kotlin 扩展函数和工具类
   - 包含协程工具、EventBus、图片压缩、日期处理等
   - GlobalContextProvider 提供全局 Context 访问
   - 资源扩展函数简化资源获取

4. **module_datastore** - DataStore 封装
   - 基于 Jetpack DataStore Preferences
   - 提供属性委托方式的便捷访问

5. **module_permission** - 权限请求模块
   - 基于 Fragment 封装统一的权限请求流程
   - 简化权限申请逻辑

6. **module_logger** - 日志组件
   - 基于 Logger 库的统一日志组件
   - 支持自定义格式化策略

7. **module_proxy** - 基础组件模块
   - 提供 BaseActivity、BaseFragment、LazyFragment
   - ActivityHierarchyManager 管理 Activity 栈
   - AppInitializer 支持模块化初始化

8. **module_component** - UI 组件模块
   - RoundedImageView：支持独立设置各角圆角半径
   - ImBarWrapperView：使用 WindowInsets 处理状态栏高度

### 初始化架构 (AppInitializer)

**核心机制**: 基于拓扑排序的模块初始化框架，支持声明依赖关系。

**使用方式**:
```kotlin
// 在 Application.onCreate() 中
AppInitializer.getInstance(this)
    .addTask(ConfigInjectInitTask())
    .addTask(LoggerInitTask())
    .addTask(NetworkInitTask())
    .addTask(DataStoreInitTask())
    .build(debug = true)
```

**创建初始化任务**:
```kotlin
class ConfigInjectInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        // 初始化代码
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(
        LoggerInitTask::class.java  // 声明依赖
    )
}
```

**关键特性**:
- 自动根据依赖关系排序执行
- 循环依赖检测（会抛出 "存在回环依赖" 错误）
- 位置: module_proxy/src/main/java/com/yikwing/proxy/startup/AppInitializer.kt:28

### 配置注入机制 (module_config)

**流程**: android_env.json → BuildConfig.YK_CONFIG → YkQuickManager.setUp() → KSP 生成代码

**使用步骤**:
1. 在根目录创建 `android_env.json` 配置文件
2. 使用注解标记配置类:
   ```kotlin
   @YkConfigNode
   @JsonClass(generateAdapter = true)
   data class NetworkConfig(
       @YkConfigValue(path = "base_url") val baseUrl: String
   )
   ```
3. KSP 会自动生成 YkQuickManager 和相关配置代码
4. 在 Application 中调用 `YkQuickManager.setUp(BuildConfig.YK_CONFIG)`

**构建时注入**: app/build.gradle.kts 中通过 `buildConfigField` 将 JSON 注入到 BuildConfig

### 依赖注入架构
- 使用 Hilt 进行依赖注入 (@HiltAndroidApp 标记 Application)
- 通过 KSP 处理注解，生成代码
- 支持 ViewModel + Repository + Hilt 的完整依赖链

### Compose UI 架构
- 主应用使用 Jetpack Compose 构建 UI
- Navigation Compose 进行页面导航
- Hilt Navigation Compose 支持 ViewModel 注入
- Coil 3 处理图片加载（在 MainApplication 中配置 SingletonImageLoader）

### Activity 生命周期管理

**ActivityHierarchyManager**: 维护 Activity 栈的全局管理器
- 在 Application 中注册 ActivityLifecycleCallbacks
- 自动跟踪 Activity 的创建和销毁
- 提供 `printActivityHierarchy()` 打印当前 Activity 栈（debug 模式）
- 位置: module_proxy/src/main/java/com/yikwing/proxy/util/ActivityHierarchyManager.kt

**使用示例** (在 MainApplication.kt 中):
```kotlin
class AppActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks by noOpDelegate() {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ActivityHierarchyManager.register(activity)
        ActivityHierarchyManager.printActivityHierarchy(BuildConfig.DEBUG)
    }

    override fun onActivityDestroyed(activity: Activity) {
        ActivityHierarchyManager.unregister(activity)
    }
}
```

### WorkManager 任务调度

项目使用 WorkManager 进行后台任务管理，例如缓存清理：
- **CleanCacheWork**: 定时清理缓存任务
- 配置为每天执行一次，仅在设备充电且电量充足时运行
- 位置: app/src/main/java/com/yikwing/ykquickdev/work/CleanCacheWork.kt

## 重要配置文件

### 必需配置（需手动创建）
1. **android_env.json** - 应用配置数据，示例结构：
   ```json
   {
     "base_url": "https://api.example.com"
   }
   ```

2. **keystore.properties** - 签名配置，示例结构：
   ```properties
   storeFile=path/to/keystore.jks
   keyAlias=your_alias
   keyPassword=your_key_password
   storePassword=your_store_password
   ```

### 核心配置文件
- **gradle/libs.versions.toml** - 版本目录，统一管理所有依赖版本和插件
- **settings.gradle.kts** - 项目模块配置，定义所有 include 的模块
- **build.gradle.kts (root)** - 根项目构建配置，包含强制依赖版本设置
- **android_build.sh** - 便捷构建脚本，支持 dev/build/clean/install/dependency 等命令

### app/build.gradle.kts 关键配置
- **版本号生成**: `gitVersionCode()` 函数通过 Git commit 计数生成
- **构建时间注入**: `manifestPlaceholders["debug_time"]` 记录打包时间
- **JSON 配置注入**: `buildConfigField("String", "YK_CONFIG", ...)` 将 android_env.json 内容注入到 BuildConfig
- **资源重定向**: `sourceSets.getByName("main") { res.setSrcDirs(...) }` 支持多资源目录
- **Wire 配置**: Protobuf 支持，proto 文件位于 `src/main/protos`
- **Room Schema**: KSP 参数配置 Room 数据库 schema 导出位置

## 开发注意事项

### 环境要求
1. **JDK 版本**: 项目使用 JDK 21，确保环境配置正确
2. **编译 SDK**: compileSdk = 36 (Android 15)
3. **最低 SDK**: minSdk = 26 (Android 8.0)
4. **Kotlin 版本**: 2.2.20
5. **KSP 版本**: 2.2.20-2.0.4

### 构建配置
1. **版本号管理**: versionCode 通过 Git commit 数量自动生成（基础值 4645）
2. **签名配置**: Release 和 Debug 都使用同一签名文件（配置在 keystore.properties）
3. **资源重定向**: app 模块的资源文件按功能分类存放在不同子目录：
   - `src/main/res/common` - 通用资源
   - `src/main/res/activity` - Activity 相关资源
   - `src/main/res/fragment` - Fragment 相关资源

### 依赖版本管理
- 项目在根 build.gradle.kts 中通过 `resolutionStrategy.force()` 强制统一关键依赖版本
- 强制版本包括: `activity` 和 `kotlinx-coroutines-core`
- 所有版本在 `gradle/libs.versions.toml` 中集中管理
- 支持 Bundle 依赖配置，如: `network-okhttp`, `network-ktor`, `testBundle`, `androidTestBundle`

### Debug 工具
- **Chucker**: 网络请求可视化抓包工具（仅 Debug 版本）
- **LeakCanary**: 内存泄漏检测（仅 Debug 版本）
- **Glance**: 性能监控工具（仅 Debug 版本）
- 使用 `BuildConfig.DEBUG` 控制调试功能的开关

### KSP 注解处理
- module_config: 处理 `@YkConfigNode` 和 `@YkConfigValue`
- Moshi: 使用 `@JsonClass(generateAdapter = true)` 生成 JSON 适配器
- Hilt: 使用 `@HiltAndroidApp`, `@AndroidEntryPoint` 等注解
- Room: 使用 `@Entity`, `@Dao`, `@Database` 等注解

## 模块发布

各模块配置了 Maven 发布，可以发布到 JitPack：
- groupId: com.github.yikwing.ykmagic
- artifactId: 对应模块名（config、network、proxy、extension、permission、logger、datastore、component）
- 当前版本: 查看各模块的 build.gradle.kts 中的 version 配置（例如 1.0.1）

### 发布配置说明
每个模块的 build.gradle.kts 包含：
```kotlin
android {
    publishing {
        singleVariant("release") {}
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "模块名"
                version = "版本号"
                from(components["release"])
            }
        }
    }
}
```

### 使用已发布的模块
在项目中添加 JitPack 仓库和依赖：
```gradle
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.yikwing.ykmagic:config:版本号")
    implementation("com.github.yikwing.ykmagic:network:版本号")
    // 其他模块...
}
```

## 常见任务

### 添加新的依赖
1. 在 `gradle/libs.versions.toml` 的 [versions] 部分添加版本号
2. 在 [libraries] 部分添加依赖声明
3. 在需要的模块 build.gradle.kts 中引用

### 创建新模块
1. 在 settings.gradle.kts 添加 `include(":module_name")`
2. 创建模块目录和 build.gradle.kts
3. 配置模块的包名、依赖等
4. 如需发布，添加 maven-publish 配置

### 调试网络请求
Debug 模式下会自动添加网络日志拦截器，可以在 Logcat 中查看请求详情。Debug 版本集成 Chucker 可视化抓包工具。

### 依赖配置说明
- 项目强制指定统一的 activity 和 kotlinx-coroutines-core 版本
- 支持 Bundle 依赖，如 network-okhttp、network-ktor、testBundle 等
- Debug 和 Release 构建分别使用不同的依赖（如 Chucker、LeakCanary）