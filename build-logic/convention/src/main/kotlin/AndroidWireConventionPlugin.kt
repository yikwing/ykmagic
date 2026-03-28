import com.squareup.wire.gradle.WireExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidWireConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.squareup.wire")

            extensions.configure<WireExtension> {
                sourcePath {
                    srcDir("src/main/protos")
                }
                kotlin {
                    // Wire 消息用于 DataStore proto schema，不需要 Parcelable 实现
                    // 若未来需要在 Bundle/Intent 中传递 Wire 对象，改为 android = true
                    android = false
                }
            }
        }
    }
}
