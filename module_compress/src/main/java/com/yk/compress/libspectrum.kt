package com.yk.compress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.Size
import com.facebook.spectrum.*
import com.facebook.spectrum.image.EncodedImageFormat
import com.facebook.spectrum.image.ImageSize
import com.facebook.spectrum.logging.SpectrumLogcatLogger
import com.facebook.spectrum.options.TranscodeOptions
import com.facebook.spectrum.requirements.EncodeRequirement
import com.facebook.spectrum.requirements.ResizeRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transform
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @Author yikwing
 * @Date 25/5/2022-13:20
 * @Description:
 */


fun makeSpectrum(): Spectrum {
    return Spectrum.make(SpectrumLogcatLogger(Log.INFO), DefaultPlugins.get())
}

/**
 * libspectrum 压缩图片
 */
suspend fun compress(context: Context, imgList: List<File>, callBack: (MutableList<File?>) -> Unit) {

    val compressList = mutableListOf<File?>()

    val mSpectrum = makeSpectrum()

    val transcodeOptions = TranscodeOptions
        .Builder(
            EncodeRequirement(EncodedImageFormat.WEBP, 70)
        ).resize(
            ResizeRequirement.Mode.EXACT_OR_SMALLER,
            ImageSize(1080, 1920)
        ).build()

    imgList.asFlow()
        .transform {

            val sink = System.currentTimeMillis().toString() + ".webp"

            try {
                mSpectrum.transcode(
                    EncodedImageSource.from(it),
                    EncodedImageSink.from(File(context.cacheDir, sink)),
                    transcodeOptions,
                    "upload_flow_callsite_identifier"
                )
                emit(Result.success(File(context.cacheDir, sink)))
            } catch (e: IOException) {
                emit(Result.failure(e))
            } catch (e: SpectrumException) {
                emit(Result.success(it))
            }
        }.flowOn(Dispatchers.IO)
        .onCompletion {
            callBack(compressList)
        }
        .collect {

            if (it.isSuccess) {
                compressList.add(it.getOrNull())
            } else {
                Log.e("compress", it.exceptionOrNull()?.localizedMessage ?: "compress Error")
            }
        }

}






