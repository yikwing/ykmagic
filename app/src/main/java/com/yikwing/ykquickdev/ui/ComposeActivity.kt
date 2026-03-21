package com.yikwing.ykquickdev.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.yikwing.ykquickdev.app.AppNavGraph
import com.yikwing.ykquickdev.ui.utils.ProvideDesignScale
import com.yikwing.ykquickdev.viewmodel.WanAndroidViewModel
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

/**
 * <pre>
 *     author: yikwing
 *
 *        _ _              _
 *       (_) |            (_)
 *  _   _ _| | ____      ___ _ __   __ _
 * | | | | | |/ /\ \ /\ / / | '_ \ / _` |
 * | |_| | |   <  \ V  V /| | | | | (_| |
 *  \__, |_|_|\_\  \_/\_/ |_|_| |_|\__, |
 *   __/ |                          __/ |
 *  |___/                          |___/
 *
 *     email : 49999@live.com
 *     time  : 2025-12-17 22:17
 *     desc  :
 * </pre>
 */
// 定义数据类，避免在 UI 中硬编码
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

class ComposeActivity : ComponentActivity() {
    private val wanAndroidViewModel by inject<WanAndroidViewModel>(
        parameters = { parametersOf("ComposeActivity") },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle =
                SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
        )

        // 2. 关键修复：关闭系统强制的导航栏遮罩 (仅 API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            ProvideDesignScale {
                AppNavGraph()
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    currentPageIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .navigationBarsPadding()
                .padding(bottom = 10.dp)
                .height(60.5.dp)
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(if (currentPageIndex == 2) Color(0x33000000) else Color(0x33FFFFFF)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItems.forEachIndexed { index, item ->

            val selected = index == currentPageIndex

            Column(
                modifier =
                    Modifier.weight(1f).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onItemSelected(index)
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
private fun CustomBottomBarPreView() {
    CustomBottomBar(
        currentPageIndex = 1,
        onItemSelected = {},
    )
}
