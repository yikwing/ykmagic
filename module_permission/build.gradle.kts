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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    namespace = "com.yikwing.permission"

    // 添加以下代码块
    publishing {
        singleVariant("release") {}
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)

    testImplementation(libs.bundles.testBundle)
    androidTestImplementation(libs.bundles.androidTestBundle)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "permission"
                version = "1.0.1"

                from(components["release"])
            }
        }
    }
}
