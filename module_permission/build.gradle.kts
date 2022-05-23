plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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
        minSdk = 24
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
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

    implementation("com.google.android.material:material:${safeExtGet("material_version", "1.5.0")}")
    testImplementation("junit:junit:${safeExtGet("junit_version", "4.13.2")}")

}


afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.yikwing"
                artifactId = "permission"
                version = "1.0"
                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}