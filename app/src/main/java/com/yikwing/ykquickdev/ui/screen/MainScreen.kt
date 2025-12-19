package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yikwing.ykquickdev.components.Center
import com.yikwing.ykquickdev.ui.BottomNavItems
import com.yikwing.ykquickdev.ui.CustomBottomBar
import com.yikwing.ykquickdev.ui.widget.SystemBarsStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainScreen(coroutineScope: CoroutineScope = rememberCoroutineScope()) {
    var currentPageIndex by remember { mutableStateOf(0) }

    // 创建分页器状态
    val pagerState =
        rememberPagerState(
            initialPage = currentPageIndex,
        ) {
            BottomNavItems.size
        }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
        bottomBar = {
            CustomBottomBar(currentPageIndex, { targetPage ->
                currentPageIndex = targetPage
                coroutineScope.launch {
//                    pagerState.animateScrollToPage(targetPage) // 带动画
                    pagerState.scrollToPage(targetPage) // 无动画
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
    SystemBarsStyle(darkIcons = false)
    Center(
        modifier = Modifier.fillMaxSize().background(KleinBlue),
    ) {
        Text("Home", color = Color.White)
    }
}

@Composable
fun CategoryRoute(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().background(BordeauxRed),
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
                    .padding(bottom = paddingValues.calculateBottomPadding() + 10.dp)
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.3f)),
        ) {
            // Row 内容
        }
    }
}

@Composable
fun CartRoute() {
    SystemBarsStyle(darkIcons = true)
    Center(
        modifier = Modifier.fillMaxSize().background(Color.White),
    ) {
        Text("Cart", color = Color.Black)
    }
}

@Composable
fun MeRoute() {
    SystemBarsStyle(darkIcons = false)
    Center(
        modifier = Modifier.fillMaxSize().background(Color.Black),
    ) {
        Text("Me", color = Color.White)
    }
}
