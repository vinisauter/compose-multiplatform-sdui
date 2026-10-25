plugins { alias(libs.plugins.multiplatform) }
kotlin {
    jvmToolchain(25)
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":humming-server-application"))
            implementation(project(":humming-server-postgres"))
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}
val regularHostTests = tasks.named<Test>("jvmTest") { exclude("**/PostgresHttpIntegrationTest*") }
tasks.register<Test>("postgresHttpIntegrationTest") {
    group = "verification"
    dependsOn("jvmTestClasses")
    testClassesDirs = regularHostTests.get().testClassesDirs
    classpath = regularHostTests.get().classpath
    include("**/PostgresHttpIntegrationTest*")
    outputs.upToDateWhen { false }
    doFirst {
        for (key in listOf("HUMMING_TEST_DB_URL", "HUMMING_TEST_DB_USER", "HUMMING_TEST_DB_PASSWORD"))
            environment(key, requireNotNull(System.getenv(key)) { "$key is required for PostgreSQL HTTP integration" })
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
