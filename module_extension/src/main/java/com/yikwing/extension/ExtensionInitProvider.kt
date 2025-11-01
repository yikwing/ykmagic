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
 *
 * 工作原理：
 * 1. Android 系统在应用启动时自动调用 ContentProvider.onCreate()
 * 2. onCreate() 在主线程调用，保证初始化顺序
 * 3. 自动获取 Application Context 并初始化 GlobalContextProvider
 */
class ExtensionInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        // 获取 Application Context 并初始化
        val context = context?.applicationContext
        if (context != null) {
            Log.d("============", "ExtensionInitProvider")
            GlobalContextProvider.initialize(context)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
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
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
