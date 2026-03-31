package com.yikwing.compose.state

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yikwing.compose.layout.Center

@Composable
fun LoadingWidget(modifier: Modifier = Modifier) {
    Center(modifier = modifier) {
        CircularProgressIndicator()
    }
}
