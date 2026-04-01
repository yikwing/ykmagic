package com.yikwing.compose.state

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yikwing.compose.layout.Center

@Composable
fun LoadingWidget(modifier: Modifier = Modifier) {
    Center(modifier = modifier) {
        CircularProgressIndicator()
    }
}

@Composable
@Preview(widthDp = 375, heightDp = 812)
private fun LoadingWidgetPreview() {
    LoadingWidget()
}
