import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * 配置 Android 通用依赖
 */
internal fun Project.configureAndroidDependencies() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        // AndroidX 核心库
        add("implementation", libs.findLibrary("core-ktx").get())
        add("implementation", libs.findLibrary("appcompat").get())

        // 协程
        add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())

        // 测试
        add("testImplementation", libs.findBundle("testBundle").get())
        add("androidTestImplementation", libs.findBundle("androidTestBundle").get())
    }
}

/**
 * 配置 Compose 依赖
 */
internal fun Project.configureComposeDependencies() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        val composeBom = libs.findLibrary("compose-bom").get()
        add("implementation", platform(composeBom))
        add("implementation", libs.findLibrary("compose-material3").get())
        add("implementation", libs.findLibrary("ui-tooling-preview").get())

        // Compose 测试
        add("androidTestImplementation", platform(composeBom))
        add("androidTestImplementation", libs.findLibrary("ui-test-junit4").get())
        add("debugImplementation", libs.findLibrary("ui-tooling").get())
        add("debugImplementation", libs.findLibrary("ui-test-manifest").get())
    }
}
