plugins {
    // Convention Plugins
    id("ykmagic.android.library")

    id("maven-publish")
}

android {
    namespace = "com.yikwing.datastore"

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
    // Preferences DataStore
    implementation(libs.datastore.preferences)

    testImplementation(libs.bundles.testBundle)
    androidTestImplementation(libs.bundles.androidTestBundle)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "datastore"
                version = "1.0.1"

                from(components["release"])
            }
        }
    }
}
