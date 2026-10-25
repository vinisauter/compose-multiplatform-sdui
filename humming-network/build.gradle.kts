import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
}
kotlin {
    jvmToolchain(25)
    jvm()
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    js { browser() }
    wasmJs { browser() }
    iosX64(); iosArm64(); iosSimulatorArm64(); macosX64(); macosArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(":humming-core"))
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        jvmTest.dependencies { implementation(libs.ktor.client.okhttp) }
    }
}
android {
    namespace = "me.developes.humming.sdui.network"
    compileSdk = libs.versions.androidSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.androidMinSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
