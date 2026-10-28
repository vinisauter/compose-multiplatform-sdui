package me.developes.humming.builder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.studio.*

/** Official composition root. The document, commands and runtime are shared with the sample. */
@Composable
fun HummingBuilder(files: StudioFiles? = null) {
    AppBuilder(files = files, initialDocument = remember { builderStarter() }, official = true)
}

internal fun builderColorScheme(official: Boolean, dark: Boolean): ColorScheme = when {
    !official -> lightColorScheme(primary = Color(0xFF4F46E5), secondary = Color(0xFF475569))
    dark -> darkColorScheme(primary = Color(0xFF93B4FF), primaryContainer = Color(0xFF1E3A5F),
        background = Color(0xFF0F172A), surface = Color(0xFF151E2E), surfaceVariant = Color(0xFF243044))
    else -> lightColorScheme(primary = Color(0xFF2563EB), secondary = Color(0xFF475569),
        background = Color(0xFFF8FAFC), surface = Color.White, surfaceVariant = Color(0xFFF1F5F9),
        primaryContainer = Color(0xFFDBEAFE), onPrimaryContainer = Color(0xFF1E3A8A))
}

internal fun builderStarter(): AuthoringDocument {
    val document = LegacyAuthoringCodec(initialNodeCatalog()).importDocument(PIX_STARTER, "builder-pix").requireValue()
    val workspace = LocalWorkspace(document)
    val before = workspace.document.nodes.keys
    check(workspace.addScreen(CanvasPoint(400.0, 0.0)).isSuccess)
    val receipt = workspace.document.nodes.values.single { it.id !in before }.id
    val prior = workspace.document.nodes.keys
    check(workspace.insertVisual(receipt, "layout:text").isSuccess)
    val label = workspace.document.nodes.values.single { it.id !in prior && it.definition == "layout:text" }.id
    check(workspace.putProperty(label, "text", JsonPrimitive("Fluxo concluído no sandbox local")).isSuccess)
    check(workspace.addAction("pix-confirm", "onClick", "navigation:goTo", receipt).isSuccess)
    return workspace.document
}

