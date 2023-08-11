package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.yikwing.network.RequestState
import com.yikwing.network.doError
import com.yikwing.network.doSuccess
import com.yikwing.ykquickdev.components.Center

@Composable
fun OtherPageScreen(
    msg: String?,
    viewModel: OtherViewModel = hiltViewModel()
) {
    val httpBin by viewModel.headers.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Center {
            Column {
                Text(text = "$msg")

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
                        httpBin.repo.doSuccess {
                            Text(text = it.userAgent)
                        }
                    }

                    is RequestState.Error -> {
                        httpBin.repo.doError {
                            Text(text = "${it?.message}", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
