package com.yikwing.ykquickdev

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Get the latest snapshot value, suspending until available.
 *
 * ```kotlin
 * val prefs = userPreferencesStore.getLatest()
 * Log.d("TAG", "current name: ${prefs.name}")
 * ```
 */
suspend fun <T> DataStore<T>.getLatest(): T = data.first()

/**
 * Update data and return the updated value.
 *
 * ```kotlin
 * val updated = userPreferencesStore.updateAndGet { it.copy(name = "Alice") }
 * Log.d("TAG", "updated name: ${updated.name}")
 * ```
 */
suspend fun <T> DataStore<T>.updateAndGet(transform: (T) -> T): T {
    updateData(transform)
    return data.first()
}

/**
 * Observe a specific field, only emitting when its value actually changes.
 * Avoids unnecessary recomposition when unrelated fields are updated.
 *
 * ```kotlin
 * // In ViewModel
 * val nameFlow: Flow<String> = userPreferencesStore.select { it.name }
 *
 * // Compose
 * val name by viewModel.nameFlow.collectAsStateWithLifecycle("")
 * ```
 */
fun <T, R> DataStore<T>.select(selector: (T) -> R): Flow<R> = data.map(selector).distinctUntilChanged()