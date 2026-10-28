plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(25)
    jvm()
    wasmJs { browser(); binaries.executable() }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(project(":humming-studio-ui"))
        }
        jvmMain.dependencies { implementation(compose.desktop.currentOs) }
    }
}
compose.desktop {
    application { mainClass = "me.developes.humming.builder.MainKt" }
}
