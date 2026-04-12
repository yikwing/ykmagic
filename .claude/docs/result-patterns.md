# Result 错误处理模式

类似 Go 语言的 `(value, error)` 返回模式，使用 Kotlin 标准库的 `Result<T>` 替代抛异常或返回可空类型。

## 为什么用 Result

| 方式 | 问题 |
|------|------|
| 抛异常 | 调用方容易忘记处理，运行时崩溃 |
| 返回 `T?` | 无法区分"无数据"和"出错"，丢失错误信息 |
| 自定义 `Pair<Boolean, T?>` | 冗余，类型不安全 |
| `Result<T>` | 强制处理成功/失败，保留异常信息 |

## 创建 Result

```kotlin
// 使用 runCatching 自动捕获异常
fun copyFile(src: File, dst: File): Result<File> = runCatching {
    src.copyTo(dst, overwrite = true)
}

// 手动创建
Result.success(file)
Result.failure(IOException("文件不存在"))
```

## 消费 Result

```kotlin
val result = copyAssetToCache(context, "config.json")

// 方式1: 链式处理（推荐）
result
    .onSuccess { file -> Log.d("Copy", "路径: ${file.absolutePath}") }
    .onFailure { e -> Log.e("Copy", "失败: ${e.message}") }

// 方式2: 获取值
val file = result.getOrNull()           // 失败返回 null
val fileOrDefault = result.getOrDefault(fallbackFile)
val fileOrThrow = result.getOrThrow()   // 失败抛异常

// 方式3: fold 模式匹配
val message = result.fold(
    onSuccess = { "保存到: ${it.absolutePath}" },
    onFailure = { "错误: ${it.message}" }
)

// 方式4: 检查状态
if (result.isSuccess) { /* ... */ }
if (result.isFailure) { /* ... */ }
result.exceptionOrNull()  // 获取异常，成功返回 null
```

## 链式转换

```kotlin
copyAssetToCache(context, "data.json")
    .map { file -> file.readText() }           // 成功时转换
    .mapCatching { json -> Json.parse(json) }  // 转换可能抛异常
    .recover { e -> "默认值" }                  // 失败时恢复
    .getOrNull()
```

## 适用场景

- IO 操作（文件读写、网络请求）
- 解析操作（JSON、XML）
- 任何可能失败的操作

## 项目示例

```kotlin
// module_extension/io/AssetsExtensions.kt
fun copyAssetToCache(context: Context, fileName: String): Result<File> =
    runCatching {
        val cacheFile = File(context.cacheDir, fileName)
        context.assets.open(fileName).source().buffer().use { source ->
            cacheFile.sink().buffer().use { sink ->
                sink.writeAll(source)
            }
        }
        cacheFile
    }
```
