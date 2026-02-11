package com.yikwing.extension.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Size
import okio.Buffer
import okio.buffer
import okio.sink
import java.io.File

private const val TAG = "CompressUtil"

// 默认目标分辨率（1440p）
private const val DEFAULT_TARGET_WIDTH = 1440
private const val DEFAULT_TARGET_HEIGHT = 2560

// 压缩阈值
private const val COMPRESS_THRESHOLD_5MB = 5000 * 1024L
private const val COMPRESS_THRESHOLD_1MB = 1000 * 1024L

private const val BYTES_PER_KB = 1024

/**
 * 获取图片的原始宽高
 */
fun getImageDimensions(
    context: Context,
    uri: Uri,
): Size? =
    try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        if (options.outWidth > 0 && options.outHeight > 0) {
            Log.d(TAG, "获取图片宽高: width: ${options.outWidth}, height: ${options.outHeight}")
            Size(options.outWidth, options.outHeight)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e(TAG, "获取图片宽高失败: ${e.message}")
        null
    }

/**
 * 根据分辨率压缩图片
 */
fun getCompressedBitmapByResolution(
    context: Context,
    uri: Uri,
    targetWidth: Int = DEFAULT_TARGET_WIDTH,
    targetHeight: Int = DEFAULT_TARGET_HEIGHT,
): Bitmap? {
    val originSize = getImageDimensions(context, uri) ?: return null
    val widthScale = originSize.width.toFloat() / targetWidth
    val heightScale = originSize.height.toFloat() / targetHeight
    val scale = maxOf(widthScale, heightScale).takeIf { it > 1 } ?: 1f

    Log.d(TAG, "压缩 scale: $scale")

    val options =
        BitmapFactory.Options().apply {
            inSampleSize = scale.toInt()
            inJustDecodeBounds = false
        }

    return try {
        /**
         * 	InputStream 是一次性的，不能多次读取。
         * 	同一个 InputStream 不能同时用于 BitmapFactory.decodeStream() 和 ExifInterface 的读取。
         *
         * */
        val rotationInt =
            context.contentResolver.openInputStream(uri).use { inputStream ->
                getExifRotationFromInputStream(inputStream)
            }

        Log.d(TAG, "旋转角度: $rotationInt")

        val bitmap =
            context.contentResolver.openInputStream(uri).use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

        bitmap?.let { rotateBitmapIfNeeded(it, rotationInt) }
    } catch (e: Exception) {
        Log.e(TAG, "压缩图片失败: ${e.message}")
        null
    }
}

/**
 * 压缩 Bitmap 到指定大小
 */
fun compressBitmap(
    bitmap: Bitmap,
    maxSizeKB: Int,
): ByteArray {
    var compressCount = 0
    val buffer = Buffer()
    var quality = 100

    bitmap.compress(Bitmap.CompressFormat.WEBP, quality, buffer.outputStream())
    Log.d(TAG, "初次压缩图片大小: ${buffer.size / 1024} KB")

    // 循环压缩直到满足文件大小要求
    while (buffer.size > maxSizeKB * BYTES_PER_KB && quality > 10) {
        compressCount++
        val subtract =
            when {
                buffer.size > COMPRESS_THRESHOLD_5MB && compressCount == 1 -> 50
                buffer.size > COMPRESS_THRESHOLD_1MB && compressCount == 2 -> 20
                else -> 10
            }

        quality -= subtract
        buffer.clear()
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, buffer.outputStream())
        Log.d(TAG, "第${compressCount}次压缩后大小: ${buffer.size / 1024} KB")
    }

    return buffer.readByteArray()
}

/**
 * 保存压缩后的图片
 */
fun saveCompressedImage(
    savePath: String,
    compressedBytes: ByteArray,
) {
    try {
        File(savePath).sink().buffer().use { sink ->
            sink.write(compressedBytes)
        }
        Log.d(TAG, "图片保存成功，路径: $savePath")
    } catch (e: Exception) {
        Log.e(TAG, "图片保存失败: ${e.message}")
    }
}

/**
 * 压缩图片并保存到指定路径
 */
fun compressImageFromUri(
    context: Context,
    uri: Uri,
    savePath: String,
    maxSizeKB: Int,
): Boolean {
    val bitmap = getCompressedBitmapByResolution(context, uri) ?: return false
    val compressedBytes = compressBitmap(bitmap, maxSizeKB)
    saveCompressedImage(savePath, compressedBytes)
    bitmap.recycle() // 回收资源
    return true
}
