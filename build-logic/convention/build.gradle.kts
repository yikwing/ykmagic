import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.yikwing.ykquickdev.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.wire.gradlePlugin)
    compileOnly(libs.koin.compilerGradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "ykmagic.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "ykmagic.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "ykmagic.android.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("androidRoom") {
            id = "ykmagic.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidKoin") {
            id = "ykmagic.android.koin"
            implementationClass = "AndroidKoinConventionPlugin"
        }
        register("androidWire") {
            id = "ykmagic.android.wire"
            implementationClass = "AndroidWireConventionPlugin"
        }
    }
}
