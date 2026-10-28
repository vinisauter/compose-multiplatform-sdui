@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package me.developes.humming.builder.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*
import me.developes.humming.builder.resources.*
import me.developes.humming.sdui.studio.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.LayoutDefinitions
import org.jetbrains.compose.resources.Font

internal val StitchBlue = Color(0xFF2563EB)
internal val StitchBorder = Color(0xFFE5E7EB)
internal val StitchMuted = Color(0xFF6B7280)

@Composable internal fun StitchPrimitiveGlyph(name: String, color: Color) {
    if(name=="text") Text("Tt",fontSize=23.sp,color=color)
    else Canvas(Modifier.size(28.dp,24.dp)) {
        val w=size.width;val h=size.height;val line=Stroke(1.3.dp.toPx())
        when(name) {
            "column" -> { drawRect(color,Offset(w*.12f,h*.24f),Size(w*.76f,h*.2f));drawRect(color,Offset(w*.12f,h*.56f),Size(w*.76f,h*.2f)) }
            "row" -> { drawRect(color,Offset(w*.17f,h*.15f),Size(w*.25f,h*.7f));drawRect(color,Offset(w*.57f,h*.15f),Size(w*.25f,h*.7f)) }
            "box" -> drawRect(color,Offset(w*.12f,h*.12f),Size(w*.76f,h*.76f),style=line)
            "button" -> drawRoundRect(color,Offset(w*.08f,h*.25f),Size(w*.84f,h*.5f),androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            "outlinedTextField" -> { drawRect(color,Offset(0f,h*.2f),Size(w,h*.6f),style=line);drawRect(color,Offset(w*.1f,h*.4f),Size(w*.15f,h*.2f)) }
            "switch" -> { drawRoundRect(color,Offset(0f,h*.2f),Size(w,h*.6f),androidx.compose.ui.geometry.CornerRadius(h));drawCircle(Color.White,h*.22f,Offset(w*.7f,h*.5f)) }
            "layers" -> for(y in listOf(.25f,.48f,.71f)) drawPath(Path().apply {moveTo(w*.15f,h*y);lineTo(w*.5f,h*(y-.18f));lineTo(w*.85f,h*y);lineTo(w*.5f,h*(y+.18f));close()},color,style=line)
            else -> {drawRect(color,Offset(w*.1f,h*.1f),Size(w*.8f,h*.8f),style=line);drawLine(color,Offset(w*.15f,h*.8f),Offset(w*.45f,h*.4f),line.width);drawLine(color,Offset(w*.45f,h*.4f),Offset(w*.8f,h*.8f),line.width)}
        }
    }
}

@Composable internal fun stitchTypography(): Typography {
    val font = FontFamily(Font(Res.font.inter))
    fun style(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(fontFamily=font, fontSize=size.sp,
        lineHeight=(size+5).sp, fontWeight=weight, letterSpacing=0.sp)
    return Typography(displayLarge=style(28), displayMedium=style(24), displaySmall=style(20),
        headlineLarge=style(22,FontWeight.SemiBold), headlineMedium=style(20,FontWeight.SemiBold), headlineSmall=style(18,FontWeight.SemiBold),
        titleLarge=style(15,FontWeight.SemiBold),titleMedium=style(13,FontWeight.SemiBold),titleSmall=style(12,FontWeight.SemiBold),
        bodyLarge=style(13),bodyMedium=style(12),bodySmall=style(11),
        labelLarge=style(12,FontWeight.Medium),labelMedium=style(11,FontWeight.Medium),labelSmall=style(10,FontWeight.Medium))
}
@Composable internal fun stitchMono() = FontFamily(Font(Res.font.jetbrains_mono))

internal fun stitchShapes() = Shapes(extraSmall=RoundedCornerShape(4.dp),small=RoundedCornerShape(6.dp),
    medium=RoundedCornerShape(8.dp),large=RoundedCornerShape(12.dp),extraLarge=RoundedCornerShape(16.dp))

@Composable internal fun StitchButton(label: String, modifier: Modifier = Modifier, primary: Boolean = false,
    active: Boolean = false, enabled: Boolean = true, click: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier.height(28.dp).clip(RoundedCornerShape(6.dp))
        .background(if(primary) StitchBlue else if(active) scheme.primaryContainer else scheme.surface)
        .border(1.dp,if(primary || active) StitchBlue.copy(alpha=.35f) else StitchBorder,RoundedCornerShape(6.dp))
        .clickable(enabled=enabled,onClick=click).padding(horizontal=9.dp),contentAlignment=Alignment.Center) {
        if(label in setOf("▷","☾","☼","⚙")) Canvas(Modifier.size(14.dp)) {
            val ink=scheme.onSurfaceVariant;val w=size.width;val h=size.height;val stroke=Stroke(1.2.dp.toPx())
            when(label) {
                "▷" -> drawPath(Path().apply {moveTo(w*.2f,h*.1f);lineTo(w*.85f,h*.5f);lineTo(w*.2f,h*.9f);close()},ink,style=stroke)
                "☾" -> {drawCircle(ink,w*.42f);drawCircle(scheme.surface,w*.36f,Offset(w*.7f,h*.3f))}
                else -> {drawCircle(ink,w*.22f,style=stroke);for(i in 0..7) {val angle=i*kotlin.math.PI/4;drawLine(ink,Offset(w/2+(kotlin.math.cos(angle)*w*.32).toFloat(),h/2+(kotlin.math.sin(angle)*h*.32).toFloat()),Offset(w/2+(kotlin.math.cos(angle)*w*.47).toFloat(),h/2+(kotlin.math.sin(angle)*h*.47).toFloat()),stroke.width)}}
            }
        } else Text(label,color=if(!enabled) StitchMuted.copy(alpha=.45f) else if(primary) Color.White else if(active) StitchBlue else scheme.onSurface,
                style=MaterialTheme.typography.labelMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
    }
}

@Composable internal fun StitchInput(value: String, change: (String)->Unit, modifier: Modifier = Modifier,
    placeholder: String = "", enabled: Boolean = true, mono: Boolean = false) {
    val color = MaterialTheme.colorScheme.onSurface
    BasicTextField(value,change,enabled=enabled,singleLine=true,
        textStyle=MaterialTheme.typography.bodySmall.copy(color=color,fontFamily=if(mono) stitchMono() else MaterialTheme.typography.bodySmall.fontFamily),
        modifier=modifier.height(30.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.4f),RoundedCornerShape(5.dp))
            .border(1.dp,StitchBorder,RoundedCornerShape(5.dp)).padding(horizontal=8.dp,vertical=6.dp),
        decorationBox={ inner -> Box { if(value.isEmpty()) Text(placeholder,color=StitchMuted,style=MaterialTheme.typography.bodySmall); inner() } })
}

@Composable internal fun StitchSection(title: String, aside: String = "", content: @Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(title.uppercase(),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
            Text(aside,color=StitchMuted,style=MaterialTheme.typography.labelSmall)
        }
        content()
    }
    HorizontalDivider(color=StitchBorder)
}

