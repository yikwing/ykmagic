package com.yikwing.extension.plus

import android.util.SparseIntArray

fun SparseIntArray.getOrPut(
    key: Int,
    defaultValue: () -> Int,
): Int {
    // Check if the key already exists
    val index = indexOfKey(key)

    return if (index >= 0) {
        // Key found, return existing value
        valueAt(index)
    } else {
        // Key not found, calculate the default value
        val value = defaultValue()
        // Put the new key-value pair into the array
        put(key, value)
        // Return the calculated value
        value
    }
}
