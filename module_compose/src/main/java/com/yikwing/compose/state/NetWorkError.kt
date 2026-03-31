package com.yikwing.compose.state

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yikwing.compose.layout.Center

@Composable
fun NetWorkError(
    throwable: Throwable?,
    modifier: Modifier = Modifier,
) {
    Center(modifier = modifier) {
        Text(text = "${throwable?.message}")
    }
}

@Preview(showBackground = true)
@Composable
private fun NetWorkErrorPreview() {
    NetWorkError(RuntimeException("Not Found"), modifier = Modifier.fillMaxSize())
}
