import com.android.build.api.dsl.LibraryExtension
import com.yikwing.ykmagic.ProjectConfig
import com.yikwing.ykmagic.configureAndroidDependencies
import com.yikwing.ykmagic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                defaultConfig.minSdk = ProjectConfig.MIN_SDK

                // Library 模块默认不启用 BuildConfig
                // 需要的模块在自己的 build.gradle.kts 中显式启用
                configureKotlinAndroid(this, enableBuildConfig = false)
            }

            // 配置通用依赖
            configureAndroidDependencies()
        }
    }
}
