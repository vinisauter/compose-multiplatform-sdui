package me.developes.humming.builder.ui

import androidx.compose.foundation.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*

internal data class BuilderStateField(val nodeId: String, val key: String, val defaultValue: String)

@Composable
internal fun BuilderStateScopes(document: AuthoringDocument) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
    Text("STATE COLLECTIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(20.dp))
    Text("Interactive state", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    Text("${builderStateFields(document).map { it.key }.distinct().size} chaves declaradas", style = MaterialTheme.typography.bodySmall)
    HorizontalDivider(Modifier.padding(vertical = 20.dp))
    Text("SOURCES", style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(12.dp))
    Text("Screen inputs", style = MaterialTheme.typography.bodyMedium)
    Text("Fornecidos pelo host", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(16.dp))
    Text("Remote data", style = MaterialTheme.typography.bodyMedium)
    Text("Nenhuma fonte instalada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    HorizontalDivider(Modifier.padding(vertical = 20.dp))
    Text("AÇÕES DO DOCUMENTO", style = MaterialTheme.typography.labelSmall)
    Text("${document.nodes.values.count { it.kind == AuthorNodeKind.ACTION }} nós registrados", style = MaterialTheme.typography.bodySmall)
    }
}

/** Derived from the document, never a second mutable state declaration. */
internal fun builderStateFields(document: AuthoringDocument): List<BuilderStateField> = document.nodes.values.mapNotNull { node ->
    val key = (node.properties["stateKey"] ?: node.properties["onChangeUpdateState"]) as? JsonPrimitive
    key?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
        BuilderStateField(node.id, it, ((node.properties["text"] ?: node.properties["value"]) as? JsonPrimitive)?.contentOrNull
            ?: (node.properties["checked"] as? JsonPrimitive)?.contentOrNull.orEmpty())
    }
}

