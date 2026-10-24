rootProject.name = "HummingUI"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":humming-core")
include(":humming-contract")
include(":humming-studio-model")
include(":humming-actions")
include(":humming-navigation")
include(":humming-layout-material3")

include(":sample:composeApp")
include(":sample:iosApp")
