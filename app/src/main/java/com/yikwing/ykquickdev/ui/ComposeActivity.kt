package com.yikwing.ykquickdev.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yikwing.ykquickdev.R
import com.yikwing.ykquickdev.app.AppNavGraph
import com.yikwing.ykquickdev.app.TabDestination
import com.yikwing.ykquickdev.ui.base.NoIndication

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
data class BottomNavItem<out T>(
    @DrawableRes val normalIcon: Int,
    @DrawableRes val selectedIcon: Int,
    val route: T,
)

val BottomNavItems =
    listOf(
        BottomNavItem(
            R.drawable.ic_contacts_outlined,
            R.drawable.ic_contacts_filled,
            TabDestination.Home,
        ),
        BottomNavItem(R.drawable.ic_me_outlined, R.drawable.ic_me_filled, TabDestination.Mine),
    )

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalIndication provides NoIndication) {
                // 1. 创建 NavController
                val navController = rememberNavController()

                // 2. 观察当前路由状态
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // 判断是否显示 bottomBar（仅在 Tab 页面显示）
                val shouldShowBottomBar =
                    BottomNavItems.any { item ->
                        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                    }

//            Scaffold(
//                bottomBar = {
//                    if (shouldShowBottomBar) {
//                        CustomBottomBar(
//                            currentDestination = currentDestination,
//                            onItemSelected = { route ->
//
//                                // 3. 导航逻辑：避免重复入栈，保存状态
//                                navController.navigate(route) {
//                                    popUpTo(navController.graph.findStartDestination().id) {
//                                        saveState = true
//                                    }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                        )
//                    }
//                },
//            ) {
//                AppNavGraph(navController = navController)
//            }

                ConstraintLayout(
                    constraintSet = decoupledConstraints(),
                ) {
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.layoutId("appNav"),
                    )

                    if (shouldShowBottomBar) {
                        CustomBottomBar(
                            currentDestination = currentDestination,
                            onItemSelected = { route ->

                                // 3. 导航逻辑：避免重复入栈，保存状态
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.navigationBarsPadding().layoutId("bottomBar"),
                        )
                    }
                }
            }
        }
    }
}

private fun decoupledConstraints(): ConstraintSet =
    ConstraintSet {
        val appNav = createRefFor("appNav")
        val bottomBar = createRefFor("bottomBar")

        constrain(appNav) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(bottomBar.top)
        }

        constrain(bottomBar) {
            width = Dimension.fillToConstraints
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom, 10.dp)
        }
    }

@Composable
fun CustomBottomBar(
    currentDestination: NavDestination?,
    onItemSelected: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(60.5.dp)
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.8f))
                .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItems.forEach { item ->
            val isSelected =
                currentDestination?.hierarchy?.any {
                    it.hasRoute(item.route::class)
                } == true

            Column(
                modifier =
                    Modifier.weight(1f).clickable(
                        onClick = { onItemSelected(item.route) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(if (isSelected) item.selectedIcon else item.normalIcon),
                    null,
                    tint = if (isSelected) Color(0xFF2BA245) else Color.Black,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CustomBottomBarPreView() {
    CustomBottomBar(
        currentDestination = null,
        onItemSelected = {},
    )
}
