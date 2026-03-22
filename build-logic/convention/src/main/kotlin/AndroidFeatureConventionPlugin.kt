import com.yikwing.ykmagic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Android Feature 模块构建插件
 *
 * 功能：
 * - 应用 Android Library 和 Compose 配置
 * - 自动添加 Feature 模块通用依赖（Koin、Navigation3、Lifecycle）
 *
 * 使用场景：
 * - 功能模块（如 feature_home, feature_user）
 * - 包含 UI 和业务逻辑的独立功能单元
 *
 * 注意：
 * - 项目特定依赖（如 module_extension, module_network）需在各 Feature 模块自行添加
 * - 这样可以避免不必要的依赖和模块耦合
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 应用基础插件
            pluginManager.apply {
                apply("ykmagic.android.library")
                apply("ykmagic.android.compose")
            }

            // 配置 Feature 模块通用依赖
            dependencies {
                // Koin 依赖注入（Feature 模块通常需要）
                add("implementation", libs.findLibrary("koin-android").get())
                add("implementation", libs.findLibrary("koin-androidx-compose").get())
                add("implementation", libs.findLibrary("koin-compose-viewmodel").get())

                // Navigation3 导航（Feature 模块通常需要）
                add("implementation", libs.findLibrary("androidx-navigation3-compose").get())

                // Lifecycle 组件（Feature 模块通常需要）
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            }
        }
    }
}