package com.yikwing.compose.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.yikwing.compose.layout.Center

@Composable
fun NetWorkError(
    message: String?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Center(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = message ?: "网络异常，请检查网络连接")
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text("重试")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NetWorkErrorPreview() {
    NetWorkError("Not Found", modifier = Modifier.fillMaxSize(), onRetry = {})
}
