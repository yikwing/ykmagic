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
 *
 * 一般由 [ExtensionInitProvider] 在主线程自动初始化；若手动接入，请在
 * [android.app.Application.onCreate] 中调用 `GlobalContextProvider.initialize(this)`。
 *
 * 使用 `@Volatile` + DCL 保证跨线程访问时不会读到半初始化状态。
 */
object GlobalContextProvider {
    @Volatile
    private var _appContext: Context? = null

    /**
     * 获取 Application Context。
     * @throws IllegalStateException 如果尚未调用 [initialize]。
     */
    val appContext: Context
        get() = _appContext ?: error(
            "GlobalContextProvider 尚未初始化。请在 Application 的 onCreate() 中调用 initialize(this)。",
        )

    @JvmStatic
    fun initialize(context: Context) {
        if (_appContext != null) return
        synchronized(this) {
            if (_appContext == null) {
                _appContext = context.applicationContext
            }
        }
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
