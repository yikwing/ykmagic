import com.android.build.api.dsl.ApplicationExtension
import com.yikwing.ykmagic.configureAndroidDependencies
import com.yikwing.ykmagic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig.targetSdk = ProjectConfig.TARGET_SDK

                // Application 模块默认启用 BuildConfig
                configureKotlinAndroid(this, enableBuildConfig = true)
            }

            // 配置通用依赖
            configureAndroidDependencies()
        }
    }
}
