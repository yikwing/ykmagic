package com.yikwing.extension.image

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

// 获取路径的图片exif
fun getExifRotationFromInputFilePath(filePath: String): Int =
    run {
        val exif = ExifInterface(filePath)
        getExifRotation(exif)
    }

// 获取stream的图片exif
fun getExifRotationFromInputStream(inputStream: InputStream?): Int =
    inputStream?.use {
        // 使用 use 来确保 InputStream 在操作后关闭
        val exif = ExifInterface(it)
        getExifRotation(exif)
    } ?: 0

fun getExifRotation(exif: ExifInterface): Int =
    when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

// 修正图片的旋转角度
fun rotateBitmapIfNeeded(
    bitmap: Bitmap,
    rotationAngle: Int,
): Bitmap {
    if (rotationAngle == 0) return bitmap

    val matrix =
        Matrix().apply {
            postRotate(rotationAngle.toFloat())
        }

    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    // 回收旧的 bitmap 以防内存泄漏
    if (!bitmap.isRecycled) {
        bitmap.recycle()
    }

    return rotatedBitmap
}
