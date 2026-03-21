import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.gradle.kotlin.dsl.configure

class KotlinAndroidConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                jvmToolchain(17)
                compilerOptions {
                    jvmTarget.set(ProjectConfig.JVM_TARGET)
                }
            }
        }
    }
}
