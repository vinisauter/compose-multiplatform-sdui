rootProject.name = "SDUI-ComposeApplication"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":shared")
include(":iosApp")
include(":androidApp")
include(":desktopApp")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}