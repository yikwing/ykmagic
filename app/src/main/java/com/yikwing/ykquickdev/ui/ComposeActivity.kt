package com.yikwing.ykquickdev.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yikwing.ykquickdev.app.AppNavGraph
import com.yikwing.ykquickdev.app.DiyInputScreen
import com.yikwing.ykquickdev.app.PackageInfoScreen
import com.yikwing.ykquickdev.ui.theme.ComposeTheme

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
    val icon: ImageVector,
    val label: String,
    val route: T,
)

val BottomNavItems =
    listOf(
        BottomNavItem(Icons.Default.Home, "首页", PackageInfoScreen),
        BottomNavItem(Icons.Default.Person, "我的", DiyInputScreen),
    )

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. 创建 NavController
            val navController = rememberNavController()

            // 2. 观察当前路由状态
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            ComposeTheme {
                Scaffold(
                    bottomBar = {
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
                        )
                    },
                ) { innerPadding ->
                    // 4. 应用 PaddingValues
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    currentDestination: NavDestination?,
    onItemSelected: (Any) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding() // 适配全面屏手势区域
                .height(64.dp),
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
                    Modifier
                        .weight(1f)
                        // 5. 使用 selectable 替代 clickable，提供更好的语义
                        .selectable(
                            selected = isSelected,
                            onClick = { onItemSelected(item.route) },
                            role = Role.Tab,
                            indication = ripple(), // 添加点击波纹
                            interactionSource = remember { MutableInteractionSource() },
                        ).padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 动画过渡颜色
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF1147EF) else Color(0xFF666666),
                    label = "color",
                )

                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isSelected) 28.dp else 24.dp),
                    tint = contentColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
            }
        }
    }
}
