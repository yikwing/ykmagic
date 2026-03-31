plugins {
    // Convention Plugins
    id("ykmagic.android.library")
    id("ykmagic.android.compose")

    id("maven-publish")
}

android {
    namespace = "com.yikwing.compose"

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
    implementation(libs.coil.compose)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "proxy"
                version = "1.0.1"

                from(components["release"])
            }
        }
    }
}
