plugins { alias(libs.plugins.multiplatform) }
kotlin {
    jvmToolchain(25)
    jvm()
    sourceSets {
        jvmMain.dependencies {
            api(project(":humming-server-application"))
            implementation(libs.postgresql)
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}
val regularTests = tasks.named<Test>("jvmTest") { exclude("**/PostgresIntegrationTest*") }
tasks.register<Test>("postgresIntegrationTest") {
    group = "verification"
    description = "Requires an explicitly supplied real PostgreSQL database; never substitutes an in-memory database"
    dependsOn("jvmTestClasses")
    testClassesDirs = regularTests.get().testClassesDirs
    classpath = regularTests.get().classpath
    include("**/PostgresIntegrationTest*")
    outputs.upToDateWhen { false }
    doFirst {
        for (key in listOf("HUMMING_TEST_DB_URL", "HUMMING_TEST_DB_USER", "HUMMING_TEST_DB_PASSWORD"))
            environment(key, requireNotNull(System.getenv(key)) { "$key is required for PostgreSQL integration" })
    }
}
