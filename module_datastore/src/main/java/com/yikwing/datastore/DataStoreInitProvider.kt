package com.yikwing.datastore

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * DataStore 模块自动初始化提供者
 *
 * 通过 ContentProvider 实现 IDataStoreOwner.application 的自动初始化，
 * 无需在 Application.onCreate() 中手动调用。
 */
class DataStoreInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext as? Application
        if (appContext != null) {
            Log.d(TAG, "IDataStoreOwner.application initialized")
            IDataStoreOwner.application = appContext
        } else {
            Log.w(TAG, "Context is null or not Application, initialization skipped")
        }
        return true
    }

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

    private companion object {
        const val TAG = "DataStoreInitProvider"
    }
}