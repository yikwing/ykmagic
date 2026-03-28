plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.wire) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room3) apply false
    alias(libs.plugins.koin.compiler) apply false
}

// 强制指定依赖版本
allprojects {
    configurations.configureEach {
        resolutionStrategy {
            force(libs.activity)
            force(libs.kotlinx.coroutines.core)
        }
    }
}
