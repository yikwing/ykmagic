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

object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences =
        try {
            input.source().buffer().use { source ->
                UserPreferences.ADAPTER.decode(source)
            }
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read protos.", exception)
        }

    override suspend fun writeTo(
        t: UserPreferences,
        output: OutputStream,
    ) {
        output.sink().buffer().use { sink ->
            UserPreferences.ADAPTER.encode(sink, t)
        }
    }
}

val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_preferences.pb",
    serializer = UserPreferencesSerializer,
)
