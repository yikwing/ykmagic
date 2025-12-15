package com.yikwing.ykquickdev

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import okio.buffer
import okio.sink
import okio.source
import java.io.InputStream
import java.io.OutputStream

/**
 * <pre>
 *     author: yikwing
 *
 *        _ _              _
 *       (_) |            (_)
 *  _   _ _| | ____      ___ _ __   __ _
 * | | | | | |/ /\ \ /\ / / | '_ \ / _` |
 * | |_| | |   <  \ V  V /| | | | | (_| |
 *  \__, |_|_|\_\  \_/\_/ |_|_| |_|\__, |
 *   __/ |                          __/ |
 *  |___/                          |___/
 *
 *     email : 49999@live.com
 *     time  : 2025-12-15 22:58
 *     desc  :
 * </pre>
 */
object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings
        get() = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings =
        try {
            input.source().buffer().use { bufferedSource ->
                AppSettings.ADAPTER.decode(bufferedSource)
            }
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read protos.", exception)
        }

    override suspend fun writeTo(
        t: AppSettings,
        output: OutputStream,
    ) {
        output.sink().buffer().use { bufferedSink ->
            t.adapter.encode(bufferedSink, t)
            bufferedSink.flush()
        }
    }
}

val Context.appSettingsStore: DataStore<AppSettings> by dataStore(
    fileName = "app_settings.pb",
    serializer = AppSettingsSerializer,
)
