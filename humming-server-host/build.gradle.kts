plugins { alias(libs.plugins.multiplatform) }
kotlin {
    jvmToolchain(25)
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":humming-server-application"))
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}
tasks.register<JavaExec>("runLocal") {
    group = "application"
    description = "Runs the authenticated, volatile draft host on loopback only"
    val main = kotlin.targets.getByName("jvm").compilations.getByName("main")
    dependsOn(main.compileTaskProvider)
    classpath(main.output.allOutputs, main.runtimeDependencyFiles)
    mainClass.set("me.developes.humming.server.host.LocalHostKt")
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(25)) })
}
