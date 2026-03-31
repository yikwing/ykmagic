package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yikwing.compose.image.ImageSource
import com.yikwing.compose.image.RoundedImage
import com.yikwing.network.RequestState
import com.yikwing.network.onFailure
import com.yikwing.network.onSuccess
import com.yikwing.proxy.R
import com.yikwing.ykquickdev.components.Center
import com.yikwing.ykquickdev.ui.utils.sdp
import com.yikwing.ykquickdev.viewmodel.HttpBinViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class Product(
    val id: String,
) : NavKey

fun EntryProviderScope<NavKey>.otherPageEntry() {
    entry<Product> { product ->
        OtherPageScreen(product.id)
    }
}

@Composable
fun OtherPageScreen(
    id: String,
    viewModel: HttpBinViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Center {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = "Product ID: $id === User: ${uiState.userName}",
                    modifier =
                        Modifier
                            .background(Color.Green)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                )

                RoundedImage(
                    source = ImageSource.Network("https://images.pexels.com/photos/34353414/pexels-photo-34353414.jpeg"),
                    cornerRadius = 6.sdp,
                    modifier = Modifier
                        .width(300.sdp)
                        .height(400.sdp),
                )

                when (uiState.headers) {
                    is RequestState.Loading -> {
                        Column {
                            CircularProgressIndicator()
                            LaunchedEffect(Unit) {
                                viewModel.initHttpBinData()
                            }
                        }
                    }

                    is RequestState.Success -> {
                        uiState.headers.onSuccess {
                            Text(
                                text = it.userAgent,
                                modifier =
                                    Modifier.clickable {
                                        viewModel.updateName("abc")
                                    },
                            )
                        }
                    }

                    is RequestState.Error -> {
                        uiState.headers.onFailure {
                            Text(text = "${it.message}", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun OtherPageScreenPreview() {
    OtherPageScreen(id = "preview-product-123")
}
