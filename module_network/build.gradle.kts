plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("maven-publish")
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
        getByName("release") {
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
    namespace = "com.yikwing.network"
}

dependencies {
    implementation("androidx.core:core-ktx:${safeExtGet("core_ktx_version")}")
    implementation("androidx.appcompat:appcompat:${safeExtGet("appcompat_version")}")

    implementation("com.squareup.okhttp3:okhttp:${safeExtGet("okhttp_version")}")
    implementation("com.squareup.okhttp3:logging-interceptor:${safeExtGet("okhttp_version")}")
    implementation("com.squareup.retrofit2:retrofit:${safeExtGet("retrofit_version")}")
    implementation("com.squareup.retrofit2:converter-moshi:${safeExtGet("retrofit_version")}")

    implementation("com.squareup.moshi:moshi:${safeExtGet("moshi_version")}")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:${safeExtGet("moshi_version")}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${safeExtGet("coroutines_version")}")

    testImplementation("junit:junit:${safeExtGet("junit_version")}")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "network"
                version = "1.0"
                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}
