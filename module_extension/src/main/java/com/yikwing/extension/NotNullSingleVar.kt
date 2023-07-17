package com.yikwing.extension

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *
 * companion object {
 *    var instance by Delegates.notNullSingle<MainApplication>()
 * }
 *
 * */
private class NotNullSingleVar<T> : ReadWriteProperty<Any?, T> {

    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
            ?: throw IllegalStateException(
                "Property ${property.name} should be initialized before get."
            )
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value == null) {
            value
        } else {
            throw IllegalStateException("Property ${property.name} already initialized")
        }
    }
}

fun <T : Any> Delegates.notNullSingle(): ReadWriteProperty<Any?, T> = NotNullSingleVar()
