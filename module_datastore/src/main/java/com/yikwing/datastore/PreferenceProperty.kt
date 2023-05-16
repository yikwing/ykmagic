package com.yikwing.datastore

import androidx.datastore.preferences.core.Preferences
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PreferenceProperty<V>(
    private val key: (String) -> Preferences.Key<V>,
    private val default: V? = null
) : ReadOnlyProperty<IDataStoreOwner, DataStorePreferences<V>> {

    private var cache: DataStorePreferences<V>? = null

    override fun getValue(
        thisRef: IDataStoreOwner,
        property: KProperty<*>
    ): DataStorePreferences<V> {
        return cache ?: DataStorePreferences(
            thisRef.dataStore,
            key(property.name),
            default
        ).also {
            cache = it
        }
    }
}