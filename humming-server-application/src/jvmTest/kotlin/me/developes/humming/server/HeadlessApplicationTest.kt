package me.developes.humming.server

import kotlin.test.Test
import kotlin.test.assertFailsWith

class HeadlessApplicationTest {
    @Test fun applicationClasspathDoesNotContainUiOrDatabaseDrivers() {
        for (name in listOf("androidx.compose.runtime.Composer", "androidx.lifecycle.ViewModel", "org.postgresql.Driver"))
            assertFailsWith<ClassNotFoundException> { Class.forName(name) }
    }
}
