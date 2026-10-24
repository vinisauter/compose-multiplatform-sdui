package me.developes.humming.sdui.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTestSupport {
    private val dispatcher = StandardTestDispatcher()
    private val stores = mutableListOf<ViewModelStore>()

    @BeforeTest
    fun installMainDispatcher() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun releaseViewModelsAndDispatcher() {
        try {
            stores.forEach { it.clear() }
            dispatcher.scheduler.runCurrent()
        } finally {
            stores.clear()
            Dispatchers.resetMain()
        }
    }

    protected fun viewModel(vararg libraries: SDLibrary): SDUIFormViewModel {
        val vm = SDUIFormViewModel(SavedStateHandle(), libraries.toList())
        stores += ViewModelStore().also { it.put("test", vm) }
        return vm
    }
}
