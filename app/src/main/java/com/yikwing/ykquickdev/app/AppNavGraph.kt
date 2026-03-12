package com.yikwing.ykquickdev.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.yikwing.ykquickdev.ui.screen.AuthLoginScreen
import com.yikwing.ykquickdev.ui.screen.AuthRegisterScreen
import com.yikwing.ykquickdev.ui.screen.DiyInputScreen
import com.yikwing.ykquickdev.ui.screen.MainScreen
import com.yikwing.ykquickdev.ui.screen.OtherPageScreen
import com.yikwing.ykquickdev.ui.screen.PackageInfoScreen
import com.yikwing.ykquickdev.ui.utils.navigate
import com.yikwing.ykquickdev.ui.utils.setRoot

/**
 * 应用导航图
 *
 * 使用 Navigation 3.x 实现类型安全的导航
 *
 * 核心配置：
 * - backStack: 导航栈，初始页面为 PackageInfoScreen
 * - entryDecorators: 状态保存和 ViewModel 生命周期管理
 * - predictivePopTransitionSpec: 预测性返回手势动画
 */
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(PackageInfoScreen)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        // 状态保存和 ViewModel 生命周期管理
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(), // 保存 UI 状态
                rememberViewModelStoreNavEntryDecorator(), // 管理 ViewModel 生命周期
            ),
        // 预测性返回手势动画（淡入淡出）
        predictivePopTransitionSpec = { _ ->
            ContentTransform(
                fadeIn(animationSpec = tween(400)),
                fadeOut(animationSpec = tween(400)),
            )
        },
        entryProvider =
            entryProvider {
                featureAEntryBuilder(backStack)
                authNavGraph(backStack)
            },
    )
}

/**
 * 定义导航目的地和路由
 *
 * 使用 entry<T> 注册每个导航目的地，T 为 NavKey 类型
 * 导航操作通过 backStack += 或 backStack.add() 实现
 */
fun EntryProviderScope<NavKey>.featureAEntryBuilder(backStack: NavBackStack<NavKey>) {
    entry<PackageInfoScreen> {
        PackageInfoScreen(
            navigationToPage = { id ->
                backStack.navigate(Product(id = id))
            },
            navigationToDiy = {
                backStack.navigate(DiyInputScreen)
            },
        )
    }

    entry<Product> { product ->
        OtherPageScreen(product.id)
    }

    entry<DiyInputScreen> {
        DiyInputScreen(
            navigationToAuth = {
                backStack.navigate(AuthDestination.Login)
            },
        )
    }

    entry<MainScreen> {
        MainScreen()
    }
}

fun EntryProviderScope<NavKey>.authNavGraph(backStack: NavBackStack<NavKey>) {
    entry<AuthDestination.Login> {
        AuthLoginScreen(
            navigationToRegister = { backStack.navigate(AuthDestination.Register) },
        )
    }

    entry<AuthDestination.Register> {
        AuthRegisterScreen(
            navigationToHome = { backStack.setRoot(MainScreen) },
        )
    }
}
