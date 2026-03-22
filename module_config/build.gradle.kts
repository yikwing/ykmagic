plugins {
    // Convention Plugins
    id("ykmagic.android.library")

    alias(libs.plugins.kotlin.serialization)

    id("maven-publish")
}

android {
    namespace = "com.yikwing.config"

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

    publishing {
        singleVariant("release") {}
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.testBundle)
    androidTestImplementation(libs.bundles.androidTestBundle)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "config"
                version = "1.0.1"

                from(components["release"])
            }
        }
    }
}
