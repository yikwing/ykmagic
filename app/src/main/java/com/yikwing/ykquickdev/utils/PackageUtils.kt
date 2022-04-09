package com.yikwing.ykquickdev.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.yikwing.logger.Logger

data class PackageInfo(
    var appIcon: Drawable,
    var appName: String,
    var appPackageName: String,
    var versionCode: Int,
    var versionName: String,
    var signMD5: String,
    var signSHA1: String
)


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

    } catch (e: PackageManager.NameNotFoundException) {
        Logger.e(e, "PackageManager.NameNotFoundException")
        return null
    } catch (e: Exception) {
        Logger.e(e, "Other Exception")
        return null
    }
}