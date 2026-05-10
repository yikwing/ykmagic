plugins {
    // Convention Plugins
    id("ykmagic.android.library")
    id("ykmagic.android.koin")

    alias(libs.plugins.kotlin.serialization)

    id("maven-publish")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    namespace = "com.yikwing.network"

    publishing {
        singleVariant("release") {}
    }
}

dependencies {
    implementation(libs.bundles.network.ktor)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.okhttp3)
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
