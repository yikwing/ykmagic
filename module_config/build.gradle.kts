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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("com.squareup.moshi:moshi:${safeExtGet("moshi_version", "1.13.0")}")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:${safeExtGet("moshi_version", "1.13.0")}")

    testImplementation("junit:junit:${safeExtGet("junit_version", "4.13.2")}")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "config"
                version = "1.0"
                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}