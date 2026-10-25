package me.developes.humming.server.postgres

import kotlin.test.*

class HeadlessPostgresTest {
    @Test fun adapterDoesNotDependOnUi() {
        assertFailsWith<ClassNotFoundException> { Class.forName("androidx.compose.runtime.Composer") }
        assertNotNull(Class.forName("org.postgresql.ds.PGSimpleDataSource"))
    }
}
