package com.yikwing.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStorePreferences<V>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<V>,
    private val default: V?
) {

    suspend fun set(value: V?): Preferences =
        dataStore.edit { preference ->
            if (value == null) {
                preference.remove(key)
            } else {
                preference[key] = value
            }
        }

    suspend fun get(): V? = asFLow().first()

    fun asFLow(): Flow<V?> = dataStore.data.map { it[key] ?: default }
}

