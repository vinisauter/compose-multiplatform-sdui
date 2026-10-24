import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(25)
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    js { browser() }
    wasmJs { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()

    sourceSets {
        commonMain.dependencies { api(libs.kotlinx.serialization.json) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
