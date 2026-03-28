/**
 * 项目级构建配置常量
 * 集中管理所有 Convention Plugins 使用的配置值
 */
object ProjectConfig {
    // SDK 版本
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36

    /**
     * Kotlin 编译器选项
     */
    object CompilerOptions {
        val FREE_COMPILER_ARGS = emptyList<String>()
    }
}
