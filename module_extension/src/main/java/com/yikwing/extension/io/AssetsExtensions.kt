package com.yikwing.extension.io

import android.content.Context
import okio.buffer
import okio.sink
import okio.source
import java.io.File

/**
 * 从 assets 复制文件到 cache 目录
 *
 * @param context 上下文对象
 * @param fileName 需要复制的 assets 文件名
 * @return Result<File> 成功返回文件，失败返回异常
 *
 * 使用示例:
 * ```
 * copyAssetToCache(context, "config.json")
 *     .onSuccess { file -> Log.d("Copy", "路径: ${file.absolutePath}") }
 *     .onFailure { e -> Log.e("Copy", "失败: ${e.message}") }
 *
 * // 或者
 * val file = copyAssetToCache(context, "config.json").getOrNull()
 * ```
 */
fun copyAssetToCache(
    context: Context,
    fileName: String,
): Result<File> =
    runCatching {
        val cacheFile = File(context.cacheDir, fileName)
        context.assets.open(fileName).source().buffer().use { source ->
            cacheFile.sink().buffer().use { sink ->
                sink.writeAll(source)
            }
        }
        cacheFile
    }
