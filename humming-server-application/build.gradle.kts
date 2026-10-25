import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { alias(libs.plugins.multiplatform) }
kotlin {
    jvmToolchain(25)
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    sourceSets {
        commonMain.dependencies {
            api(project(":humming-studio-model"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
tasks.withType<JavaCompile>().configureEach { options.release.set(17) }
