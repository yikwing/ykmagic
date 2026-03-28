import com.yikwing.ykmagic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.koin.compiler.plugin.KoinGradleExtension

class AndroidKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.insert-koin.compiler.plugin")

            extensions.configure<KoinGradleExtension> {
                userLogs.set(true)
            }

            dependencies {
                add("implementation", platform(libs.findLibrary("koin-bom").get()))
                add("implementation", libs.findLibrary("koin-android").get())
                add("implementation", libs.findLibrary("koin-annotations").get())
            }
        }
    }
}
