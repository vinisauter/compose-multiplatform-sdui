enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
//        maven {
//            url = uri("https://nexus.prebanco.com.br/nexus/repository/next_mobile-build/")
//            credentials {
//                username = "next-mobile"
//                password = "next-mobile@nexus"
//            }
//            isAllowInsecureProtocol = true
//        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
//        maven {
//            url = uri("https://nexus.prebanco.com.br/nexus/repository/next_mobile-build/")
//            credentials {
//                username = "next-mobile"
//                password = "next-mobile@nexus"
//            }
//            isAllowInsecureProtocol = true
//        }
    }
}

rootProject.name = "SDUI-ComposeApplication"

include(":androidApp")
include(":iosApp")
include(":shared")