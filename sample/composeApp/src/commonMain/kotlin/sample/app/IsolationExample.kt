package sample.app

import me.developes.humming.sdui.studio.*

internal fun isolationExample(): AuthoringDocument {
    val workspace=LocalWorkspace()
    workspace.importContract("""{"component":"layout:column","id":"isolation-screen","properties":{"paddingAll":"24"},"children":[{"component":"layout:text","id":"isolation-title","properties":{"text":"Estado por entrada: edite e abra outra visita"}},{"component":"layout:textField","id":"isolation-field","properties":{"text":"inicial","onChangeUpdateState":"nome"}},{"component":"layout:text","id":"isolation-value","properties":{"text":"Valor: #{nome}"}},{"component":"layout:button","id":"isolation-push","properties":{"onClick":{"component":"navigation:goTo","id":"isolation-go","properties":{"destiny":"legacy"}}},"children":[{"component":"layout:text","id":"isolation-push-label","properties":{"text":"Nova visita"}}]},{"component":"layout:button","id":"isolation-back","properties":{"onClick":{"component":"navigation:goBack","id":"isolation-go-back"}},"children":[{"component":"layout:text","id":"isolation-back-label","properties":{"text":"Voltar à visita anterior"}}]}]}""").requireValue()
    check(workspace.setNavigation("isolation-go","isolation-screen").isSuccess)
    return workspace.document
}
