package com.yikwing.extension

import android.content.Context
import android.util.Log
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.InputStream

/**
 * 从 assets 复制文件到 cache 目录
 *
 * @param context 上下文对象
 * @param fileName 需要复制的 assets 文件名
 * @return 复制后的文件在 cache 目录中的路径
 */
fun copyAssetToCache(
    context: Context,
    fileName: String,
): File? =
    try {
        // 获取 assets 文件的输入流
        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open(fileName)

        // 创建 cache 目录中的目标文件
        val cacheFile = File(context.cacheDir, fileName)

        // 使用 Okio 将文件复制到 cache 目录
        cacheFile.sink().buffer().use { sink ->
            inputStream.source().buffer().use { source ->
                sink.writeAll(source)
            }
        }

        Log.i("CopyAsset", "文件已复制到: ${cacheFile.absolutePath}")
        cacheFile // 返回复制后的文件
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("CopyAsset", "复制文件失败: ${e.message}")
        null
    }
