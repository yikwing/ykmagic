plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)

    id("maven-publish")
}

android {
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // 启用资源压缩
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

    namespace = "com.yikwing.network"

    // 添加以下代码块
    publishing {
        singleVariant("release") {}
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)

    implementation(libs.bundles.network.okhttp)

    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.kotlinx.serialization.json.okio)

    testImplementation(libs.bundles.testBundle)
    androidTestImplementation(libs.bundles.androidTestBundle)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "network"
                version = "1.0.1"

                from(components["release"])
            }
        }
    }
}
