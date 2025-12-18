package com.yikwing.ykquickdev.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.jetpackcompose.widget.ConstraintPage
import com.example.jetpackcompose.widget.MyMaterial3ModalBottomSheetExample
import com.yikwing.ykquickdev.components.Center
import com.yikwing.ykquickdev.ui.screen.AuthLoginScreen
import com.yikwing.ykquickdev.ui.screen.AuthRegisterScreen
import com.yikwing.ykquickdev.ui.screen.DiyInputScreen
import com.yikwing.ykquickdev.ui.screen.OtherPageScreen
import com.yikwing.ykquickdev.ui.screen.PackageInfoScreen
import com.yikwing.ykquickdev.ui.widget.GradientPage
import com.yikwing.ykquickdev.ui.widget.UIRoot

fun NavGraphBuilder.tabNavGraph(
    modifier: Modifier = Modifier,
    navActions: AppNavigationActions,
) {
    navigation<TabDestination.TabNavigation>(
        startDestination = TabDestination.Home,
    ) {
        composable<TabDestination.Home> {
            Center(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
            ) {
                Text(
                    "Home",
                    color = Color.Black,
                    modifier =
                        modifier
                            .background(Color.Yellow)
                            .clickable {
                                navActions.navigationToA()
                            },
                )
            }
        }

        composable<TabDestination.Mine> {
            Center(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
            ) {
                Text("Mine", color = Color.White)
            }
        }
    }
}

fun NavGraphBuilder.authNavGraph(navActions: AppNavigationActions) {
    navigation<AuthDestination.AuthNavigation>(
        startDestination = AuthDestination.Login,
    ) {
        composable<AuthDestination.Login> {
            AuthLoginScreen(
                navigationToRegister = navActions::navigationToRegister,
            )
        }

        composable<AuthDestination.Register> {
            AuthRegisterScreen(
                navigationToHome = navActions::navigationToHome,
            )
        }
    }
}

fun NavGraphBuilder.uiNavGraph(
    modifier: Modifier = Modifier,
    navActions: AppNavigationActions,
) {
    navigation<UIDestination.UINavigation>(
        startDestination = UIDestination.UIRoot,
    ) {
        composable<UIDestination.UIRoot> {
            UIRoot(
                navigationToGradient = navActions::navigationToGradient,
                navigationToBSheet = navActions::navigationToBSheet,
                navigationToConstraint = navActions::navigationToConstraint,
                modifier = modifier,
            )
        }

        composable<UIDestination.Gradient> {
            GradientPage()
        }

        composable<UIDestination.BSheet> {
            MyMaterial3ModalBottomSheetExample()
        }

        composable<UIDestination.ConstraintPage> {
            ConstraintPage(modifier = modifier)
        }
    }
}

fun NavGraphBuilder.mainScreens(
    modifier: Modifier = Modifier,
    navActions: AppNavigationActions,
) {
    composable<PackageInfoScreen> {
        PackageInfoScreen(
            navigationToPage = navActions::navigationToA,
            navigationToDiy = navActions::navigationToDiyInput,
            modifier = modifier,
        )
    }

    composable<Product> { backStackEntry ->
        val product: Product = backStackEntry.toRoute()
        OtherPageScreen(product.id, navActions::navigationToUI)
    }

    composable<DiyInputScreen> {
        DiyInputScreen(
            navigationToAuth = navActions::navigationToAuth,
        )
    }
}
