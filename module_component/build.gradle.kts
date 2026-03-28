plugins {
    // Convention Plugins
    id("ykmagic.android.library")

    id("maven-publish")
}

android {
    namespace = "com.yikwing.component"

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
    implementation(project(":module_extension"))
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "component"
                version = "1.0.1"

                from(components["release"])
            }
        }
    }
}
