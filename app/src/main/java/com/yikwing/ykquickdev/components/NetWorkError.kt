package com.yikwing.ykquickdev.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yk.yknetwork.ApiException

@Composable
fun NetWorkError(throwable: Throwable?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = "wifi off",
                tint = Color.Gray,
                modifier = Modifier.size(100.dp),
            )
            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "${throwable?.message}")
        }
    }
}

@Composable
@Preview(
    showBackground = true,
)
fun NetWorkErrorPreview() {
    NetWorkError(ApiException(-1, "Not Found"), modifier = Modifier.fillMaxSize())
}
