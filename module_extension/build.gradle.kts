plugins {
    // Convention Plugins
    id("ykmagic.android.library")
    id("ykmagic.android.koin")

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

    namespace = "com.yikwing.extension"

    publishing {
        singleVariant("release") {}
    }
}

dependencies {
    implementation(libs.androidx.exifinterface)
    api(libs.androidx.window)

    implementation(libs.okio)
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
