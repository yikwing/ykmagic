import com.google.gson.GsonBuilder
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.loadProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kotzilla)

    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.wire)

    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)

    id("com.github.ben-manes.versions") version "0.53.0"
}

// 读取config
val jsonEnvFile: File = rootProject.file("./android_env.json")
val injectJson = jsonEnvFile.readText()

// 读取签名文件
val keystorePropertiesPath: String = rootProject.file("keystore.properties").path
val keystoreProperties: Properties = loadProperties(keystorePropertiesPath)

// 资源重定向
fun listSubFile(): List<String> =
    listOf(
        "src/main/res/common",
        "src/main/res/activity",
        "src/main/res/fragment",
    )

// 获取当前打包时间
fun getDateStr(): String {
    val localDate = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return localDate.format(formatter)
}

// 获取version code
fun gitVersionCode(): Int {
    val cmd = "git rev-list HEAD --count"
    val process = ProcessBuilder(cmd.split(" ")).start()
    process.inputStream.bufferedReader().use { reader ->
        val output = reader.readLine()
        return (output?.trim()?.toInt() ?: 0) + 4645
    }
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
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.yikwing.ykquickdev"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.compileSdk
                .get()
                .toInt()
        versionCode = gitVersionCode()
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders.apply {
            put("debug_time", getDateStr())
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
            // 启用代码压缩、优化及混淆
            isMinifyEnabled = true
            // 启用资源压缩
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets.getByName("main") {
        res.setSrcDirs(listSubFile())
    }

    lint {
        checkDependencies = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("KOIN_CONFIG_CHECK", "true")
}

wire {
    sourcePath {
        srcDir("src/main/protos")
    }
    kotlin {
        android = false
    }
}

dependencies {
    // 官方依赖库
    implementation(libs.activity)
    implementation(libs.activity.ktx)
    implementation(libs.activity.compose)
    implementation(libs.fragment.ktx)
    implementation(libs.core.ktx)
    implementation(libs.collection.ktx)
    implementation(libs.appcompat)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.webkit)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // 三方依赖库
    implementation(libs.bundles.network.ktor)

    implementation(libs.kotlinx.coroutines.android)

//    implementation("com.github.yikwing.ykmagic:config:0.1.0")
//    implementation("com.github.yikwing.ykmagic:network:0.1.0")
//    implementation("com.github.yikwing.ykmagic:proxy:0.1.0")
//    implementation("com.github.yikwing.ykmagic:extension:0.1.0")
//    implementation("com.github.yikwing.ykmagic:permission:0.1.0")
//    implementation("com.github.yikwing.ykmagic:logger:0.1.0")
//    implementation("com.github.yikwing.ykmagic:datastore:0.1.0")

    arrayOf(
        ":module_config",
        ":module_network",
        ":module_proxy",
        ":module_extension",
        ":module_permission",
        ":module_logger",
        ":module_datastore",
        ":module_component",
    ).forEach { dep ->
        implementation(project(dep))
    }

//    // koin
//    implementation(platform(libs.koin.bom))
//    implementation(libs.koin.core)
//    implementation(libs.koin.android)
//    implementation(libs.koin.core.coroutines)
//    implementation(libs.koin.androidx.startup)
//    // Koin Annotations
//    implementation(libs.koin.annotations)
//    // Koin Annotations KSP Compiler
//    ksp(libs.koin.ksp.compiler)

    // compose依赖库
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material.icons.extended)
    implementation(libs.compose.material3)

    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.constraintlayout.compose)
    implementation(libs.navigation.compose)

    implementation(libs.coil.compose)

    // Debug依赖库
    debugImplementation(libs.glance)
    debugImplementation(libs.leakcanary.android)

    debugImplementation(libs.chuckerteam)
    releaseImplementation(libs.chuckerteam.no.op)

    // 测试依赖库
    testImplementation(libs.bundles.testBundle)
    androidTestImplementation(libs.bundles.androidTestBundle)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // -------------- hilt 代支持ksp 再合并 ----------------
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.kotzilla.sdk)

    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    implementation(libs.javax.inject)

    // https://juejin.cn/post/7079229035254906888
    implementation(libs.kotlinx.serialization.json.okio)

    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)
}
