package sample.app

internal const val REACTIVITY_EXAMPLE = """{
  "component":"layout:column","id":"reactivity-screen","properties":{"paddingAll":24},"children":[
    {"component":"layout:text","id":"reactivity-title","properties":{"text":"Expressões: edite o nome; ocultar esconde a mensagem"}},
    {"component":"layout:validatedTextField","id":"reactivity-input","properties":{"stateKey":"nome","label":"Nome",
      "valid":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"(state.nome ?? \"\") + \"\" != \"\""},"fallback":false},
      "errorText":"Preencha o nome"}},
    {"component":"layout:reactiveText","id":"reactivity-message","properties":{
      "value":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"\"Olá, \" + (state.nome ?? \"Visitante\")"},"fallback":"Mensagem indisponível"},
      "visible":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state.nome != \"ocultar\""},"fallback":true}
    }},
    {"component":"layout:reactiveButton","id":"reactivity-submit","properties":{
      "enabled":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"(state.nome ?? \"\") + \"\" != \"\" && state.nome != \"ocultar\""},"fallback":false},
      "onClick":{"component":"action:update","id":"reactivity-confirm","properties":{"state":"status","value":"confirmado"}}
    },"children":[{"component":"layout:text","id":"reactivity-submit-label","properties":{"text":"Confirmar"}}]},
    {"component":"layout:reactiveText","id":"reactivity-status","properties":{
      "value":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state.status ?? \"Aguardando confirmação\""},"fallback":"Status indisponível"}
    }}
  ]
}"""
