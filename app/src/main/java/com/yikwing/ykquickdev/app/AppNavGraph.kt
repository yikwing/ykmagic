package com.yikwing.ykquickdev.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.yikwing.ykquickdev.components.LocalNavigator
import com.yikwing.ykquickdev.ui.screen.TextDebounceRoute
import com.yikwing.ykquickdev.ui.screen.authLoginEntry
import com.yikwing.ykquickdev.ui.screen.authRegisterEntry
import com.yikwing.ykquickdev.ui.screen.diyInputEntry
import com.yikwing.ykquickdev.ui.screen.mainScreenEntry
import com.yikwing.ykquickdev.ui.screen.otherPageEntry
import com.yikwing.ykquickdev.ui.screen.packageInfoEntry
import com.yikwing.ykquickdev.ui.screen.textDebounceEntry

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(TextDebounceRoute)

    CompositionLocalProvider(LocalNavigator provides backStack) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            predictivePopTransitionSpec = { _ ->
                ContentTransform(
                    fadeIn(animationSpec = tween(400)),
                    fadeOut(animationSpec = tween(400)),
                )
            },
            entryProvider =
                entryProvider {
                    textDebounceEntry()
                    packageInfoEntry()
                    otherPageEntry()
                    diyInputEntry()
                    mainScreenEntry()
                    authLoginEntry()
                    authRegisterEntry()
                },
        )
    }
}
