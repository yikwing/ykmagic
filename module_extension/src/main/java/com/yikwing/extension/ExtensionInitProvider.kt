package com.yikwing.extension

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * Extension 模块自动初始化提供者
 *
 * 通过 ContentProvider 实现 GlobalContextProvider 的自动初始化，
 * 无需在 Application.onCreate() 中手动调用。
 */
class ExtensionInitProvider : ContentProviderAdapter() {
    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext
        if (appContext != null) {
            Log.d(TAG, "GlobalContextProvider initialized")
            GlobalContextProvider.initialize(appContext)
        } else {
            Log.w(TAG, "Context is null, initialization skipped")
        }
        return true
    }

    private companion object {
        const val TAG = "ExtensionInitProvider"
    }
}

/**
 * ContentProvider 适配器，提供空实现，子类只需重写 onCreate
 */
abstract class ContentProviderAdapter : ContentProvider() {
    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int = 0
}
