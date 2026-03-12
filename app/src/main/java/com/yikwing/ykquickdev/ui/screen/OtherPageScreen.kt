package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yikwing.network.RequestState
import com.yikwing.network.onFailure
import com.yikwing.network.onSuccess
import com.yikwing.ykquickdev.components.Center
import com.yikwing.ykquickdev.viewmodel.HttpBinViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherPageScreen(
    id: String,
    viewModel: HttpBinViewModel = koinViewModel(),
) {
    val httpBin by viewModel.headers.collectAsState()
    val userName by viewModel.userName.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Center {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = "Product ID: $id === User: $userName",
                    modifier =
                        Modifier
                            .background(Color.Green)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                )

                AsyncImage(
                    model = "https://images.pexels.com/photos/34353414/pexels-photo-34353414.jpeg",
                    contentDescription = null,
                    modifier =
                        Modifier.clip(
                            RoundedCornerShape(6),
                        ),
                )

                when (httpBin.repo) {
                    is RequestState.Loading -> {
                        Column {
                            CircularProgressIndicator()
                            LaunchedEffect(Unit) {
                                viewModel.initHttpBinData()
                            }
                        }
                    }

                    is RequestState.Success -> {
                        httpBin.repo.onSuccess {
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
                        httpBin.repo.onFailure {
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
fun OtherPageScreenPreview() {
    OtherPageScreen(id = "preview-product-123")
}
