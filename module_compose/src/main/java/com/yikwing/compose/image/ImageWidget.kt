package com.yikwing.compose.image

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

data class CornerRadius(
    val topStart: Dp = 0.dp,
    val topEnd: Dp = topStart,
    val bottomStart: Dp = topStart,
    val bottomEnd: Dp = topStart,
)

@Stable
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
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
    @DrawableRes placeholder: Int? = null,
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
                placeholder = placeholder?.let { painterResource(it) },
            )
        }
    }
}

@Composable
fun RoundedImage(
    source: ImageSource,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes placeholder: Int? = null,
    corner: CornerRadius = CornerRadius(),
) {
    val shape =
        remember(corner) {
            RoundedCornerShape(
                topStart = corner.topStart,
                topEnd = corner.topEnd,
                bottomStart = corner.bottomStart,
                bottomEnd = corner.bottomEnd,
            )
        }
    BaseImage(source, contentScale, modifier.clip(shape), placeholder)
}

@Composable
fun CircleImage(
    source: ImageSource,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes placeholder: Int? = null,
) {
    BaseImage(source, contentScale, modifier.clip(CircleShape), placeholder)
}
