package me.developes.humming.sdui.common

import androidx.compose.runtime.Composable

interface ComposableContent {
    @Composable
    fun Content()
}

/**
 * A typealias for a composable function that takes a ServerDrivenNode and a MutableMap<String, String> (representing component state) as parameters.
 * This function is responsible for rendering the UI for a component
 * */
typealias ComponentHandler = (ServerDrivenNode, SDUIFormViewModel) -> ComposableContent

/**
 * A typealias for a suspending function with the same parameters as ComponentHandler.
 * This function handles actions or events triggered by components.
 * */
typealias ActionHandler = suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit

/**
 * @param namespace: A property to store a namespace for the library (optional).
 * @property components: A HashMap to store component handlers,
 * where the key is the component name and the value is the ComponentHandler function.
 * @property actions: A HashMap to store action handlers, similar to components.
 *
 * Usage example
 *
 * ```
 * val library = SDLibrary()
 *     .addComponent("text", { node, state ->
 *         Text(text = node.attributes["text"] ?: "")
 *     })
 *     .addAction("click", { node, state ->
 *         // Handle click action
 *     })
 * ```
 *
 * This code snippet demonstrates how to add a component named "text" that displays text from a [ServerDrivenNode],
 * and an action named "click" that could handle click events on components.
 * */
open class SDLibrary(val namespace: String = "") {
    protected val components = HashMap<String, ComponentHandler>()
    private val actions = HashMap<String, ActionHandler>()

    constructor(namespace: String, block: SDLibrary.() -> Unit) : this(namespace) {
        block()
    }

    fun addComponent(name: String, handler: ComponentHandler): SDLibrary {
        components[name] = handler
        return this
    }

    /**
     * Retrieves a component handler by its [name].
     * */
    open fun getComponent(name: String): ComponentHandler? {
        return components[name]
    }

    /**
     * The function takes two parameters:
     * @param name: A [String] representing a name to the action.
     * @param action: An [ActionHandler] object that likely encapsulates the code to be executed for this action.
     * @return [SDLibrary]: This indicates that the function returns an SDLibrary object.
     * This pattern is often used for method chaining.
     *
     * actions[name] = action: This line adds the action to a collection
     * named actions within the SDLibrary object.
     *
     * [actions]: Likely a MutableMap or similar data structure that stores the actions.
     * [name]: Uses the name parameter as the key to store the action in the actions collection.
     * action: Assigns the action parameter as the value associated with the given name.
     * return this: This line returns the current instance of the SDLibrary object.
     * This allows you to chain multiple addAction calls together.
     *
     * example usage
     * ```
     * val library = SDLibrary().library.addAction("open", OpenAction())
     *      .addAction("close", CloseAction())
     * ```
     * */
    open fun addAction(name: String, action: ActionHandler): SDLibrary {
        actions[name] = action
        return this
    }

    /**
     * Retrieves an action handler by its [name].
     * */
    open fun getAction(
        name: String
    ): ActionHandler? {
        return actions[name]
    }
}