plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.ksp)

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
    implementation(libs.appcompat)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.exifinterface)

    implementation(libs.moshi.kotlin)
    implementation(libs.lifecycle.runtime.compose)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    implementation(libs.javax.inject)

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
