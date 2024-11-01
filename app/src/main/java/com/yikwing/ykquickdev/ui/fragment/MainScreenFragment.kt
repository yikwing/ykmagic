package com.yikwing.ykquickdev.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yikwing.network.RequestState
import com.yikwing.network.doError
import com.yikwing.network.doSuccess
import com.yikwing.ykquickdev.MyViewModel
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.components.LoadingWidget
import com.yikwing.ykquickdev.components.NetWorkError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreenFragment : Fragment() {
    private var forActivityResultLauncher =
        registerForActivityResult(
            object : ActivityResultContract<String, String>() {
                override fun createIntent(
                    context: Context,
                    input: String,
                ): Intent = Intent(Intent.ACTION_VIEW, Uri.parse("yikwing://yk:9001/props?$input"))

                override fun parseResult(
                    resultCode: Int,
                    intent: Intent?,
                ): String =
                    if (resultCode == Activity.RESULT_OK) {
                        intent?.getStringExtra("result") ?: "empty"
                    } else {
                        ""
                    }
            },
        ) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    ) { innerPadding ->
                        ItemDepot(innerPadding, forActivityResultLauncher)
                    }
                }
            }
        }
}

@Composable
fun ItemDepot(
    innerPadding: PaddingValues,
    forActivityResultLauncher: ActivityResultLauncher<String>,
    mainViewModel: MyViewModel = viewModel(),
) {
    val data by mainViewModel.wanAndroidList.collectAsState()

    when (data) {
        is RequestState.Loading -> {
            LoadingWidget(modifier = Modifier.fillMaxSize())
        }

        is RequestState.Success<List<ChapterBean>?> -> {
            data.doSuccess {
                it?.let {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = innerPadding.calculateTopPadding()),
                    ) {
                        LazyColumn {
                            items(it, key = {
                                it.id
                            }) {
                                Box(
                                    modifier =
                                        Modifier
                                            .height(50.dp)
                                            .fillParentMaxWidth()
                                            .clickable {
                                                forActivityResultLauncher.launch("matchId=222&time=10001")
                                            }.padding(start = 20.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(
                                        text = it.name,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is RequestState.Error -> {
            data.doError { throwable ->
                NetWorkError(throwable, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
