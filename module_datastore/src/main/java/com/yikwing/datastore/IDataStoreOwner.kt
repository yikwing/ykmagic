package com.yikwing.datastore

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlin.properties.ReadOnlyProperty

interface IDataStoreOwner {
    val context: Context get() = application
    val dataStore: DataStore<Preferences>

    fun intPreference(default: Int? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Int>> =
        PreferenceProperty(::intPreferencesKey, default)

    fun doublePreference(default: Double? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Double>> =
        PreferenceProperty(::doublePreferencesKey, default)

    fun longPreference(default: Long? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Long>> =
        PreferenceProperty(::longPreferencesKey, default)

    fun floatPreference(default: Float? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Float>> =
        PreferenceProperty(::floatPreferencesKey, default)

    fun booleanPreference(default: Boolean? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Boolean>> =
        PreferenceProperty(::booleanPreferencesKey, default)

    fun stringPreference(default: String? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<String>> =
        PreferenceProperty(::stringPreferencesKey, default)

    fun stringSetPreference(default: Set<String>? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Set<String>>> =
        PreferenceProperty(::stringSetPreferencesKey, default)

    companion object {
        lateinit var application: Application
    }
}
