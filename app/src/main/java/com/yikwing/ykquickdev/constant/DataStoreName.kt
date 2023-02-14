package com.yikwing.ykquickdev.constant

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * @Author yikwing
 * @Date 14/2/2023-15:45
 * @Description:
 */

// 定义dataStore
const val dataStore = "Study"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = dataStore)

const val dataStore2 = "Model"
val Context.dataStore2: DataStore<Preferences> by preferencesDataStore(name = dataStore2)
