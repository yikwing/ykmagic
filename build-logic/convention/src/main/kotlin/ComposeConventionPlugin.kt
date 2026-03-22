import com.android.build.api.dsl.CommonExtension
import com.yikwing.ykmagic.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // 配置 Compose
            val commonExtension = extensions.findByType(CommonExtension::class.java)
            commonExtension?.let {
                configureAndroidCompose(it)
            }
        }
    }
}
