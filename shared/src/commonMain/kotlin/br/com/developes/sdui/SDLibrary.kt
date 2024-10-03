package br.com.developes.sdui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import br.com.developes.sdui.layout.Layout
import br.com.developes.sdui.layout.components.fromNode
import br.com.developes.sdui.utils.LoaderLayout
import br.com.developes.sdui.utils.produceUiState

/**
 * A typealias for a composable function that takes a ServerDrivenNode and a MutableMap<String, String> (representing component state) as parameters.
 * This function is responsible for rendering the UI for a component
 * */
typealias ComponentHandler = @Composable (ServerDrivenNode, MutableMap<String, String>) -> Unit

/**
 * A typealias for a suspending function with the same parameters as ComponentHandler.
 * This function handles actions or events triggered by components.
 * */
typealias ActionHandler = suspend (ServerDrivenNode, MutableMap<String, String>) -> Unit

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
    private val components = HashMap<String, ComponentHandler>()
    private val actions = HashMap<String, ActionHandler>()

    /**
     * @param name: String: The name of the component being added.
     * @param load: suspend (ServerDrivenNode, MutableMap<String, String>) -> Layout:
     * A suspending function that takes a ServerDrivenNode and a MutableMap (representing component state)
     * and returns a Layout composable. This function is responsible for loading and creating the
     * actual UI content based on the server-driven data.
     **/
    fun addComponentLayout(
        name: String,
        load: suspend (ServerDrivenNode, MutableMap<String, String>) -> Layout
    ): SDLibrary {
        return addComponent(name) { node, state ->
            var modifierGen: Modifier? = null
            val uiState by produceUiState {
                modifierGen = Modifier.fromNode(node)
                load.invoke(node, state)
            }
            LoaderLayout(
                modifier = modifierGen ?: Modifier,
                state = uiState
            ) {
                it.Content()
            }
        }
    }

    /**
     * @param name: String: This is the first parameter of the function.
     * It's named name and is of type String. This likely represents the name of the component being added.
     *
     * @param handler: ComponentHandler: This is the second parameter.
     * It's named handler and is of type ComponentHandler.
     * This suggests that it's an object responsible for handling the behavior or
     * logic associated with the component.
     *
     * @return [SDLibrary]: This is the return type of the function.
     * It seems to be the class or interface that this function belongs to,
     * implying that addComponent is a member function of SDLibrary.
     *
     * components[name] = handler: This line adds the handler to a collection named components,
     * using the name as the key. components is likely a MutableMap (e.g., a HashMap)
     * that stores the components and their corresponding handlers.
     *
     * return this: This line returns the current instance of the SDLibrary object.
     * This is a common practice in builder patterns, allowing you to chain multiple calls
     * to addComponent for a fluent API.
     *
     * example usage
     *
     * ```
     *  val library = SDLibrary()
     *     .addComponent("componentA", ComponentHandlerA())
     *     .addComponent("componentB", ComponentHandlerB())
     * ```
     * */
    fun addComponent(name: String, handler: ComponentHandler): SDLibrary {
        components[name] = handler
        return this
    }

    /**
     * Retrieves a component handler by its [name].
     * */
    fun getComponent(name: String): ComponentHandler? {
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
    fun addAction(name: String, action: ActionHandler): SDLibrary {
        actions[name] = action
        return this
    }

    /**
     * Retrieves an action handler by its [name].
     * */
    fun getAction(
        name: String
    ): ActionHandler? {
        return actions[name]
    }

    /**
     * The function takes another instance of [SDLibrary] as an argument, named other.
     * It then merges the contents of other into the current instance.
     * */
    fun merge(other: SDLibrary): SDLibrary {
        components.putAll(other.components)
        return this
    }
}