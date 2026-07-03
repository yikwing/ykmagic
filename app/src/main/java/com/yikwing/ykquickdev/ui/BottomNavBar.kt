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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yikwing.ykquickdev.R
import com.yikwing.ykquickdev.ui.utils.sdp

@Immutable
data class BottomNavItem(
    @param:DrawableRes val normalIcon: Int,
    @param:DrawableRes val selectedIcon: Int,
)

val BottomNavItems =
    listOf(
        BottomNavItem(R.drawable.ic_chat_outlined, R.drawable.ic_chat_filled),
        BottomNavItem(R.drawable.ic_contacts_outlined, R.drawable.ic_contacts_filled),
        BottomNavItem(R.drawable.ic_discovery_outlined, R.drawable.ic_discovery_filled),
        BottomNavItem(R.drawable.ic_me_outlined, R.drawable.ic_me_filled),
    )

// CartRoute（索引 2）背景为白色，底栏需深色背景以保持对比度
private const val DARK_BACKGROUND_PAGE_INDEX = 2

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
                .padding(bottom = 10.sdp)
                .height(60.5f.sdp)
                .padding(horizontal = 10.sdp)
                .clip(RoundedCornerShape(30.sdp))
                .background(
                    if (currentPageIndex == DARK_BACKGROUND_PAGE_INDEX) {
                        Color(0x33000000)
                    } else {
                        Color(0x33FFFFFF)
                    },
                ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItems.forEachIndexed { index, item ->

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
