package com.yikwing.compose.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yikwing.compose.R

/** [StatePage]、[LoadingPane] 和 [ErrorPane] 共用的默认内容，可独立使用或通过插槽替换。 */
object StatePageDefaults {
    @Composable
    fun LoadingContent(modifier: Modifier = Modifier) {
        CircularProgressIndicator(modifier = modifier)
    }

    @Composable
    fun EmptyContent(modifier: Modifier = Modifier) {
        Text(text = stringResource(R.string.yk_compose_empty), modifier = modifier)
    }

    @Composable
    fun ErrorContent(
        modifier: Modifier = Modifier,
        message: String? = null,
        onRetry: (() -> Unit)? = null,
        maxLines: Int = Int.MAX_VALUE,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message?.takeIf { it.isNotBlank() } ?: stringResource(R.string.yk_compose_error),
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(stringResource(R.string.yk_compose_retry))
                }
            }
        }
    }
}