@Composable
internal fun BuilderStateManager(workspace: LocalWorkspace, document: AuthoringDocument, screen: String,
    select: (String) -> Unit, change: (EditOutcome) -> Unit) {
    val fields = remember(document) { builderStateFields(document) }
    var query by remember { mutableStateOf("") }
    var generation by remember { mutableIntStateOf(0) }
    var owner by remember(document.revision, screen, generation) { mutableStateOf<SDUIFormViewModel?>(null) }
    val compiled = remember(document, screen) {
        ContractCompiler(workspace.catalog).compileJourney(document.copy(entryNodeId = screen),
            TargetCapabilities(workspace.catalog.capabilities, providers = setOf("compiled-preview"), colorTokens = workspace.presentationTokens.colorNames,
                maskTokens = workspace.presentationTokens.maskNames), "compiled-preview", unreachable = UnreachablePolicy.REPORT)
    }
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(22.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                Text("Variables & Live State",style=MaterialTheme.typography.headlineSmall,modifier=Modifier.weight(1f))
                Text("• Reactive sandbox",color=androidx.compose.ui.graphics.Color(0xFF059669),style=MaterialTheme.typography.labelSmall)
            }
            Text("Local interactive state bound to mobile screen widgets in real time.",color=StitchMuted,modifier=Modifier.padding(top=5.dp,bottom=18.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                StitchInput(query,{query=it},Modifier.weight(1f),"Filter variables…")
                StitchButton("Reset",Modifier.testTag("official-reset-state"),click={generation++})
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                Text("Active Scope Variables",fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text("${fields.size} properties",color=StitchMuted,style=MaterialTheme.typography.labelSmall)
            }
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                Row(Modifier.width(780.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.45f)).padding(10.dp)) {
                    Text("VARIABLE",Modifier.width(135.dp),style=MaterialTheme.typography.labelSmall,color=StitchMuted)
                    Text("TYPE",Modifier.width(80.dp),style=MaterialTheme.typography.labelSmall,color=StitchMuted)
                    Text("DEFAULT",Modifier.width(90.dp),style=MaterialTheme.typography.labelSmall,color=StitchMuted)
                    Text("LIVE SANDBOX VALUE",Modifier.width(170.dp),style=MaterialTheme.typography.labelSmall,color=StitchMuted)
                    Text("TARGET WIDGET",style=MaterialTheme.typography.labelSmall,color=StitchMuted)
                }
                fields.filter { it.key.contains(query,true) }.forEach { field ->
                    val node=document.nodes.getValue(field.nodeId)
                    Row(Modifier.width(780.dp).background(MaterialTheme.colorScheme.surface).padding(10.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                        Text("ϟ  ${field.key}",Modifier.width(135.dp),color=StitchBlue,style=MaterialTheme.typography.bodySmall)
                        Text(if(node.definition=="layout:switch") "Boolean" else "String",Modifier.width(80.dp),color=StitchMuted,style=MaterialTheme.typography.bodySmall)
                        Text(field.defaultValue.ifEmpty { "\"\"" },Modifier.width(90.dp),style=MaterialTheme.typography.bodySmall)
                        StitchInput(owner?.formState?.get(field.key).orEmpty(),{owner?.updateState(field.key,it)},Modifier.width(160.dp).testTag("official-state-${field.key}"),enabled=owner!=null,mono=true)
                        Text(field.nodeId,Modifier.padding(start=10.dp).width(140.dp).clickable {select(field.nodeId)},color=StitchMuted,style=MaterialTheme.typography.labelSmall)
                        StitchButton("Apply",enabled=owner?.formState?.containsKey(field.key)==true,click={
                            val property=if("checked" in node.properties) "checked" else if(node.definition in setOf("layout:outlinedTextField","layout:textField")) "text" else "value"
                            change(workspace.putProperty(field.nodeId,property,JsonPrimitive(owner?.formState?.get(field.key).orEmpty())))
                        })
                    }
                    HorizontalDivider(Modifier.width(780.dp),color=StitchBorder)
                }
            }
            if(fields.isEmpty()) Text("Add an input from Library to declare an interactive state key.",Modifier.padding(16.dp),color=StitchMuted)
            Spacer(Modifier.height(24.dp))
            Text("Reactive Validation & State Rules",style=MaterialTheme.typography.titleLarge)
            Text("Registered actions and conditions from the authored document.",Modifier.padding(top=5.dp,bottom=12.dp),color=StitchMuted,style=MaterialTheme.typography.bodySmall)
            document.nodes.values.filter {it.kind==AuthorNodeKind.ACTION}.forEach {action ->
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface,androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .border(1.dp,StitchBorder,androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).clickable {select(action.id)}.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                        Text(action.definition,fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text("REGISTERED",color=StitchBlue,style=MaterialTheme.typography.labelSmall)
                    }
                    Text(action.id,color=StitchMuted,style=MaterialTheme.typography.bodySmall)
                    action.properties.forEach { (key,value) -> Text("$key  →  $value",style=MaterialTheme.typography.bodySmall) }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("STATE SNAPSHOT DEBUGGER",style=MaterialTheme.typography.labelSmall,color=StitchMuted)
            val pretty=remember { Json { prettyPrint=true } }
            val json=pretty.encodeToString(JsonObject.serializer(),JsonObject(owner?.formState.orEmpty().mapValues {JsonPrimitive(it.value)}))
            SelectionContainer { Text(json,Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Color(0xFF0F172A),androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(16.dp).testTag("official-state-json"),color=androidx.compose.ui.graphics.Color(0xFF93C5FD),fontFamily=stitchMono(),style=MaterialTheme.typography.bodySmall) }
        }
        Column(Modifier.width(310.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.3f)).padding(12.dp)) {
            Text("LIVE MOBILE PREVIEW",style=MaterialTheme.typography.labelSmall,color=StitchMuted,modifier=Modifier.padding(4.dp))
            key(document.revision,screen,generation) {
                if(compiled.isSuccess) BoxWithConstraints(Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {val spacing=16.dp.toPx();for(x in 0..(size.width/spacing).toInt()) for(y in 0..(size.height/spacing).toInt()) drawCircle(androidx.compose.ui.graphics.Color(0xFFCBD5E1),1.dp.toPx(),androidx.compose.ui.geometry.Offset(x*spacing,y*spacing))}
                    val scale=minOf((maxWidth.value-12)/390,(maxHeight.value-20)/844).coerceIn(.25f,1f)
                    Box(Modifier.size((390*scale).dp,(844*scale).dp).align(androidx.compose.ui.Alignment.Center)) {
                        Box(Modifier.graphicsLayer {scaleX=scale;scaleY=scale;transformOrigin=androidx.compose.ui.graphics.TransformOrigin(0f,0f)}
                            .wrapContentSize(androidx.compose.ui.Alignment.TopStart,unbounded=true).requiredSize(390.dp,844.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp)).background(androidx.compose.ui.graphics.Color.White)
                            .border(5.dp,androidx.compose.ui.graphics.Color(0xFF1F2937),androidx.compose.foundation.shape.RoundedCornerShape(28.dp)).padding(14.dp)) {
                            CompiledPreview(compiled.requireValue(),stateObserver={vm -> SideEffect {owner=vm}})
                        }
                    }
                } else compiled.diagnostics.forEach {Text("${it.code}: ${it.path}")}
            }
        }
    }
}
