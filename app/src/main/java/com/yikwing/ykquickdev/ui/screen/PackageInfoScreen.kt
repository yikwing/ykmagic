package com.yikwing.ykquickdev.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import coil3.compose.AsyncImage
import com.yikwing.extension.app.getMetaData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackageInfoScreen(
    navigationToPage: (String) -> Unit = {},
    navigationToDiy: () -> Unit = {},
) {
    val context: Context = LocalContext.current

    val buildTime by lazy {
        context.getMetaData("com.yikwing.debug.time")
    }

//    Column(
//        modifier = Modifier.padding(horizontal = 16.dp, vertical = 30.dp)
//    ) {
//        TopHeader(packageInfo = packageInfo)
//
//        PackageInfoDes(
//            "versionCode:",
//            (packageInfo?.versionCode ?: 0).toString()
//        )
//        PackageInfoDes(
//            "versionName:",
//            (packageInfo?.versionName ?: "").toString()
//        )
//        PackageInfoDes(
//            "MD5:",
//            (packageInfo?.signMD5 ?: "").toString(),
//            onClick = {
//                copyToClipboard(context, packageInfo?.signMD5, "MD5值已复制")
//            }
//        )
//        PackageInfoDes(
//            "SHA1:",
//            (packageInfo?.signSHA1 ?: "").toString(),
//            onClick = {
//                copyToClipboard(context, packageInfo?.signSHA1, "SHA1值已复制")
//            }
//        )
//    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                navigationToPage("hello")
            },
        ) {
            Text(text = "参数传递")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = navigationToDiy,
        ) {
            Text(text = "diy input")
        }
    }
}

private fun copyToClipboard(
    context: Context,
    copyStr: String?,
    tips: String,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("simple text", copyStr)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
}

@Composable
fun TopHeader(packageInfo: PackageInfo?) {
    val decoupledConstraints =
        ConstraintSet {
            val cover = createRefFor("cover")
            val appName = createRefFor("appName")
            val appPackageName = createRefFor("appPackageName")

            constrain(cover) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            constrain(appName) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(cover.bottom, margin = 8.dp)
            }
            constrain(appPackageName) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(appName.bottom, margin = 8.dp)
            }
        }

    ConstraintLayout(decoupledConstraints, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = packageInfo?.applicationInfo?.icon ?: 0,
            contentDescription = null,
            modifier =
                Modifier
                    .size(120.dp)
                    .layoutId("cover"),
        )
        Text(packageInfo?.applicationInfo?.name ?: "", modifier = Modifier.layoutId("appName"))
        Text(
            packageInfo?.applicationInfo?.packageName ?: "",
            modifier =
                Modifier
                    .layoutId("appPackageName")
                    .padding(bottom = 30.dp),
        )
    }
}

@Composable
fun PackageInfoDes(
    title: String,
    info: String,
    onClick: () -> Unit = {},
) {
    Column {
        Text(title, color = Color(0xFF999999))
        Text(
            info,
            color = Color(0xFF666666),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Preview
@Composable
fun PackageInfoDesPreview() {
    PackageInfoDes("123", "456")
}

@Preview
@Composable
fun TopHeaderPreview() {
    TopHeader(null)
}
