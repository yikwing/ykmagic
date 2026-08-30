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
                // 默认 "warning" 会把组件发现日志全部输出为编译警告，淹没真实告警
                logSeverity.set("info")
            }

            // K2 增量编译不追踪 module 内定义变更和 @ComponentScan 包内的新增类，
            // 聚合模块可能被误判 UP-TO-DATE 而沿用过期的依赖图。
            // 插件的自动检测并不总能覆盖（改注解时实测漏判），故对 application 模块显式强制全图校验。
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<KoinGradleExtension> {
                    strictSafety.set(true)
                }
            }

            dependencies {
                add("implementation", platform(libs.findLibrary("koin-bom").get()))
                add("implementation", libs.findLibrary("koin-android").get())
                add("implementation", libs.findLibrary("koin-compose").get())
                add("implementation", libs.findLibrary("koin-annotations").get())
            }
        }
    }
}
