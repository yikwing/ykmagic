package com.yk.compress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.Size
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * @Author yikwing
 * @Date 25/5/2022-15:14
 * @Description:
 */

/**
 *
 * 获取原始图片长宽
 */
fun getBitmapSize(filePath: String): Size {
    val options = BitmapFactory.Options()
    // 该属性设置为 true 只会加载图片的边框进来，并不会加载图片具体的像素点
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)

    val outWidth = options.outWidth
    val outHeight = options.outHeight
    return Size(outWidth, outHeight)
}

/**
 *
 * 根据分辨率压缩图片比例
 */
fun compressByResolution(filePath: String, w: Int = 960, h: Int = 1280): Bitmap {
    val options = BitmapFactory.Options()
    val originSize = getBitmapSize(filePath)

    val widthScale = originSize.width / w
    val heightScale = originSize.height / h

    var scale = if (widthScale < heightScale) widthScale else heightScale

    if (scale < 1) {
        scale = 1
    }

    Log.i("compressByResolution", "图片分辨率压缩比例：$scale")
    options.inSampleSize = scale
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeFile(filePath, options)

}


/**
 *
 * 图片压缩
 */
fun compressBitmap(context: Context, filePath: String, ImageSize: Int, savePath: (File) -> Unit) {

    /**
     * 质量因子
     */
    var subtract: Int

    /**
     * 压缩次数
     */
    var compressCount = 0

    if (!File(filePath).exists()) {
        Log.e("compressBitmap", "File Not Exists")
        return
    }


    /**
     * 基础分辨率 仿微信
     */
    val bitmap = compressByResolution(filePath, 960, 1280)


    val baos = ByteArrayOutputStream()
    var options = 100
    // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
    bitmap.compress(Bitmap.CompressFormat.WEBP, options, baos)

    Log.i("compressBitmap", "图片分辨率压缩后：" + baos.toByteArray().size / 1024 + "KB")

    while (baos.toByteArray().size > ImageSize * 1024) {

        /**
         *
         * 图片过大时 适当添加options 减少压缩耗时
         */
        subtract = if (baos.toByteArray().size > 5000 && compressCount < 1) {
            50
        } else if (baos.toByteArray().size > 1000 && compressCount < 2) {
            20
        } else {
            10
        }

        baos.reset()

        options -= subtract

        bitmap.compress(Bitmap.CompressFormat.WEBP, options, baos)

        compressCount++

        Log.i("compressBitmap", "图片压缩后：" + baos.toByteArray().size / 1024 + "KB")

    }

    Log.i("compressBitmap", "图片处理完成：" + baos.toByteArray().size / 1024 + "KB")

    val file = File(context.cacheDir, System.currentTimeMillis().toString() + "android.webp")

    try {
        val fos = FileOutputStream(file)
        fos.write(baos.toByteArray())
        fos.flush()
        fos.close()

        savePath(file)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    bitmap.recycle()
}