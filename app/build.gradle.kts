import com.google.gson.GsonBuilder
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.loadProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    // Convention Plugins
    id("ykmagic.android.application")
    id("ykmagic.android.compose")

    id("kotlin-parcelize")

    id("ykmagic.android.wire")

    id("ykmagic.android.room")
    id("ykmagic.android.koin")
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.hotswan.compiler)
}

// 读取config
val jsonEnvFile: File = rootProject.file("./android_env.json")
val injectJson = jsonEnvFile.readText()

// 读取签名文件
val keystorePropertiesPath: String = rootProject.file("keystore.properties").path
val keystoreProperties: Properties = loadProperties(keystorePropertiesPath)

// 获取当前打包时间
fun getDateStr(): String {
    val localDate = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return localDate.format(formatter)
}

// 获取version code
// Use providers.exec so the git call is a configuration-cache aware build input
// instead of an unsupported configuration-time external process.
fun gitVersionCode(): Int {
    val output =
        providers
            .exec { commandLine("git", "rev-list", "HEAD", "--count") }
            .standardOutput
            .asText
            .get()
    return (output.trim().toIntOrNull() ?: 0) + 4645
}

// 获取最近五条git日志
fun getGitLog(): String {
    val cmd = "git log --oneline -5"
    val process = ProcessBuilder(cmd.split(" ")).start()
    process.inputStream.bufferedReader().use { reader ->
        val outputs = reader.readLines()
        return outputs.joinToString(separator = "") { it + "\n" }
    }
}

// json格式化
fun getJsonStr(): String {
    val json =
        with(GsonBuilder()) {
            setPrettyPrinting()
            create()
        }
    return json.toJson(injectJson)
}

android {
    namespace = "com.yikwing.ykquickdev"

    defaultConfig {
        applicationId = "com.yikwing.ykquickdev"
        versionCode = gitVersionCode()
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders.apply {
            put("debug_time", getDateStr())
        }

        ndk {
            abiFilters += "arm64-v8a"
        }

        androidResources {
            localeFilters += listOf("en")
        }
    }

    signingConfigs {
        create("config") {
            storeFile = rootProject.file(keystoreProperties["storeFile"].toString())
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
            storePassword = keystoreProperties["storePassword"].toString()
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("config")
            buildConfigField(
                "String",
                "YK_CONFIG",
                getJsonStr(),
            )
        }

        release {
            signingConfig = signingConfigs.getByName("config")
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField(
                "String",
                "YK_CONFIG",
                getJsonStr(),
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        checkDependencies = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_reports")
    metricsDestination = layout.buildDirectory.dir("compose_metrics")
}

dependencies {
    // 官方依赖库
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.webkit)

    // 三方依赖库
    implementation(libs.bundles.network.ktor)

    arrayOf(
        ":module_config",
        ":module_network",
        ":module_proxy",
        ":module_extension",
        ":module_permission",
        ":module_compose",
    ).forEach { dep ->
        implementation(project(dep))
    }

    // compose material-icons-extended
    implementation(libs.material.icons.extended)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)

    // Debug依赖库
    debugImplementation(libs.glance)
    debugImplementation(libs.leakcanary.android)

    debugImplementation(libs.chuckerteam)
    releaseImplementation(libs.chuckerteam.no.op)

    // compose viewmodel 依赖
    implementation(libs.koin.compose.viewmodel)

    // https://juejin.cn/post/7079229035254906888
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okio)
}
