package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.yikwing.ykquickdev.ui.utils.sdp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yikwing.compose.window.SystemBarsStyle
import com.yikwing.ykquickdev.components.Center
import com.yikwing.ykquickdev.ui.BottomNavItems
import com.yikwing.ykquickdev.ui.CustomBottomBar
import com.yikwing.ykquickdev.ui.utils.sdp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object MainRoute : NavKey

fun EntryProviderScope<NavKey>.mainScreenEntry() {
    entry<MainRoute> {
        MainScreen()
    }
}

@Composable
fun MainScreen(coroutineScope: CoroutineScope = rememberCoroutineScope()) {
    val pagerState =
        rememberPagerState {
            BottomNavItems.size
        }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
        bottomBar = {
            CustomBottomBar(pagerState.currentPage, { targetPage ->
                coroutineScope.launch {
                    pagerState.scrollToPage(targetPage)
                }
            })
        },
    ) { paddingValues ->
        MainScreenContentView(pagerState, paddingValues)
    }
}

@Composable
fun MainScreenContentView(
    pageState: PagerState,
    // bottomBar 的padding
    paddingValues: PaddingValues,
) {
    HorizontalPager(
        state = pageState,
        userScrollEnabled = false, // 禁止手势滑动
        modifier = Modifier.fillMaxSize(), // 忽略 paddingValues，占满全屏
    ) { page: Int ->
        when (page) {
            0 -> {
                HomeRoute()
            }

            1 -> {
                CategoryRoute(paddingValues)
            }

            2 -> {
                CartRoute()
            }

            3 -> {
                MeRoute()
            }
        }
    }
}

// 克莱因蓝 (Klein Blue) - 伊夫·克莱因标志性蓝色
private val KleinBlue = Color(0xFF002FA7)

// 波尔多红 (Bordeaux Red) - 法国波尔多葡萄酒色
private val BordeauxRed = Color(0xFF6D2C41)

@Composable
fun HomeRoute() {
    SystemBarsStyle(statusBarDarkIcons = false)
    Center(
        modifier =
            Modifier
                .fillMaxSize()
                .background(KleinBlue),
    ) {
        Text("Home", color = Color.White)
    }
}

@Composable
fun CategoryRoute(paddingValues: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BordeauxRed),
    ) {
        // 主内容居中
        Text(
            text = "Category",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center),
        )
        // Row 固定在底部，避开 bottomBar
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 10.sdp)
                    .fillMaxWidth()
                    .height(100.sdp)
                    .clip(RoundedCornerShape(16.sdp))
                    .background(Color.White.copy(alpha = 0.3f)),
        ) {
            // Row 内容
        }
    }
}

@Composable
fun CartRoute() {
    SystemBarsStyle(statusBarDarkIcons = true)
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        ConstraintLayout(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(100.sdp)
                    .align(Alignment.Center),
            constraintSet = decoupledConstraints(),
        ) {
            Spacer(
                modifier =
                    Modifier
                        .layoutId(CartLayoutId.Start)
                        .size(100.sdp, 50.sdp)
                        .background(Color.Yellow),
            )

            Spacer(
                modifier =
                    Modifier
                        .layoutId(CartLayoutId.Center)
                        .size(120.sdp, 50.sdp)
                        .background(Color.Red),
            )

            Spacer(
                modifier =
                    Modifier
                        .layoutId(CartLayoutId.End)
                        .size(155.sdp, 50.sdp)
                        .background(Color.Blue),
            )
        }
    }
}

private fun decoupledConstraints(): ConstraintSet =
    ConstraintSet {
        val startRef = createRefFor(CartLayoutId.Start)
        val centerRef = createRefFor(CartLayoutId.Center)
        val endRef = createRefFor(CartLayoutId.End)

        constrain(startRef) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }

        constrain(centerRef) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(startRef.end)
        }

        constrain(endRef) {
            bottom.linkTo(parent.bottom)
            end.linkTo(parent.end)
        }
    }

@Composable
fun MeRoute() {
    SystemBarsStyle(statusBarDarkIcons = false)
    Center(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        Text("Me", color = Color.White)
    }
}

private enum class CartLayoutId {
    Start,
    Center,
    End,
}

@Preview(
    name = "UI-375",
    widthDp = 375,
    showBackground = true,
)
@Composable
private fun MainScreenPreview() {
    CartRoute()
}
