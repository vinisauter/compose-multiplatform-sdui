plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
//    jvmToolchain(11)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
//    linuxX64()
//    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":humming-core"))
            api(compose.material3)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.coil.compose)

            implementation(libs.compose.icons.feather)
            implementation(libs.compose.icons.fontAwesome)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kermit)
            implementation(libs.multiplatformSettings)
            api(libs.koin.core)
            implementation(libs.koin.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            implementation(libs.coil.network.okhttp)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }

}

android {
    namespace = "me.developes.humming.sdui"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
    publishToMavenCentral()
    coordinates("me.developes.humming.sdui", "humming-core", "1.0.0")

    pom {
        name = "HummingUI"
        description = "Kotlin Multiplatform library"
        url = "github url" //todo

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "" //todo
                name = "" //todo
                email = "" //todo
            }
        }

        scm {
            url = "github url" //todo
        }
    }
    if (project.hasProperty("signing.keyId")) signAllPublications()
}