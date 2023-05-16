package com.yikwing.datastore

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

interface IDataStoreOwner {
    val context: Context get() = application
    val dataStore: DataStore<Preferences>

    fun intPreference(default: Int? = null) =
        PreferenceProperty(::intPreferencesKey, default)

    fun doublePreference(default: Double? = null) =
        PreferenceProperty(::doublePreferencesKey, default)

    fun booleanPreference(default: Boolean? = null) =
        PreferenceProperty(::booleanPreferencesKey, default)

    fun stringPreference(default: String? = null) =
        PreferenceProperty(::stringPreferencesKey, default)


    companion object {
        lateinit var application: Application
    }
}