@Composable
internal fun StitchWorkspace(title: String, setTitle: (String)->Unit, screenTitle: String, viewport: ProjectViewport, document: AuthoringDocument,
    workspace: LocalWorkspace, selectedId: String, screenId: String, mode: Int, names: Map<String,String>,
    filesAvailable: Boolean, dirty: Boolean, message: String, dark: Boolean, running: Boolean,
    selectMode: (Int)->Unit,selectScreen: (String)->Unit,selectNode: (String)->Unit,change: (EditOutcome)->Unit,
    palette: PaletteDragState,insert: (String,DropPlan?)->Unit,newScreen: ()->Unit,command: (String)->Unit,
    setViewport: (ProjectViewport)->Unit,zoom: Double,changeZoom: (Double)->Unit,
    content: @Composable ()->Unit,inspectorContent: @Composable ()->Unit,inspectorRequest: Int,setInspector: (String)->Unit) {
    var rename by remember { mutableStateOf(false) }
    var projectName by remember(title) { mutableStateOf(title) }
    var menu by remember { mutableStateOf(false) }
    var inspectorTab by remember { mutableStateOf("Design") }
    var fullInspector by remember { mutableStateOf(false) }
    LaunchedEffect(inspectorRequest) {if(inspectorRequest>0) fullInspector=true}
    val selected = document.nodes[selectedId]
    Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
        BoxWithConstraints {
        val workspaceWidth=maxWidth
        val compactHeader=workspaceWidth<1180.dp
        Column {
            Row(Modifier.fillMaxWidth().height(52.dp).background(MaterialTheme.colorScheme.surface)
                .border(1.dp,StitchBorder).padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Box {
                    Row(Modifier.testTag("stitch-project-menu").clickable { menu=true },verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(27.dp).background(StitchBlue,RoundedCornerShape(7.dp)),contentAlignment=Alignment.Center) { StitchPrimitiveGlyph("layers",Color.White) }
                        Text("Humming",fontWeight=FontWeight.Bold,fontSize=13.sp)
                        Text("UI",color=StitchBlue,fontWeight=FontWeight.Bold,fontSize=13.sp,modifier=Modifier.offset(x=(-7).dp))
                        if(!compactHeader) Text("BUILDER",color=StitchBlue,style=MaterialTheme.typography.labelSmall,
                            modifier=Modifier.background(Color(0xFFEFF6FF),RoundedCornerShape(3.dp)).padding(4.dp,2.dp))
                    }
                    DropdownMenu(menu,{menu=false}) {
                        DropdownMenuItem(text={Text("Rename project")},onClick={menu=false;rename=true})
                        listOf("Novo projeto" to "new","Abrir projeto" to "open","Salvar projeto" to "save","Desfazer" to "undo","Refazer" to "redo","Studio avançado" to "advanced").forEach { (label,key) ->
                            DropdownMenuItem(text={ Text(label) },onClick={ menu=false;command(key) },enabled=key !in setOf("open","save") || filesAvailable)
                        }
                    }
                }
                VerticalDivider(Modifier.height(24.dp),color=StitchBorder)
                if(!compactHeader) Text(title,color=StitchMuted,maxLines=1,modifier=Modifier.widthIn(max=130.dp))
                Text("›",color=StitchMuted)
                Text(screenTitle,fontWeight=FontWeight.SemiBold,maxLines=1,modifier=Modifier.widthIn(max=145.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.5f),RoundedCornerShape(3.dp)).padding(6.dp,3.dp))
                if(!compactHeader) Text(if(dirty) "• Local changes" else "• Local draft",color=Color(0xFF059669),style=MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Box {
                    var devices by remember { mutableStateOf(false) }
                    StitchButton("${viewport.name.lowercase().replaceFirstChar { it.uppercase() }} • ${viewport.width}×${viewport.height}",click={devices=true})
                    DropdownMenu(devices,{devices=false}) { ProjectViewport.entries.forEach { preset -> DropdownMenuItem(text={Text(preset.name)},onClick={devices=false;setViewport(preset)}) } }
                }
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                    StitchButton("−",click={changeZoom(1/1.2)})
                    Text("${(zoom*100).toInt()}%",style=MaterialTheme.typography.labelSmall)
                    StitchButton("+",click={changeZoom(1.2)})
                }
                StitchButton("schema.json",click={command("schema")})
                if(mode in 3..4 && workspaceWidth<1500.dp) StitchButton("Inspector",click={fullInspector=true})
                StitchButton("▷",Modifier.testTag("official-run"),click={command("run")})
                StitchButton("Export SDUI",primary=true,click={command("schema")})
            }
            Row(Modifier.weight(1f)) {
                Column(Modifier.width(60.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surface)
                    .border(1.dp,StitchBorder).padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    listOf("Screens","Library","Flows","Logic","Sheets").forEachIndexed { index,label ->
                        Column(Modifier.padding(vertical=3.dp).size(48.dp).clip(RoundedCornerShape(9.dp))
                            .background(if(mode==index) MaterialTheme.colorScheme.primaryContainer.copy(alpha=.55f) else Color.Transparent)
                            .testTag("official-nav-$index").clickable { selectMode(index) },horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                            BuilderGlyph(index,if(mode==index) StitchBlue else StitchMuted)
                            Text(label,fontSize=9.sp,lineHeight=11.sp,color=if(mode==index) StitchBlue else StitchMuted,modifier=Modifier.padding(top=3.dp))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    StitchButton(if(dark) "☼" else "☾",Modifier.testTag("official-theme"),click={command("dark")})
                    Spacer(Modifier.height(8.dp))
                    StitchButton("⚙",click={command("theme")})
                }
                if(mode!=2 && !running) Column(Modifier.width(if(mode in 3..4) 220.dp else 260.dp).fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface).border(1.dp,StitchBorder)) {
                    if(mode in 3..4) Box(Modifier.padding(16.dp)) { BuilderStateScopes(document) }
                    else StitchLibrary(workspace,document,selectedId,screenId,names,palette,insert,selectScreen,selectNode,change,newScreen,mode)
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    if(message.isNotEmpty()) Text(message,Modifier.fillMaxWidth().heightIn(max=45.dp).background(Color(0xFFEFF6FF)).padding(6.dp),style=MaterialTheme.typography.bodySmall)
                    Box(Modifier.weight(1f)) {
                        content()
                        if(mode in 0..1 && !running) Text("Server UI Renderer:  HummingUI / Compose  •  Local sandbox",
                            modifier=Modifier.align(Alignment.TopCenter).padding(top=16.dp).background(MaterialTheme.colorScheme.surface,RoundedCornerShape(20.dp))
                                .border(1.dp,StitchBorder,RoundedCornerShape(20.dp)).padding(12.dp,6.dp),style=MaterialTheme.typography.labelSmall)
                    }
                }
                if(!running && (mode !in 3..4 || workspaceWidth>=1500.dp)) Column(Modifier.width(300.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surface).border(1.dp,StitchBorder)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.5f),RoundedCornerShape(6.dp))) {
                        listOf("Design","Interactions","Rules").forEach { tab ->
                            Box(Modifier.weight(1f).height(28.dp).background(if(inspectorTab==tab) MaterialTheme.colorScheme.surface else Color.Transparent,RoundedCornerShape(5.dp))
                                .clickable { inspectorTab=tab;setInspector(if(tab=="Design") "Propriedades" else "Ações") },contentAlignment=Alignment.Center) {
                                Text(tab,style=MaterialTheme.typography.labelMedium,color=if(inspectorTab==tab) MaterialTheme.colorScheme.onSurface else StitchMuted)
                            }
                        }
                    }
                    Row(Modifier.padding(horizontal=14.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("• ",color=StitchBlue)
                        Text(selected?.definition?.substringAfter(':').orEmpty(),fontWeight=FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(selectedId.take(18),fontFamily=FontFamily.Monospace,fontSize=9.sp,color=StitchMuted)
                    }
                    HorizontalDivider(Modifier.padding(top=10.dp),color=StitchBorder)
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        if(mode==2 && selected!=null) StitchConnectionInspector(selected,workspace,document,names,selectNode,change)
                        else if(inspectorTab=="Design" && selected!=null) StitchInspector(selected,workspace,change) { fullInspector=true }
                        else Column(Modifier.padding(12.dp)) {
                            if(mode==2) Text("Connection Flow",style=MaterialTheme.typography.titleMedium)
                            inspectorContent()
                        }
                    }
                    StitchButton("All properties & events",Modifier.fillMaxWidth().padding(12.dp),click={fullInspector=true})
                }
            }
        }
    }
    }
    if(rename) AlertDialog(onDismissRequest={rename=false},title={Text("Rename project")},text={StitchInput(projectName,{projectName=it},Modifier.fillMaxWidth().testTag("stitch-project-name"))},confirmButton={TextButton(enabled=projectName.isNotBlank()&&projectName.length<=80,onClick={setTitle(projectName.trim());rename=false}) {Text("Apply")}},dismissButton={TextButton(onClick={rename=false}) {Text("Cancel")}})
    if(fullInspector) AlertDialog(onDismissRequest={fullInspector=false},title={Text("Inspector · ${selected?.definition.orEmpty()}")},
        text={Column(Modifier.width(600.dp).heightIn(max=650.dp).verticalScroll(rememberScrollState())) { inspectorContent() }},
        confirmButton={TextButton(onClick={fullInspector=false}) { Text("Fechar") }})
}

