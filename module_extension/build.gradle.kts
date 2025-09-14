plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

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

    namespace = "com.yikwing.extension"

    // 添加以下代码块
    publishing {
        singleVariant("release") {}
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.exifinterface)

    implementation(libs.moshi.kotlin)
    implementation(libs.lifecycle.runtime.compose)

    testImplementation(libs.bundles.testBundle)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "extension"
                version = "1.0.1"

                from(components["release"])
            }
        }
    }
}
