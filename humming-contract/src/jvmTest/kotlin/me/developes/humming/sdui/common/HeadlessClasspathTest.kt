package me.developes.humming.sdui.common

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HeadlessClasspathTest {
    @Test fun runtimeHasNoComposeLifecycleNavigationMaterialOrSkiaClasses() {
        for (name in listOf("androidx.compose.runtime.Composer", "androidx.lifecycle.ViewModel",
            "androidx.navigation.NavController", "androidx.compose.material3.MaterialTheme",
            "org.jetbrains.skia.Image")) {
            assertFailsWith<ClassNotFoundException>(name) { Class.forName(name, false, javaClass.classLoader) }
        }
        assertTrue(initialNodeCatalog().definitions.isNotEmpty())
    }
}
