package com.yikwing.ykextension

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// 线程安全
fun <T> safeLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.SYNCHRONIZED, initializer)

// 非线程安全
fun <T> unSafeLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)


/**
 *
 * 获取activity intent参数
 *
 * */
class IntentWrapper<T>(private val default: T) : ReadOnlyProperty<Activity, T> {
    override fun getValue(thisRef: Activity, property: KProperty<*>): T {
        return when (default) {
            is Int -> thisRef.intent.getIntExtra(property.name, default)
            is String -> thisRef.intent.getStringExtra(property.name) ?: default
            is Boolean -> thisRef.intent.getBooleanExtra(property.name, default)
            else -> throw  Exception("Not Found Class Type")
        } as T
    }
}

/**
 *
 * private val data by intIntent()
 *
 * */

fun intIntent(default: Int = 0) = IntentWrapper(default)

fun stringIntent(default: String = "") = IntentWrapper(default)

fun booleanIntent(default: Boolean = false) = IntentWrapper(default)

/**
 *
 * fragment argument参数
 *
 * */
class ArgumentWrapper<T>(private val default: T) : ReadOnlyProperty<Fragment, T> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return when (default) {
            is Int -> thisRef.arguments?.getInt(property.name, default)
            is String -> thisRef.arguments?.getString(property.name, default)
            is Boolean -> thisRef.arguments?.getBoolean(property.name, default)
            else -> throw  Exception("Not Found Class Type")
        } as T
    }
}

fun intArgument(default: Int = 0) = ArgumentWrapper(default)

fun stringArgument(default: String = "") = ArgumentWrapper(default)

fun booleanArgument(default: Boolean = false) = ArgumentWrapper(default)