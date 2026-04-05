plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.ksp) apply false

    // 以下插件由 Convention Plugins 负责应用，此处声明以确保 JAR 进入主构建 classpath，
    // 避免 Convention Plugin 访问扩展类时出现 classloader 隔离问题
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.room3) apply false
    alias(libs.plugins.koin.compiler) apply false

    alias(libs.plugins.hotswan.compiler) apply false
}
