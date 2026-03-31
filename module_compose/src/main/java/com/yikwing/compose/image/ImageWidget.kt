package com.yikwing.compose.image

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
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
private fun BaseImage(
    source: ImageSource,
    modifier: Modifier = Modifier,
    contentScale: ContentScale,
    placeholder: Painter? = null,
) {
    when (source) {
        is ImageSource.Local -> {
            Image(
                painter = painterResource(id = source.resId),
                modifier = modifier,
                contentDescription = null,
                contentScale = contentScale,
            )
        }

        is ImageSource.Network -> {
            AsyncImage(
                model = source.url,
                modifier = modifier,
                contentDescription = null,
                contentScale = contentScale,
                placeholder = placeholder,
            )
        }
    }
}

@Composable
fun RoundedImage(
    source: ImageSource,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
    cornerRadius: Dp = 0.dp,
    topStart: Dp = cornerRadius,
    topEnd: Dp = cornerRadius,
    bottomStart: Dp = cornerRadius,
    bottomEnd: Dp = cornerRadius,
) {
    val shape =
        remember(topStart, topEnd, bottomStart, bottomEnd) {
            RoundedCornerShape(
                topStart = topStart,
                topEnd = topEnd,
                bottomStart = bottomStart,
                bottomEnd = bottomEnd,
            )
        }
    BaseImage(source, modifier.clip(shape), contentScale, placeholder)
}

@Composable
fun CircleImage(
    source: ImageSource,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
) {
    BaseImage(source, modifier.clip(CircleShape), contentScale, placeholder)
}
