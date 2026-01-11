# module_config

配置管理模块，使用 kotlinx.serialization 静态解析 JSON 配置。

## 使用方式

### 1. 定义配置类

```kotlin
@Serializable
data class AppConfig(
    @SerialName("base_url") val baseUrl: String,
)
```

### 2. 初始化

在 Application 启动时调用：

```kotlin
YkConfigManager.setUp(BuildConfig.YK_CONFIG)
```

### 3. 获取配置

```kotlin
val baseUrl = YkConfigManager.config.baseUrl
```

## API

| 方法/属性                 | 说明       |
|-----------------------|----------|
| `setUp(json: String)` | 初始化配置    |
| `config`              | 获取配置对象   |
| `isInitialized`       | 检查是否已初始化 |