@Composable private fun ColumnScope.StitchLibrary(workspace: LocalWorkspace,document: AuthoringDocument,selected: String,screen: String,
    names: Map<String,String>,palette: PaletteDragState,insert: (String,DropPlan?)->Unit,selectScreen: (String)->Unit,
    select: (String)->Unit,change: (EditOutcome)->Unit,newScreen: ()->Unit,mode: Int) {
    var tab by remember { mutableStateOf("Primitives") }
    var query by remember { mutableStateOf("") }
    Row(Modifier.padding(12.dp).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        listOf("Primitives","Components").forEach { label -> StitchButton(label,Modifier.weight(1f),active=tab==label,click={tab=label}) }
    }
    StitchInput(query,{query=it},Modifier.padding(horizontal=12.dp).fillMaxWidth().testTag("builder-search"),"Insert UI primitive")
    if(mode==0) Row(Modifier.padding(10.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        names.forEach { (id,label) -> StitchButton(label,active=screen==id,click={selectScreen(id)}) }
    }
    Row(Modifier.padding(12.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text("SERVER UI PRIMITIVES",style=MaterialTheme.typography.labelSmall,color=StitchMuted)
        Text("${LayoutDefinitions.all.size}",color=StitchBlue,style=MaterialTheme.typography.labelSmall)
    }
    val primitives=listOf("column","row","box","text","button","outlinedTextField","switch","image")
    val definitions=if(tab=="Primitives" && query.isBlank()) primitives else LayoutDefinitions.all.map { it.identifier.substringAfter(':') }.filter { it.contains(query,true) }
    LazyColumn(Modifier.fillMaxWidth().heightIn(max=280.dp).padding(horizontal=10.dp)) {
        items(definitions.chunked(2)) { pair -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            pair.forEach { name -> Box(Modifier.weight(1f)) { BuilderPaletteCard(name,palette,{insert(name,null)},{n,p->insert(n,p)},compact=true,active=document.nodes[selected]?.definition=="layout:$name") } }
            if(pair.size==1) Spacer(Modifier.weight(1f))
        } }
    }
    HorizontalDivider(color=StitchBorder)
    Row(Modifier.padding(12.dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Text("LAYER TREE",style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
        Text("  DOM: ${document.nodes.values.count { it.kind!=AuthorNodeKind.ACTION }}",fontSize=9.sp,color=StitchMuted)
        Spacer(Modifier.weight(1f));Text("+",Modifier.clickable(onClick=newScreen),color=StitchMuted,fontSize=16.sp)
    }
    val indices=remember(document) { AuthoringIndices(document) }
    var expanded by remember(document.documentId) { mutableStateOf<Set<String>>(emptySet()) }
    fun ancestors(id: String): List<String> {
        val result=mutableListOf<String>();var current=document.nodes[id]
        while(current?.placement is Placement.Child) { val parent=(current.placement as Placement.Child).parentId;result+=parent;current=document.nodes[parent] }
        return result
    }
    LaunchedEffect(selected,document.documentId) { expanded=expanded+ancestors(selected)+screen }
    val allRows=workspace.tree().filter { it.node.id==screen || ancestors(it.node.id).lastOrNull()==screen }
    val rows=allRows.filter { ancestors(it.node.id).all { parent -> parent in expanded } }
    val labels=allRows.associate { row ->
        val node=row.node
        val children=indices.childrenByParentAndSlot[ParentSlot(node.id,"children")].orEmpty()
        val label=when {
            node.kind==AuthorNodeKind.SCREEN -> "Screen: ${names[node.id].orEmpty()}"
            node.definition=="layout:text" -> (node.properties["text"] as? JsonPrimitive)?.content.orEmpty().take(27)
            node.definition=="layout:outlinedTextField" -> "Text Input · ${(node.properties["stateKey"] as? JsonPrimitive)?.content.orEmpty()}"
            node.definition=="layout:column" -> "V-Stack"
            node.definition=="layout:row" -> "H-Row"
            else -> node.definition.substringAfter(':').replaceFirstChar { it.uppercase() }
        }
        node.id to ((if(children.isNotEmpty()) {if(node.id in expanded) "v  " else ">  "} else "   ")+label)
    }
    EditorTree(workspace,document,rows,selected,{ row ->
        if(row.node.id==selected) expanded=if(row.node.id in expanded) expanded-row.node.id else expanded+row.node.id
        select(row.node.id)
    },change,Modifier.weight(1f),tagPrefix="builder-node-",treeTag="builder-tree",compact=true,labels=labels)
    StitchButton(if(expanded.size>=allRows.size) "Collapse groups" else "Expand all layers",Modifier.fillMaxWidth().padding(horizontal=10.dp),click={expanded=if(expanded.size>=allRows.size) setOf(screen) else allRows.map {it.node.id}.toSet()})

    Row(Modifier.fillMaxWidth().padding(10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        StitchButton("+ Screen",Modifier.weight(1f),click=newScreen)
        StitchButton("Undo",enabled=workspace.session.canUndo,click={change(workspace.undo())})
        StitchButton("Redo",enabled=workspace.session.canRedo,click={change(workspace.redo())})
    }
}

@Composable private fun StitchInspector(node: AuthorNode,workspace: LocalWorkspace,change: (EditOutcome)->Unit,advanced: ()->Unit) {
    fun set(key: String,value: String?) { change(workspace.putProperty(node.id,key,value?.let(::JsonPrimitive))) }
    StitchSection("Auto Layout & Spacing","Flexbox") {
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.4f),RoundedCornerShape(7.dp)).padding(8.dp)) {
                Text("Direction",color=StitchMuted,style=MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                    StitchButton("↓",active=node.definition=="layout:column",click=advanced)
                    StitchButton("→",active=node.definition=="layout:row",click=advanced)
                }
            }
            Column(Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.4f),RoundedCornerShape(7.dp)).padding(8.dp)) {
                Text("Align & Pack",color=StitchMuted,style=MaterialTheme.typography.labelSmall)
                StitchAlignmentGrid(node,workspace,change,advanced)
            }
        }
        Text("Padding Tokens",style=MaterialTheme.typography.bodySmall,color=StitchMuted)
        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) { listOf("None" to "0","Sm · 8" to "8","Md · 16" to "16","Lg · 24" to "24").forEach { (label,value) ->
            StitchButton(label,Modifier.weight(1f).testTag("stitch-padding-$value"),active=(node.properties["paddingAll"] as? JsonPrimitive)?.content==value,click={set("paddingAll",value)})
        } }
    }
    StitchSection("Appearance & Typography","Tokens") {
        Text("Text Style",color=StitchMuted,style=MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf("Title" to "20","Subtitle" to "14","Body" to "12").forEach { (label,size) ->
            Column(Modifier.weight(1f).border(1.dp,StitchBorder,RoundedCornerShape(6.dp)).clickable { if(workspace.catalog[node.definition]?.properties?.any { it.name=="fontSize" }==true) set("fontSize",size) else advanced() }.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Text(label,fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.bodySmall)
                Text("$size / Regular",fontSize=9.sp,color=StitchMuted)
            }
        } }
        Text("Semantic Color Token",color=StitchMuted,style=MaterialTheme.typography.labelSmall)
        listOf("Brand Accent" to "2563EB","Text Primary" to "111827","Surface Card" to "FFFFFF").forEach { (label,hex) ->
            Row(Modifier.fillMaxWidth().border(1.dp,StitchBorder,RoundedCornerShape(4.dp)).clickable {
                val property=if(label=="Surface Card") "backgroundColor" else "color"
                if(workspace.catalog[node.definition]?.properties?.any {it.name==property}==true) set(property,hex) else advanced()
            }.padding(6.dp),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(14.dp).background(Color(0xFF000000 or hex.toLong(16)),RoundedCornerShape(7.dp)).border(1.dp,StitchBorder,RoundedCornerShape(7.dp)))
                Text(label,Modifier.weight(1f).padding(start=6.dp),style=MaterialTheme.typography.bodySmall)
                Text("#$hex",fontSize=9.sp,color=StitchMuted,fontFamily=FontFamily.Monospace)
            }
        }
    }
    StitchSection("State Binding","Local") {
        val keyProperty=if(node.definition=="layout:textField") "onChangeUpdateState" else "stateKey"
        val supports=workspace.catalog[node.definition]?.properties?.any { it.name==keyProperty }==true
        val bound=(node.properties[keyProperty] as? JsonPrimitive)?.content.orEmpty()
        Text("Map this widget to a registered state key.",color=StitchMuted,style=MaterialTheme.typography.bodySmall)
        if(supports) {
            StitchInput(bound,{set(keyProperty,it)},Modifier.fillMaxWidth(),"State key",mono=true)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text("Default value",color=StitchMuted,style=MaterialTheme.typography.labelSmall)
                Text("Unlink",Modifier.clickable { set(keyProperty,null) },color=StitchBlue,style=MaterialTheme.typography.labelSmall)
            }
            val valueProperty=if(node.definition in setOf("layout:textField","layout:outlinedTextField")) "text" else "checked"
            StitchInput((node.properties[valueProperty] as? JsonPrimitive)?.content.orEmpty(),{set(valueProperty,it)},Modifier.fillMaxWidth(),mono=true)
        } else Text("Select an input to edit its state binding.",color=StitchMuted,style=MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun StitchAlignmentGrid(node: AuthorNode,workspace: LocalWorkspace,change: (EditOutcome)->Unit,advanced: ()->Unit) {
    val vertical=listOf("Top","Center","Bottom")
    val horizontal=listOf("Start","Center","End")
    Column(Modifier.padding(top=5.dp),verticalArrangement=Arrangement.spacedBy(3.dp)) {
        vertical.forEachIndexed { y,v -> Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
            horizontal.forEachIndexed { x,h ->
                val properties=when(node.definition) {
                    "layout:column" -> mapOf("horizontalAlignment" to h,"verticalArrangement" to v)
                    "layout:row" -> mapOf("horizontalArrangement" to h,"verticalAlignment" to v)
                    "layout:box" -> mapOf("contentAlignment" to if(v=="Center" && h=="Center") "Center" else v+h)
                    else -> emptyMap()
                }
                val active=properties.isNotEmpty() && properties.all {(key,value)->(node.properties[key] as? JsonPrimitive)?.content==value}
                Box(Modifier.size(14.dp).background(if(active) StitchBlue else StitchBorder)
                    .testTag("stitch-align-$y-$x").clickable {
                        if(properties.isEmpty()) advanced()
                        else change(workspace.apply(properties.map {(key,value)->EditOperation.PutProperty(node.id,key,JsonPrimitive(value))}))
                    })
            }
        } }
    }
}

