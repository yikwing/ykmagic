package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.background
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherPageScreen(
    msg: String?,
    viewModel: OtherViewModel = koinViewModel(),
) {
    val httpBin by viewModel.headers.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Center {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = "$msg",
                    modifier =
                        Modifier
                            .background(Color.Green)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                )

                AsyncImage(
                    model = "https://pic.netbian.com/uploads/allimg/240528/213609-17169033695ae8.jpg",
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
                            Text(text = it.userAgent)
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
    OtherPageScreen(msg = "OtherPageScreen")
}
