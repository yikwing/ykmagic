plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

/**
 * 统一获取默认依赖
 */
fun safeExtGet(prop: String): String {
    return if (rootProject.ext.has(prop)) {
        rootProject.ext.get(prop) as String
    } else {
        throw Error()
    }
}

android {
    compileSdk = rootProject.ext.get("compileSdk") as Int

    defaultConfig {
        minSdk = rootProject.ext.get("minSdk") as Int
        targetSdk = rootProject.ext.get("targetSdk") as Int

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    namespace = "com.yikwing.compress"
}

dependencies {

    implementation("com.facebook.spectrum:spectrum-default:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${safeExtGet("coroutines_version")}")

    testImplementation("junit:junit:${safeExtGet("junit_version")}")
}
