package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.yk.yknetwork.doSuccess

@Composable
fun OtherPageScreen(
    msg: String?,
    viewModel: OtherViewModel = hiltViewModel()
) {
    val httpBin by viewModel.headers.collectAsState()

    Surface {
        Column {
            Text(text = "$msg")
            httpBin.doSuccess {
                Text(text = it.userAgent)
            }
        }
    }
}
