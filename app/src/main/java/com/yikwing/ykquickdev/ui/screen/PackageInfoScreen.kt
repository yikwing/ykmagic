package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yikwing.ykquickdev.R

val rubikGemstonesRegular =
    FontFamily(
        Font(R.font.rubik_gemstones_regular, FontWeight.Normal),
    )

val permanentMarkerRegular =
    FontFamily(
        Font(R.font.permanent_marker_regular, FontWeight.Normal),
    )

@Composable
fun PackageInfoScreen(
    navigationToPage: (str: String) -> Unit,
    navigationToDiy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.then(modifier),
    ) {
        Button(
            onClick = {
                navigationToPage("kotlin")
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Text("to Other", fontFamily = rubikGemstonesRegular)
        }

        Button(
            onClick = navigationToDiy,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Text("to Diy", fontFamily = rubikGemstonesRegular)
        }
    }
}

@Composable
fun OtherPageScreen(
    str: String,
    navigationToUI: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            "OtherPage : $str",
            fontFamily = rubikGemstonesRegular,
            fontSize = 24.sp,
            color = Color(0xFF4C1B24),
            modifier =
                Modifier.clickable {
                    navigationToUI()
                },
        )
    }
}

@Composable
fun DiyInputScreen(navigationToAuth: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            "DiyInput",
            fontFamily = rubikGemstonesRegular,
            fontSize = 24.sp,
            color = Color(0xFF002FA7),
            modifier =
                Modifier.clickable {
                    navigationToAuth()
                },
        )
    }
}

@Composable
fun AuthLoginScreen(navigationToRegister: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            "AuthLogin",
            fontFamily = permanentMarkerRegular,
            fontSize = 24.sp,
            color = Color(0xFF002FA7),
            modifier =
                Modifier.clickable {
                    navigationToRegister()
                },
        )
    }
}

@Composable
fun AuthRegisterScreen(navigationToHome: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            "AuthRegister",
            fontFamily = permanentMarkerRegular,
            fontSize = 24.sp,
            color = Color(0xFF002FA7),
            modifier =
                Modifier.clickable {
                    navigationToHome()
                },
        )
    }
}