internal const val PIX_STARTER = """{
  "component": "layout:column",
  "id": "pix-screen",
  "properties": {
    "fillMaxSize": "1",
    "paddingAll": "20",
    "backgroundColor": "F8FAFC",
    "verticalArrangement": "SpaceBetween"
  },
  "children": [
    {
      "component": "layout:column",
      "id": "pix-content",
      "properties": {
        "fillMaxWidth": "1"
      },
      "children": [
        {
          "component": "layout:row",
          "id": "pix-status",
          "properties": {
            "fillMaxWidth": "1",
            "horizontalArrangement": "SpaceBetween",
            "paddingBottom": "18"
          },
          "children": [
            {
              "component": "layout:text",
              "id": "pix-clock",
              "properties": {
                "text": "9:41",
                "fontSize": "11"
              }
            },
            {
              "component": "layout:text",
              "id": "pix-signal",
              "properties": {
                "text": "5G",
                "fontSize": "11"
              }
            }
          ]
        },
        {
          "component": "layout:text",
          "id": "pix-heading",
          "properties": {
            "text": "Send Pix Instant",
            "fontSize": "20",
            "fontWeight": "Bold"
          }
        },
        {
          "component": "layout:text",
          "id": "pix-demo",
          "properties": {
            "text": "Local demo · no money is transferred",
            "fontSize": "10",
            "color": "64748B",
            "paddingBottom": "18"
          }
        },
        {
          "component": "layout:column",
          "id": "pix-balance",
          "properties": {
            "fillMaxWidth": "1",
            "backgroundColor": "0F172A",
            "backgroundShape": "RoundedCornerShape",
            "backgroundShapeSize": 18
          },
          "children": [
            {
              "component": "layout:column",
              "id": "pix-balance-inner",
              "properties": {
                "paddingAll": "18",
                "fillMaxWidth": "1"
              },
              "children": [
                {
                  "component": "layout:text",
                  "id": "pix-balance-label",
                  "properties": {
                    "text": "Available Account Balance",
                    "fontSize": "12",
                    "color": "CBD5E1"
                  }
                },
                {
                  "component": "layout:text",
                  "id": "pix-balance-amount",
                  "properties": {
                    "text": "R$ 14.850,20",
                    "fontSize": "28",
                    "fontWeight": "Bold",
                    "color": "FFFFFF",
                    "paddingTop": "6",
                    "paddingBottom": "18"
                  }
                },
                {
                  "component": "layout:row",
                  "id": "pix-limit",
                  "properties": {
                    "fillMaxWidth": "1",
                    "horizontalArrangement": "SpaceBetween"
                  },
                  "children": [
                    {
                      "component": "layout:text",
                      "id": "pix-limit-label",
                      "properties": {
                        "text": "Pix Limit Remaining",
                        "fontSize": "11",
                        "color": "94A3B8"
                      }
                    },
                    {
                      "component": "layout:text",
                      "id": "pix-limit-value",
                      "properties": {
                        "text": "R$ 10.000,00",
                        "fontSize": "11",
                        "color": "C4B5FD"
                      }
                    }
                  ]
                }
              ]
            }
          ]
        },
        {
          "component": "layout:row",
          "id": "pix-types",
          "properties": {
            "paddingVertical": "16",
            "fillMaxWidth": "1",
            "horizontalArrangement": "SpaceBetween"
          },
          "children": [
            {
              "component": "layout:text",
              "id": "pix-type-cpf",
              "properties": {
                "text": "CPF/CNPJ",
                "fontSize": "11",
                "backgroundColor": "2563EB",
                "color": "FFFFFF",
                "paddingAll": "7",
                "backgroundShape": "RoundedCornerShape",
                "backgroundShapeSize": 12
              }
            },
            {
              "component": "layout:text",
              "id": "pix-type-phone",
              "properties": {
                "text": "Phone",
                "fontSize": "11",
                "color": "64748B",
                "paddingAll": "7"
              }
            },
            {
              "component": "layout:text",
              "id": "pix-type-email",
              "properties": {
                "text": "Email",
                "fontSize": "11",
                "color": "64748B",
                "paddingAll": "7"
              }
            },
            {
              "component": "layout:text",
              "id": "pix-type-random",
              "properties": {
                "text": "Random",
                "fontSize": "11",
                "color": "64748B",
                "paddingAll": "7"
              }
            }
          ]
        },
        {
          "component": "layout:column",
          "id": "pix-key-card",
          "properties": {
            "fillMaxWidth": "1",
            "backgroundColor": "FFFFFF",
            "backgroundShape": "RoundedCornerShape",
            "backgroundShapeSize": 12,
            "borderWidth": "1",
            "borderColor": "E5E7EB"
          },
          "children": [
            {
              "component": "layout:column",
              "id": "pix-key-card-inner",
              "properties": {
                "paddingAll": "12",
                "fillMaxWidth": "1"
              },
              "children": [
                {
                  "component": "layout:text",
                  "id": "pix-key-label",
                  "properties": {
                    "text": "Pix Recipient Key",
                    "fontSize": "12",
                    "fontWeight": "Bold"
                  }
                },
                {
                  "component": "layout:outlinedTextField",
                  "id": "pix-key",
                  "properties": {
                    "stateKey": "pix_key",
                    "text": "contato@exemplo.com",
                    "singleLine": "true",
                    "fillMaxWidth": "1"
                  }
                },
                {
                  "component": "layout:text",
                  "id": "pix-binding-label",
                  "properties": {
                    "text": "Bound to local state: pix_key",
                    "fontSize": "10",
                    "color": "8B5CF6"
                  }
                }
              ]
            }
          ]
        },
        {
          "component": "layout:column",
          "id": "pix-spacer-amount",
          "properties": {
            "height": "16"
          }
        },
        {
          "component": "layout:column",
          "id": "pix-amount-card",
          "properties": {
            "fillMaxWidth": "1",
            "backgroundColor": "FFFFFF",
            "backgroundShape": "RoundedCornerShape",
            "backgroundShapeSize": 12,
            "borderWidth": "1",
            "borderColor": "E5E7EB"
          },
          "children": [
            {
              "component": "layout:column",
              "id": "pix-amount-card-inner",
              "properties": {
                "paddingAll": "12",
                "fillMaxWidth": "1"
              },
              "children": [
                {
                  "component": "layout:text",
                  "id": "pix-amount-label",
                  "properties": {
                    "text": "Amount to Transfer",
                    "fontSize": "12",
                    "fontWeight": "Bold"
                  }
                },
                {
                  "component": "layout:outlinedTextField",
                  "id": "pix-amount",
                  "properties": {
                    "stateKey": "amount",
                    "text": "250,00",
                    "singleLine": "true",
                    "fillMaxWidth": "1"
                  }
                },
                {
                  "component": "layout:text",
                  "id": "pix-amount-help",
                  "properties": {
                    "text": "BRL · interactive sandbox value",
                    "fontSize": "10",
                    "color": "94A3B8"
                  }
                }
              ]
            }
          ]
        },
        {
          "component": "layout:column",
          "id": "pix-spacer-schedule",
          "properties": {
            "height": "16"
          }
        },
        {
          "component": "layout:row",
          "id": "pix-schedule",
          "properties": {
            "fillMaxWidth": "1",
            "backgroundColor": "FFFFFF",
            "backgroundShape": "RoundedCornerShape",
            "backgroundShapeSize": 12,
            "borderWidth": "1",
            "borderColor": "E5E7EB",
            "horizontalArrangement": "SpaceBetween",
            "verticalAlignment": "Center"
          },
          "children": [
            {
              "component": "layout:row",
              "id": "pix-schedule-inner",
              "properties": {
                "paddingAll": "12",
                "fillMaxWidth": "1",
                "horizontalArrangement": "SpaceBetween",
                "verticalAlignment": "Center"
              },
              "children": [
                {
                  "component": "layout:column",
                  "id": "pix-schedule-copy",
                  "children": [
                    {
                      "component": "layout:text",
                      "id": "pix-schedule-label",
                      "properties": {
                        "text": "Schedule Pix Transfer",
                        "fontSize": "12"
                      }
                    },
                    {
                      "component": "layout:text",
                      "id": "pix-schedule-help",
                      "properties": {
                        "text": "Local state only",
                        "fontSize": "10",
                        "color": "94A3B8"
                      }
                    }
                  ]
                },
                {
                  "component": "layout:switch",
                  "id": "pix-instant",
                  "properties": {
                    "stateKey": "instant",
                    "checked": "false"
                  }
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "component": "layout:button",
      "id": "pix-confirm",
      "properties": {
        "fillMaxWidth": "1"
      },
      "children": [
        {
          "component": "layout:text",
          "id": "pix-confirm-label",
          "properties": {
            "text": "Continuar no exemplo"
          }
        }
      ]
    }
  ]
}"""

