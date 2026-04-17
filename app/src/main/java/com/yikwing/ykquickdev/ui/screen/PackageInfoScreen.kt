package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yikwing.ykquickdev.components.LocalNavigator
import com.yikwing.ykquickdev.ui.theme.RubikGemstonesRegular
import com.yikwing.ykquickdev.ui.utils.navigate
import kotlinx.serialization.Serializable

@Serializable
data object PackageInfoRoute : NavKey

fun EntryProviderScope<NavKey>.packageInfoEntry() {
    entry<PackageInfoRoute> {
        val navigator = LocalNavigator.current
        PackageInfoScreen(
            navigationToPage = { id -> navigator.navigate(ProductRoute(id)) },
            navigationToDiy = { navigator.navigate(DiyInputRoute) },
            navigationToFoldable = { navigator.navigate(FoldableRoute) },
        )
    }
}

@Composable
fun PackageInfoScreen(
    navigationToPage: (str: String) -> Unit,
    navigationToDiy: () -> Unit,
    navigationToFoldable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .statusBarsPadding()
                .then(modifier),
    ) {
        Button(
            onClick = { navigationToPage("kotlin") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Text("to Other", fontFamily = RubikGemstonesRegular)
        }

        Button(
            onClick = navigationToDiy,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Text("to Diy", fontFamily = RubikGemstonesRegular)
        }

        Button(
            onClick = navigationToFoldable,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Text("to Foldable", fontFamily = RubikGemstonesRegular)
        }
    }
}
