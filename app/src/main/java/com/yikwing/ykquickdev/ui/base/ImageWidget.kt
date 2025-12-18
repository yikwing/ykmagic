package com.yikwing.ykquickdev.ui.base

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

sealed class ImageSource {
    data class Local(
        @param:DrawableRes val resId: Int,
    ) : ImageSource()

    data class Network(
        val url: String,
    ) : ImageSource()
}

@Composable
fun RoundedImage(
    source: ImageSource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    cornerRadius: Dp = 0.dp,
    topStart: Dp = cornerRadius,
    topEnd: Dp = cornerRadius,
    bottomStart: Dp = cornerRadius,
    bottomEnd: Dp = cornerRadius,
) {
    val shape =
        RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd,
        )
    val clippedModifier = modifier.clip(shape)

    when (source) {
        is ImageSource.Local -> {
            Image(
                painter = painterResource(id = source.resId),
                modifier = clippedModifier,
                contentDescription = contentDescription,
            )
        }

        is ImageSource.Network -> {
            AsyncImage(
                model = source.url,
                modifier = clippedModifier,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
fun CircleImage(
    source: ImageSource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val clippedModifier = modifier.clip(CircleShape)

    when (source) {
        is ImageSource.Local -> {
            Image(
                painter = painterResource(id = source.resId),
                modifier = clippedModifier,
                contentDescription = contentDescription,
            )
        }

        is ImageSource.Network -> {
            AsyncImage(
                model = source.url,
                modifier = clippedModifier,
                contentDescription = contentDescription,
            )
        }
    }
}
