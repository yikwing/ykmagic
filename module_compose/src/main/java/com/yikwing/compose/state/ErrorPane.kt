package com.yikwing.compose.state

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yikwing.compose.layout.Center

/**
 * 居中错误容器，可通过 [content] 替换默认内容。
 * [message] 和 [onRetry] 用于默认界面，自定义内容时由调用方处理。
 */
@Composable
fun ErrorPane(
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit = {
        StatePageDefaults.ErrorContent(message = message, onRetry = onRetry)
    },
) {
    Center(modifier = modifier, content = content)
}

@Preview(showBackground = true)
@Composable
private fun ErrorPanePreview() {
    ErrorPane(modifier = Modifier.fillMaxSize(), message = "Not Found", onRetry = {})
}
