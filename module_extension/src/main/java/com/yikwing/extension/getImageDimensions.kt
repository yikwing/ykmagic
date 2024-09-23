package com.yikwing.extension

import android.content.ContentResolver
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

private const val TAG = "ImageCompression"

/**
 * 获取图片的原始宽高
 */
fun getImageDimensions(
    context: Context,
    uri: Uri,
): Size? =
    try {
        val contentResolver: ContentResolver = context.contentResolver
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        contentResolver.openInputStream(uri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        if (options.outWidth > 0 && options.outHeight > 0) {
            Size(options.outWidth, options.outHeight)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e(TAG, "获取图片宽高失败")
        null
    }

/**
 * 根据分辨率压缩图片
 */
fun compressImageByResolution(
    context: Context,
    uri: Uri,
    targetWidth: Int = 960,
    targetHeight: Int = 1280,
): Bitmap? {
    val originSize = getImageDimensions(context, uri) ?: return null
    val widthScale = originSize.width.toFloat() / targetWidth
    val heightScale = originSize.height.toFloat() / targetHeight
    val scale = maxOf(widthScale, heightScale).takeIf { it > 1 } ?: 1f

    val options =
        BitmapFactory.Options().apply {
            inSampleSize = scale.toInt() // 设置缩放比例
            inJustDecodeBounds = false
        }

    return try {
        context.contentResolver.openInputStream(uri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 压缩图片到指定大小
 */
fun compressBitmap(
    bitmap: Bitmap,
    maxSizeKB: Int,
): ByteArray {
    var compressCount = 0

    // 使用 Okio 进行流操作
    val buffer = Buffer()
    var quality = 100

    bitmap.compress(Bitmap.CompressFormat.WEBP, quality, buffer.outputStream())
    Log.i("ImageCompression", "初次压缩图片大小: ${buffer.size / 1024} KB")

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

        Log.i(TAG, "第${compressCount}次压缩后大小：" + buffer.size / 1024 + "KB")
    }

    return buffer.readByteArray()
}

/**
 * 将压缩后的图片保存到指定路径
 */
fun saveCompressedImage(
    savePath: String,
    compressedBytes: ByteArray,
) {
    try {
        File(savePath).sink().buffer().use { sink ->
            sink.write(compressedBytes)
        }
        Log.i(TAG, "图片保存成功，路径: $savePath")
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e(TAG, "图片保存失败")
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
    val bitmap = compressImageByResolution(context, uri) ?: return false
    val compressedBytes = compressBitmap(bitmap, maxSizeKB)

    saveCompressedImage(savePath, compressedBytes)
    bitmap.recycle() // 回收资源
    return true
}
