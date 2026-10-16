package com.yikwing.ykquickdev.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.yikwing.ykquickdev.R
import com.yikwing.compose.layout.sdp

// Declaration order is the pager page order
enum class MainTab(
    @param:DrawableRes val normalIcon: Int,
    @param:DrawableRes val selectedIcon: Int,
    // Page has a light background: needs dark status bar icons and a dark bottom bar for contrast
    val isLightPage: Boolean = false,
) {
    Home(
        R.drawable.ic_tab_home_outlined,
        R.drawable.ic_tab_home_filled,
    ),
    Category(
        R.drawable.ic_tab_category_outlined,
        R.drawable.ic_tab_category_filled,
    ),
    Cart(
        R.drawable.ic_tab_cart_outlined,
        R.drawable.ic_tab_cart_filled,
        isLightPage = true,
    ),
    Me(R.drawable.ic_tab_me_outlined, R.drawable.ic_tab_me_filled),
}

@Composable
fun CustomBottomBar(
    currentPageIndex: Int,
    onItemSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .navigationBarsPadding()
                .padding(bottom = 8.sdp)
                .height(60.5f.sdp)
                .padding(horizontal = 10.sdp)
                .clip(RoundedCornerShape(30.sdp))
                .background(
                    if (MainTab.entries[currentPageIndex].isLightPage) {
                        Color(0x33000000)
                    } else {
                        Color(0x33FFFFFF)
                    },
                ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MainTab.entries.forEachIndexed { index, item ->

            val selected = index == currentPageIndex

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onItemSelect(index)
                                },
                            )
                        },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(if (selected) item.selectedIcon else item.normalIcon),
                    null,
                    tint = if (selected) Color(0xFF2BA245) else Color.Black,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CustomBottomBarPreview() {
    CustomBottomBar(
        currentPageIndex = 1,
        onItemSelect = {},
    )
}