@Composable private fun StitchConnectionInspector(node: AuthorNode,workspace: LocalWorkspace,document: AuthoringDocument,
    names: Map<String,String>,select: (String)->Unit,change: (EditOutcome)->Unit) {
    val ports=workspace.catalog[node.definition]?.properties.orEmpty().filter {it.kind==PropertyKind.NODES && it.nodeRole==NodeRole.ACTION}.map {it.name}
    var port by remember(node.id) {mutableStateOf(ports.firstOrNull().orEmpty())}
    var target by remember(node.id,document.entryNodeId) {mutableStateOf(names.keys.firstOrNull {it!=document.entryNodeId}.orEmpty())}
    val navigation=document.edges.values.firstOrNull {it.role==EdgeRole.NAVIGATION && it.sourceNodeId==node.id}
    StitchSection("Connection Flow","Local") {
        Text(if(navigation!=null) "${node.definition} → ${names[navigation.targetNodeId]}" else node.definition,style=MaterialTheme.typography.titleMedium)
        Text(node.id,color=StitchMuted,fontFamily=stitchMono(),style=MaterialTheme.typography.labelSmall)
    }
    if(ports.isNotEmpty()) {
        StitchSection("Trigger","Event") {
            FlowRow(horizontalArrangement=Arrangement.spacedBy(5.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {ports.forEach {event->StitchButton(event,active=event==port,click={port=event})}}
        }
        StitchSection("Action","Registered handlers") {
            StitchButton("+ Update state",Modifier.fillMaxWidth(),click={change(workspace.addUpdate(node.id,port))})
            StitchButton("Go back",Modifier.fillMaxWidth(),click={change(workspace.addAction(node.id,port,"navigation:goBack"))})
        }
        StitchSection("Destination","Screen") {
            names.forEach { (id,label)->StitchButton(label,Modifier.fillMaxWidth(),active=target==id,click={target=id}) }
            StitchButton("Connect navigation",Modifier.fillMaxWidth().testTag("builder-connect"),primary=true,enabled=target.isNotBlank(),click={change(workspace.addAction(node.id,port,"navigation:goTo",target))})
        }
    }
    if(node.definition=="navigation:goTo") StitchSection("Destination","Editable") {
        names.forEach { (id,label)->StitchButton(label,Modifier.fillMaxWidth(),active=navigation?.targetNodeId==id,click={change(workspace.setNavigation(node.id,id))}) }
    }
    StitchSection("Connected actions","Document") {
        val edges=document.edges.values.filter {it.sourceNodeId==node.id && it.role!=EdgeRole.NAVIGATION}
        if(edges.isEmpty()) Text("No downstream action for this selection.",color=StitchMuted,style=MaterialTheme.typography.bodySmall)
        edges.forEach {edge ->
            StitchButton("${edge.sourcePort} → ${document.nodes[edge.targetNodeId]?.definition?.substringAfter(':')}",Modifier.fillMaxWidth(),click={select(edge.targetNodeId)})
            Text("Disconnect",Modifier.clickable {change(workspace.disconnect(edge.id))},color=StitchBlue,style=MaterialTheme.typography.labelSmall)
        }
    }
    if(node.kind==AuthorNodeKind.ACTION) StitchSection("Parameters","Schema") {
        PropertyInspector(node,workspace.catalog[node.definition]) {key,value->change(workspace.putProperty(node.id,key,value))}
    }
}
