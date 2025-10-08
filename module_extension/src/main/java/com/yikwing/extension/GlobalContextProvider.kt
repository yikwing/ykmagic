package com.yikwing.extension

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

/**
 * 全局上下文提供者 (Global Context Provider)
 * `GlobalContextProvider.initialize(this)`
 */
object GlobalContextProvider {
    private lateinit var _appContext: Context
    private var isInitialized = false

    /**
     * 获取 Application Context。
     * @throws IllegalStateException 如果尚未调用 initialize()。
     */
    val appContext: Context
        get() {
            check(isInitialized) {
                "GlobalContextProvider 尚未初始化。请在 Application 的 onCreate() 中调用 initialize(this)。"
            }
            return _appContext
        }

    @JvmStatic
    fun initialize(context: Context) {
        if (!isInitialized) {
            _appContext = context.applicationContext
            isInitialized = true
        }
        // 忽略重复初始化，保持单例上下文
    }
}

/**
 * 通过资源 ID 获取 Drawable (可空)。如果找不到，返回 null。
 */
fun @receiver:DrawableRes Int.asDrawableOrNull(): Drawable? = ContextCompat.getDrawable(GlobalContextProvider.appContext, this)

/**
 * 通过资源 ID 获取颜色值。
 */
fun @receiver:ColorRes Int.asColor(): Int = ContextCompat.getColor(GlobalContextProvider.appContext, this)

/**
 * 通过资源 ID 获取 Typeface/字体 (可空)。如果找不到，返回 null。
 */
fun @receiver:FontRes Int.asTypefaceOrNull(): Typeface? = ResourcesCompat.getFont(GlobalContextProvider.appContext, this)

/**
 * 通过资源 ID 获取字符串。
 */
fun @receiver:StringRes Int.asString(): String = GlobalContextProvider.appContext.getString(this)

/**
 * 通过资源 ID 获取格式化的字符串。
 */
fun @receiver:StringRes Int.asString(vararg formatArgs: Any): String = GlobalContextProvider.appContext.getString(this, *formatArgs)
