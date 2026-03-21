import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            // Configure compose for both Application and Library
            extensions.findByType(CommonExtension::class.java)?.apply {
                buildFeatures {
                    compose = true
                }
            }

            // 配置 Compose 依赖
            configureComposeDependencies()
        }
    }
}
