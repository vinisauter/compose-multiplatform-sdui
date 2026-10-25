package sample.app

/** Local-only gallery: the same wire can be imported, edited and exported by Studio. */
internal const val MATERIAL3_EXAMPLE = """{
  "component":"layout:column","id":"material-gallery",
  "properties":{"paddingAll":16,"fillMaxWidth":1,"verticalScroll":"true"},
  "children":[
    {"component":"layout:text","id":"material-title","properties":{"text":"Material 3","fontSize":24}},
    {"component":"layout:outlinedCard","id":"material-card","properties":{"paddingVertical":8},"children":[
      {"component":"layout:column","id":"material-form","properties":{"paddingAll":16},"children":[
        {"component":"layout:outlinedTextField","id":"material-name","properties":{"stateKey":"material-name","label":"Nome","singleLine":true}},
        {"component":"layout:row","id":"material-toggles","children":[
          {"component":"layout:checkbox","id":"material-check","properties":{"stateKey":"material-check"}},
          {"component":"layout:text","id":"material-check-label","properties":{"text":"Aceito"}},
          {"component":"layout:switch","id":"material-switch","properties":{"stateKey":"material-switch"}}
        ]},
        {"component":"layout:slider","id":"material-slider","properties":{"stateKey":"material-progress","value":"0.5","fillMaxWidth":1}},
        {"component":"layout:linearProgressIndicator","id":"material-progress","properties":{"progress":"#{material-progress}","indeterminate":false,"fillMaxWidth":1}}
      ]}
    ]},
    {"component":"layout:row","id":"material-chips","children":[
      {"component":"layout:assistChip","id":"material-assist","properties":{"text":"Ajuda"}},
      {"component":"layout:filterChip","id":"material-filter","properties":{"text":"Filtro","stateKey":"material-filter"}},
      {"component":"layout:inputChip","id":"material-input-chip","properties":{"text":"Selecionar"}}
    ]},
    {"component":"layout:primaryTabRow","id":"material-tabs","properties":{"stateKey":"material-tab"},"children":[
      {"component":"layout:tab","id":"material-tab-a","properties":{"text":"Geral"}},
      {"component":"layout:tab","id":"material-tab-b","properties":{"text":"Detalhes"}}
    ]},
    {"component":"layout:materialOutlinedButton","id":"material-submit","properties":{"onClick":{"component":"action:update","id":"material-save","properties":{"state":"material-status","value":"Salvo"}}},"children":[
      {"component":"layout:text","id":"material-submit-label","properties":{"text":"Salvar"}}
    ]},
    {"component":"layout:text","id":"material-status","properties":{"text":"#{material-status}"}}
  ]
}"""
