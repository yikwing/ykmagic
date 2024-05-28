package com.yikwing.extension

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// 线程安全
fun <T> safeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.SYNCHRONIZED, initializer)

// 非线程安全
fun <T> unSafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

sealed class IntentValue<T> {
    abstract fun getValue(
        intent: Intent,
        name: String,
    ): T

    data class IntValue(private val defaultValue: Int) : IntentValue<Int>() {
        override fun getValue(
            intent: Intent,
            name: String,
        ): Int {
            return intent.getIntExtra(name, defaultValue)
        }
    }

    data class StringValue(private val defaultValue: String) : IntentValue<String>() {
        override fun getValue(
            intent: Intent,
            name: String,
        ): String {
            return intent.getStringExtra(name) ?: defaultValue
        }
    }

    data class BoolValue(private val defaultValue: Boolean) : IntentValue<Boolean>() {
        override fun getValue(
            intent: Intent,
            name: String,
        ): Boolean {
            return intent.getBooleanExtra(name, defaultValue)
        }
    }
}

/**
 *
 * 获取activity intent参数
 *
 * */
class IntentWrapper<T>(private val intentValue: IntentValue<T>) : ReadOnlyProperty<Activity, T> {
    override fun getValue(
        thisRef: Activity,
        property: KProperty<*>,
    ): T {
        return intentValue.getValue(thisRef.intent, property.name)
    }
}

/**
 *
 * private val data by intIntent()
 *
 * */

fun intIntent(default: Int = 0) = IntentWrapper(IntentValue.IntValue(default))

fun stringIntent(default: String = "") = IntentWrapper(IntentValue.StringValue(default))

fun booleanIntent(default: Boolean = false) = IntentWrapper(IntentValue.BoolValue(default))

/**
 *
 * fragment argument参数 带默认值
 *
 *
 * ``` example
 *  private var param1: Int by FragmentArgumentDelegate(0)
 *  private var param2: String by FragmentArgumentDelegate("")
 *  companion object {
 *      fun newInstance(a: Int, b: String) = HiltFragment().apply {
 *          param1 = a
 *          param2 = b
 *      }
 *  }
 * ```
 *
 *
 * */

class FragmentArgumentDelegate<T>(private val default: T) : ReadWriteProperty<Fragment, T> {
    override fun getValue(
        thisRef: Fragment,
        property: KProperty<*>,
    ): T {
        val arguments = thisRef.arguments ?: Bundle()

        return when (default) {
            is Boolean -> arguments.getBoolean(property.name, default)
            is String -> arguments.getString(property.name, default)
            is Int -> arguments.getInt(property.name, default)
            is Short -> arguments.getShort(property.name, default)
            is Long -> arguments.getLong(property.name, default)
            is Byte -> arguments.getByte(property.name, default)
            is Float -> arguments.getFloat(property.name, default)
            is Parcelable -> arguments.getParcelable(property.name)
            is Serializable -> arguments.getSerializable(property.name)
            else -> throw Exception("Not Found Class Type")
        } as T
    }

    override fun setValue(
        thisRef: Fragment,
        property: KProperty<*>,
        value: T,
    ) {
        val arguments = thisRef.arguments ?: Bundle()
        arguments.put(property.name, value)
    }
}

fun <T> Bundle.put(
    key: String,
    value: T,
) {
    when (value) {
        is Boolean -> putBoolean(key, value)
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Short -> putShort(key, value)
        is Long -> putLong(key, value)
        is Byte -> putByte(key, value)
        is ByteArray -> putByteArray(key, value)
        is Char -> putChar(key, value)
        is CharArray -> putCharArray(key, value)
        is CharSequence -> putCharSequence(key, value)
        is Float -> putFloat(key, value)
        is Bundle -> putBundle(key, value)
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        else -> throw IllegalStateException("Type of property $key is not supported")
    }
}
