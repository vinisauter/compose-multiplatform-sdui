plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(25)
    androidTarget()
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosX64(); iosArm64(); iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            api(project(":humming-core"))
            api(project(":humming-studio-model"))
            api(project(":humming-layout-material3"))
            implementation(project(":humming-actions"))
            implementation(project(":humming-navigation"))
            implementation(project(":humming-network"))
            implementation(libs.ktor.client.core)
        }
        jvmMain.dependencies { implementation(compose.desktop.currentOs) }
        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(compose.desktop.uiTestJUnit4)
            implementation(project(":humming-server-application"))
            implementation(project(":humming-server-postgres"))
            implementation(project(":humming-server-host"))
            implementation(libs.lifecycle.runtime)
            implementation(libs.lifecycle.runtime.compose)
        }
    }
}
android {
    namespace = "me.developes.humming.builder.ui"
    compileSdk = libs.versions.androidSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.androidMinSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
compose.resources {
    publicResClass = true
    packageOfResClass = "me.developes.humming.builder.resources"
    generateResClass = always
}
