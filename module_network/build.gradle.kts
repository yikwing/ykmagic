plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("maven-publish")
}

/**
 * 统一获取默认依赖
 */
fun safeExtGet(prop: String, fallback: String): String {
    return if (rootProject.ext.has(prop)) {
        rootProject.ext.get(prop) as String
    } else {
        fallback
    }
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:${safeExtGet("core_ktx_version", "1.7.0")}")
    implementation("androidx.appcompat:appcompat:${safeExtGet("appcompat_version", "1.3.0")}")

    implementation("com.squareup.okhttp3:okhttp:${safeExtGet("okhttp_version", "5.0.0-alpha.7")}")
    implementation("com.squareup.okhttp3:logging-interceptor:${safeExtGet("okhttp_version", "5.0.0-alpha.7")}")
    api("com.squareup.retrofit2:retrofit:${safeExtGet("retrofit_version", "2.9.0")}")
    implementation("com.squareup.retrofit2:converter-moshi:${safeExtGet("retrofit_version", "2.9.0")}")

    implementation("com.squareup.moshi:moshi:${safeExtGet("moshi_version", "1.13.0")}")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:${safeExtGet("moshi_version", "1.13.0")}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${safeExtGet("coroutines_version", "1.6.1")}")

    testImplementation("junit:junit:${safeExtGet("junit_version", "4.13.2")}")
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
