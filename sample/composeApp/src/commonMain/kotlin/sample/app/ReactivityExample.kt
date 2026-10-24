package sample.app

internal const val REACTIVITY_EXAMPLE = """{
  "component":"layout:column","id":"reactivity-screen","properties":{"paddingAll":24},"children":[
    {"component":"layout:text","id":"reactivity-title","properties":{"text":"Expressões: edite o nome; ocultar esconde a mensagem"}},
    {"component":"layout:textField","id":"reactivity-input","properties":{"text":"Visitante","onChangeUpdateState":"nome"}},
    {"component":"layout:reactiveText","id":"reactivity-message","properties":{
      "value":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"\"Olá, \" + (state.nome ?? \"Visitante\")"},"fallback":"Mensagem indisponível"},
      "visible":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state.nome != \"ocultar\""},"fallback":true}
    }}
  ]
}"""
