package com.yikwing.compose.state

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yikwing.compose.layout.Center

/** 居中加载容器，可通过 [content] 替换默认内容。 */
@Composable
fun LoadingPane(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {
        StatePageDefaults.LoadingContent()
    },
) {
    Center(modifier = modifier, content = content)
}

@Composable
@Preview(widthDp = 375, heightDp = 812)
private fun LoadingPanePreview() {
    LoadingPane()
}
