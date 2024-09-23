package com.yikwing.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.Size
import okio.Buffer
import okio.sink
import java.io.File

/**
 * 获取原始图片的长宽
 */
fun getBitmapSize(filePath: String): Size {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true // 只加载图片的边界，不加载具体像素
    BitmapFactory.decodeFile(filePath, options)

    return Size(options.outWidth, options.outHeight)
}

/**
 * 根据分辨率压缩图片比例
 */
fun compressByResolution(
    filePath: String,
    targetWidth: Int = 960,
    targetHeight: Int = 1280,
): Bitmap {
    val options = BitmapFactory.Options()
    val originSize = getBitmapSize(filePath)

    val widthScale = originSize.width.toFloat() / targetWidth
    val heightScale = originSize.height.toFloat() / targetHeight

    // 选择较大的缩放比例以保持图片比例不失真
    var scale = if (widthScale > heightScale) widthScale else heightScale

    if (scale < 1) {
        scale = 1f
    }

    Log.i("compressByResolution", "图片分辨率压缩比例：$scale")
    options.inSampleSize = scale.toInt() // 设置缩放比例
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeFile(filePath, options)
}

/**
 * 图片压缩（基于分辨率和质量的压缩，使用 Okio 优化文件操作）
 */
fun compressBitmap(
    filePath: String,
    maxSizeKB: Int,
    savePath: String,
): Boolean {
    var compressCount = 0

    // 基础分辨率压缩
    val bitmap = compressByResolution(filePath, 960, 1280)

    // 使用 Okio 进行流操作
    val buffer = Buffer()
    var quality = 100

    // 质量压缩
    bitmap.compress(Bitmap.CompressFormat.WEBP, quality, buffer.outputStream())
    Log.i("compressBitmap", "初次分辨率压缩后：" + buffer.size / 1024 + "KB")

    // 循环压缩直到满足文件大小要求
    while (buffer.size > maxSizeKB * 1024) {
        compressCount++

        val subtract =
            when {
                buffer.size > 5000 * 1024 && compressCount == 1 -> 50
                buffer.size > 1000 * 1024 && compressCount == 2 -> 20
                else -> 10
            }

        quality -= subtract

        buffer.clear() // 清空 buffer
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, buffer.outputStream())

        Log.i("compressBitmap", "第${compressCount}次压缩后大小：" + buffer.size / 1024 + "KB")
    }

    Log.i("compressBitmap", "图片压缩完成，最终大小：" + buffer.size / 1024 + "KB")

    // 将压缩后的图片写入到文件，使用 Okio Sink
    return try {
        val outputFile = File(savePath)
        outputFile.sink().use { sink ->
            buffer.readAll(sink) // 将 buffer 写入文件
        }
        bitmap.recycle() // 回收 bitmap 资源
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
