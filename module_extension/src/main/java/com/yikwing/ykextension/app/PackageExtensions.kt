package com.yikwing.ykextension.app

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.yikwing.ykextension.common.DigestUtils

const val TAG = "PackageUtils"

data class PackageInfo(
    var appIcon: Drawable,
    var appName: String,
    var appPackageName: String,
    var versionCode: Int,
    var versionName: String,
    var signMD5: String,
    var signSHA1: String
)

/**
 *
 * 获取App Package数据
 *
 */
fun Context.getPackageInfo(packageName: String): PackageInfo? {
    val pm = packageManager

    try {
        val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        val applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val appName = pm.getApplicationLabel(applicationInfo).toString()
        val appIconDrawable = pm.getApplicationIcon(packageName)

        return PackageInfo(
            appIconDrawable,
            appName,
            packageInfo.packageName,
            packageInfo.versionCode,
            packageInfo.versionName,
            DigestUtils.crypto(packageInfo.signatures[0].toByteArray(), DigestUtils.MD5),
            DigestUtils.crypto2capital(packageInfo.signatures[0].toByteArray(), DigestUtils.SHA1)
        )

    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        return null
    }
}

/**
 *
 * 获取App元数据
 *
 */
fun Context.getMetaData(key: String): String? {
    return try {
        val pm = packageManager
        val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val channel = appInfo.metaData.getString(key)
        channel
    } catch (e: Exception) {
        Log.e("PackageUtils - getMetaData", e.toString())
        null
    }
}
