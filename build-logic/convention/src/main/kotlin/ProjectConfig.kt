import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * 项目级构建配置常量
 * 集中管理所有 Convention Plugins 使用的配置值
 */
object ProjectConfig {
    // SDK 版本
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36

    // Java 版本
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17

    /**
     * 构建特性默认配置
     */
    object BuildFeatures {
        /**
         * BuildConfig 默认配置
         * - Application 模块：默认启用
         * - Library 模块：默认不启用，按需在模块中显式启用
         */
        const val BUILD_CONFIG_DEFAULT = false
        const val VIEW_BINDING_DEFAULT = false
        const val COMPOSE_DEFAULT = false
    }

    /**
     * Kotlin 编译器选项
     */
    object CompilerOptions {
        val FREE_COMPILER_ARGS = emptyList<String>()
    }
}
