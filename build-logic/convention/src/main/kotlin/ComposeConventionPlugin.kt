import com.android.build.api.dsl.CommonExtension
import com.yikwing.ykmagic.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val commonExtension =
                extensions.findByType(CommonExtension::class.java)
                    ?: error("ykmagic.android.compose 必须在 Android 插件（application/library）之后应用")
            configureAndroidCompose(commonExtension)
        }
    }
}
