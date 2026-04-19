# 构建和发布配置

## 构建命令

### 基础构建
```bash
# 编译 Debug APK
./gradlew assembleDebug
./android_build.sh dev

# 编译 Release APK
./gradlew assembleRelease
./android_build.sh build

# 清理构建
./gradlew clean
./android_build.sh clean

# 完整构建流程(清理、构建、安装)
./android_build.sh all

# 安装 Release APK
adb install app/build/outputs/apk/release/app-release.apk
./android_build.sh install

# 查看构建脚本帮助
./android_build.sh help
```

### 质量与调试
```bash
./gradlew lint                   # Lint 检查
./gradlew lintFix                # 自动修复
./gradlew clean --no-daemon      # build-logic 修改后需要完整清理
adb logcat | grep "YkQuickDev"   # 查看应用日志
```

### 依赖管理
```bash
# 检查依赖更新
./gradlew dependencyUpdates
./android_build.sh dependency
```

### 测试

测试命令详见 [testing.md](testing.md)

```bash
# 打印证书签名信息
./gradlew signingReport
```

## 环境要求

| 配置项 | 值 |
|--------|-----|
| AGP 版本 | 9.1.1 |
| JDK 版本 | 17 |
| Gradle 版本 | 9.3.0+ |
| 编译 SDK | 36 (Android 15) |
| 最低 SDK | 26 (Android 8.0) |
| Kotlin 版本 | 2.3.20 |
| KSP 版本 | 2.3.6 |

**Convention Plugins**: 详见 [build-logic.md](build-logic.md)

**兼容性**: Kotlin<2.3.0 不支持 Explicit Backing Fields | AGP<8.13.0 KSP 可能失败 | Room 3.0 命名空间为 `androidx.room3`（非 `androidx.room`）

## 必需配置文件

### android_env.json
应用配置数据:
```json
{
  "base_url": "https://api.example.com"
}
```

### keystore.properties
签名配置:
```properties
storeFile=path/to/keystore.jks
keyAlias=your_alias
keyPassword=your_key_password
storePassword=your_store_password
```

## 核心配置文件

- **gradle/libs.versions.toml** - 版本目录，统一管理所有依赖版本和插件
- **settings.gradle.kts** - 项目模块配置
- **build.gradle.kts (root)** - 根项目构建配置，包含强制依赖版本设置
- **android_build.sh** - 便捷构建脚本

## app/build.gradle.kts 关键配置

- **版本号生成**: `gitVersionCode()` 通过 Git commit 计数生成(基础值 4645)
- **构建时间注入**: `manifestPlaceholders["debug_time"]` 记录打包时间
- **JSON 配置注入**: `buildConfigField("String", "YK_CONFIG", ...)` 将 android_env.json 注入到 BuildConfig
- **Wire 配置**: 由 `ykmagic.android.wire` Convention Plugin 管理，proto 文件位于 `src/main/protos`
- **Room Schema**: 由 `ykmagic.android.room` Convention Plugin 管理，schema 导出到 `$projectDir/schemas`

## 依赖版本管理

- 所有版本在 `gradle/libs.versions.toml` 中集中管理
- 支持 Bundle 依赖配置: `network-ktor`, `testBundle`, `androidTestBundle`

## Debug 工具

仅 Debug 版本启用:
- **Chucker**: 网络请求可视化抓包工具
- **LeakCanary**: 内存泄漏检测
- **Glance**: 性能监控工具

使用 `BuildConfig.DEBUG` 控制调试功能开关。

## 模块发布

各模块配置了 Maven 发布,可以发布到 JitPack:
- groupId: com.github.yikwing.ykmagic
- artifactId: 对应模块名(config、network、proxy、extension、permission、compose)

### 发布配置
每个模块的 build.gradle.kts:
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
```gradle
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.yikwing.ykmagic:config:版本号")
    implementation("com.github.yikwing.ykmagic:network:版本号")
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
3. 配置模块的包名、依赖等（现有模块参见 [modules.md](modules.md)）
4. 如需发布,添加 maven-publish 配置

## 常见问题

| 错误 | 解决 |
|------|------|
| `YK_CONFIG 未定义` | 创建 android_env.json |
| `Keystore not found` | 配置 keystore.properties |
| KSP 生成失败 | `./android_build.sh clean` |
| `No Koin context` | 检查 `@KoinApplication` 注解 |
| 网络请求失败 | 检查 android_env.json 中的 base_url |
| ViewModel 注入失败 | 添加 `@KoinViewModel` 注解 |
| build-logic 修改不生效 | 运行 `./gradlew clean --no-daemon` 清理缓存 |
| Convention Plugin 编译错误 | 检查 `VersionCatalogsExtension` 访问方式，确保 build-logic/settings.gradle.kts 正确配置 |
| HorizontalPager 无法滑动 | 确保子项使用 `fillMaxSize()` 占满区域 |
| 依赖版本冲突 | 检查根 build.gradle.kts 中 `resolutionStrategy.force()` 配置 |

