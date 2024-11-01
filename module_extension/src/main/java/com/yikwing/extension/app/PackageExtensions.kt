package com.yikwing.extension.app

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

const val TAG = "PackageUtils"

/**
 *
 * 获取App元数据
 *
 */
fun Context.getMetaData(key: String): String? =
    try {
        val pm = packageManager
        val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val channel = appInfo.metaData.getString(key)
        channel
    } catch (e: Exception) {
        Log.e("PackageUtils - getMetaData", e.toString())
        null
    }
