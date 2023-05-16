package com.yikwing.datastore

import androidx.datastore.preferences.core.Preferences
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PreferenceProperty<V>(
    private val key: (String) -> Preferences.Key<V>,
    private val default: V? = null,
) : ReadOnlyProperty<IDataStoreOwner, DataStorePreference<V>> {
    private var cache: DataStorePreference<V>? = null

    override fun getValue(
        thisRef: IDataStoreOwner,
        property: KProperty<*>,
    ): DataStorePreference<V> =
        cache ?: DataStorePreference(thisRef.dataStore, key(property.name), default).also {
            cache = it
        }
}
