plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.libres)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.apollo)
}

kotlin {
    androidTarget()

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.libres)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.core)
                implementation(libs.napier)
                implementation(libs.composeIcons.feather)
                implementation(libs.composeIcons.fontAwesome)
                implementation(libs.apollo.runtime)
                implementation(libs.apollo.adapters)
                implementation(libs.multiplatform.settings)
                implementation(libs.sqlDelight.driver.runtime)
//                implementation(libs.composeImageLoader)
//                implementation(libs.multiplatformSettings)
//                implementation(libs.kstore)
//                implementation(libs.voyager.navigator)
//                implementation(libs.koin.core)
//                implementation(libs.moko.mvvm)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.activityCompose)
                api(libs.androidx.appcompat)
                api(libs.androidx.core.ktx)
                api(libs.androidx.preference.ktx)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.myapplication.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
}

sqldelight {
    databases {
        create("MyDatabase") {
            // Database configuration here.
            // https://cashapp.github.io/sqldelight
            packageName.set("me.next.serverdriven.db")
        }
    }
}

apollo {
    generateKotlinModels = true
    service("api") {
        // GraphQL configuration here.
        // https://www.apollographql.com/docs/kotlin/advanced/plugin-configuration/
        packageName.set("me.next.serverdriven.graphql")
        generateDataBuilders.set(true)
        generateFragmentImplementations.set(true)
        mapScalar("Any", "kotlin.Any","me.next.serverdriven.apollo.CustomAnyAdapter")
        mapScalar("Json", "kotlinx.serialization.json.JsonElement","me.next.serverdriven.apollo.JsonAdapter")
        mapScalar("Void", "kotlin.Unit", "me.next.serverdriven.apollo.VoidAdapter")
    }
}