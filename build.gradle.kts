// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.wire) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

// 强制制定依赖
allprojects {
    configurations.all {
        resolutionStrategy {
            force(libs.kotlinx.coroutines.core)
        }
    }
}
