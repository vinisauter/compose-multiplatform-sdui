package me.developes.humming.sdui.common

data class ErrorData(val code: String, val message: String) {
    companion object {
        fun missingProvider(namespace: String) = ErrorData(
            "MISSING_PROVIDER",
            "Provider type '$namespace' is not registered. Please, make sure you have the correct SDUI NodeProvider installed and registered."
        )
        fun missingComponent(componentName: String) = ErrorData(
            "MISSING_COMPONENT",
            "Component '$componentName' is not registered. Please, make sure you have the correct SDUI Library installed and registered."
        )

        fun missingAction(actionName: String) = ErrorData(
            "MISSING_ACTION",
            "Action '$actionName' is not registered. Please, make sure you have the correct SDUI Library installed and registered."
        )

        fun actionError(actionName: String, errorMessage: String) = ErrorData(
            "ACTION_ERROR",
            "Action '$actionName' failed to execute. Error: $errorMessage"
        )
    }
}