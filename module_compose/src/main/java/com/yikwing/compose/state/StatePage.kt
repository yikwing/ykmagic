package com.yikwing.compose.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** 页面展示状态；数据和空数据判断由调用方管理。 */
sealed interface PageState {
    data object Loading : PageState

    data object Empty : PageState

    data class Error(
        val message: String? = null,
    ) : PageState

    data object Content : PageState
}

/**
 * 填满可用区域，根据 [state] 展示对应内容，默认居中。
 *
 * [onRetry] 仅用于默认错误界面；不提供时隐藏重试按钮。
 * 各状态均可通过插槽自定义。切换状态时，原状态内容会退出组合。
 * 请求和生命周期副作用应放在调用方，避免在状态切换时重复触发。
 */
@Composable
fun StatePage(
    state: PageState,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    loadingContent: @Composable BoxScope.() -> Unit = { StatePageDefaults.LoadingContent() },
    emptyContent: @Composable BoxScope.() -> Unit = { StatePageDefaults.EmptyContent() },
    errorContent: @Composable BoxScope.(String?) -> Unit = { message ->
        StatePageDefaults.ErrorContent(message = message, onRetry = onRetry)
    },
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            PageState.Loading -> loadingContent()
            PageState.Empty -> emptyContent()
            is PageState.Error -> errorContent(state.message)
            PageState.Content -> content()
        }
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 320, heightDp = 240)
@Composable
private fun StatePageLoadingPreview() {
    MaterialTheme {
        StatePage(state = PageState.Loading) {}
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 320, heightDp = 240)
@Composable
private fun StatePageEmptyPreview() {
    MaterialTheme {
        StatePage(state = PageState.Empty) {}
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 320, heightDp = 240)
@Composable
private fun StatePageErrorPreview() {
    MaterialTheme {
        StatePage(state = PageState.Error(), onRetry = {}) {}
    }
}

@Preview(name = "Content", showBackground = true, widthDp = 320, heightDp = 240)
@Composable
private fun StatePageContentPreview() {
    MaterialTheme {
        StatePage(state = PageState.Content) { Text("Content") }
    }
}