@Composable
internal fun BuilderRail(selected: Int, dark: Boolean, select: (Int) -> Unit, toggleTheme: () -> Unit, settings: () -> Unit) {
    Column(Modifier.width(72.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surface).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("H", color = Color.White, style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.background(Color(0xFF2563EB), RoundedCornerShape(9.dp)).padding(horizontal = 13.dp, vertical = 6.dp))
        Spacer(Modifier.height(24.dp))
        listOf("Screens", "Library", "Flows", "Logic", "Sheets").forEachIndexed { index, label ->
            Column(Modifier.fillMaxWidth().testTag("official-nav-$index").clickable { select(index) }.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(34.dp).background(if (selected == index) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    BuilderGlyph(index, if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = toggleTheme, modifier = Modifier.testTag("official-theme")) { Text(if (dark) "Light" else "Dark") }
        TextButton(onClick = settings) { Text("Tema") }
    }
}

@Composable
internal fun BuilderGlyph(index: Int, color: Color) {
    Canvas(Modifier.size(18.dp)) {
        val w = size.width; val h = size.height; val stroke = Stroke(1.5.dp.toPx())
        when (index) {
            0 -> { drawRect(color, Offset(w*.12f, 0f), Size(w*.76f,h), style=stroke); drawLine(color,Offset(w*.3f,h*.8f),Offset(w*.7f,h*.8f),stroke.width) }
            1 -> for(x in 0..1) for(y in 0..1) drawRect(color,Offset(x*w*.6f,y*h*.6f),Size(w*.4f,h*.4f),style=stroke)
            2,3 -> { drawLine(color,Offset(0f,h*.25f),Offset(w,h*.75f),stroke.width); drawCircle(color,w*.2f,Offset(w*.2f,h*.2f));drawCircle(color,w*.2f,Offset(w*.8f,h*.8f)) }
            else -> { drawRect(color,style=stroke);for(i in 1..2) drawLine(color,Offset(0f,h*i/3),Offset(w,h*i/3),stroke.width) }
        }
    }
}
