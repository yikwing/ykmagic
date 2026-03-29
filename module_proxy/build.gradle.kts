plugins {
    // Convention Plugins
    id("ykmagic.android.library")

    id("maven-publish")
}

android {
    namespace = "com.yikwing.proxy"

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
        viewBinding = true
    }

    publishing {
        singleVariant("release") {}
    }
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
