package com.yikwing.extension

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Activity Intent 参数属性委托
 *
 * ```kotlin
 * // 使用属性名作为 key
 * private val userId by intIntent()
 *
 * // 使用自定义 key（推荐，防止混淆后 key 变化）
 * private val userId by intIntent(key = "user_id")
 * private val userName by stringIntent(key = "user_name")
 * private val isVip by booleanIntent(key = "is_vip")
 * private val user by parcelableIntent<User>(key = "user")
 * private val config by serializableIntent<Config>(key = "config")
 * ```
 */
fun intIntent(
    key: String? = null,
    default: Int = 0,
) = ReadOnlyProperty<Activity, Int> { thisRef, property ->
    thisRef.intent.getIntExtra(key ?: property.name, default)
}

fun stringIntent(
    key: String? = null,
    default: String = "",
) = ReadOnlyProperty<Activity, String> { thisRef, property ->
    thisRef.intent.getStringExtra(key ?: property.name) ?: default
}

fun booleanIntent(
    key: String? = null,
    default: Boolean = false,
) = ReadOnlyProperty<Activity, Boolean> { thisRef, property ->
    thisRef.intent.getBooleanExtra(key ?: property.name, default)
}

inline fun <reified T : Parcelable> parcelableIntent(key: String? = null) =
    ReadOnlyProperty<Activity, T?> { thisRef, property ->
        val extras = thisRef.intent.extras ?: return@ReadOnlyProperty null
        extras.classLoader = T::class.java.classLoader
        BundleCompat.getParcelable(extras, key ?: property.name, T::class.java)
    }

inline fun <reified T : Serializable> serializableIntent(key: String? = null) =
    ReadOnlyProperty<Activity, T?> { thisRef, property ->
        val extras = thisRef.intent.extras ?: return@ReadOnlyProperty null
        extras.classLoader = T::class.java.classLoader
        BundleCompat.getSerializable(extras, key ?: property.name, T::class.java)
    }

/**
 * Fragment Arguments 属性委托（支持读写）
 *
 * ```kotlin
 * // 使用属性名作为 key
 * private var userId: Int by intArgument()
 *
 * // 使用自定义 key（推荐，防止混淆后 key 变化）
 * private var userId: Int by intArgument(key = "user_id")
 * private var userName: String by stringArgument(key = "user_name")
 * private var isVip: Boolean by booleanArgument(key = "is_vip")
 * private var user: User? by parcelableArgument(key = "user")
 * private var config: Config? by serializableArgument(key = "config")
 *
 * companion object {
 *     fun newInstance(id: Int, name: String) = MyFragment().apply {
 *         userId = id
 *         userName = name
 *     }
 * }
 * ```
 */
fun intArgument(
    key: String? = null,
    default: Int = 0,
): ReadWriteProperty<Fragment, Int> =
    object : ReadWriteProperty<Fragment, Int> {
        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>,
        ): Int {
            val k = key ?: property.name
            return thisRef.arguments?.getInt(k, default) ?: default
        }

        override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: Int,
        ) {
            val k = key ?: property.name
            thisRef.ensureArguments().putInt(k, value)
        }
    }

fun stringArgument(
    key: String? = null,
    default: String = "",
): ReadWriteProperty<Fragment, String> =
    object : ReadWriteProperty<Fragment, String> {
        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>,
        ): String {
            val k = key ?: property.name
            return thisRef.arguments?.getString(k, default) ?: default
        }

        override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: String,
        ) {
            val k = key ?: property.name
            thisRef.ensureArguments().putString(k, value)
        }
    }

fun booleanArgument(
    key: String? = null,
    default: Boolean = false,
): ReadWriteProperty<Fragment, Boolean> =
    object : ReadWriteProperty<Fragment, Boolean> {
        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>,
        ): Boolean {
            val k = key ?: property.name
            return thisRef.arguments?.getBoolean(k, default) ?: default
        }

        override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: Boolean,
        ) {
            val k = key ?: property.name
            thisRef.ensureArguments().putBoolean(k, value)
        }
    }

inline fun <reified T : Parcelable> parcelableArgument(key: String? = null): ReadWriteProperty<Fragment, T?> =
    object : ReadWriteProperty<Fragment, T?> {
        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>,
        ): T? {
            val k = key ?: property.name
            val args = thisRef.arguments ?: return null
            args.classLoader = T::class.java.classLoader
            return BundleCompat.getParcelable(args, k, T::class.java)
        }

        override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: T?,
        ) {
            val k = key ?: property.name
            thisRef.ensureArguments().putParcelable(k, value)
        }
    }

inline fun <reified T : Serializable> serializableArgument(key: String? = null): ReadWriteProperty<Fragment, T?> =
    object : ReadWriteProperty<Fragment, T?> {
        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>,
        ): T? {
            val k = key ?: property.name
            val args = thisRef.arguments ?: return null
            args.classLoader = T::class.java.classLoader
            return BundleCompat.getSerializable(args, k, T::class.java)
        }

        override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: T?,
        ) {
            val k = key ?: property.name
            thisRef.ensureArguments().putSerializable(k, value)
        }
    }

/** 确保 Fragment 有 arguments Bundle，没有则创建 */
@PublishedApi
internal fun Fragment.ensureArguments(): Bundle = arguments ?: Bundle().also { arguments = it }
