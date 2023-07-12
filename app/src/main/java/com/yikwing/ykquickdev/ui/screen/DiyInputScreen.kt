package com.yikwing.ykquickdev.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yikwing.ykquickdev.components.VerificationCodeTextField

@Composable
fun DiyInputWrapperScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.height(60.dp))
            VerificationCodeTextField {
                Log.d("DiyInputScreen", "===== $it")
            }
        }
    }
}
