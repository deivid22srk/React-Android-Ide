package com.reactide.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.reactide.app.models.BuildStatus
import com.reactide.app.models.FileNode
import com.reactide.app.viewmodels.ProjectViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsState()
    val fileTree by viewModel.fileTree.collectAsState()
    val currentFile by viewModel.currentFile.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()
    val buildStatus by viewModel.buildStatus.collectAsState()
    val terminalOutput by viewModel.terminalOutput.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var editedContent by remember(currentFile) { mutableStateOf(fileContent) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(fileContent) {
        editedContent = fileContent
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                DrawerFileExplorer(
                    project = currentProject,
                    fileTree = fileTree,
                    currentFile = currentFile,
                    onFileClick = { file ->
                        if (!file.isDirectory) {
                            viewModel.openFile(File(file.path))
                            scope.launch { drawerState.close() }
                        }
                    },
                    onNewFile = { showNewFileDialog = true }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                EditorTopBar(
                    projectName = currentProject?.name ?: "Editor",
                    fileName = currentFile?.name,
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    onBack = onBack,
                    onSave = {
                        viewModel.saveFile(editedContent)
                        Toast.makeText(context, "Arquivo salvo!", Toast.LENGTH_SHORT).show()
                    },
                    onMoreActions = { showActionMenu = true },
                    isSaveEnabled = currentFile != null
                )
            },
            bottomBar = {
                EditorBottomBar(
                    buildStatus = buildStatus,
                    onBuild = { viewModel.buildProject(context) },
                    onRun = {
                        if (buildStatus is BuildStatus.Running) {
                            viewModel.stopProject()
                        } else {
                            viewModel.runProject(context)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    edgePadding = 8.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Editor") },
                        icon = { Icon(Icons.Default.Code, null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Preview") },
                        icon = { Icon(Icons.Default.Preview, null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Terminal")
                                if (buildStatus is BuildStatus.Running || buildStatus is BuildStatus.Building) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.small
                                            )
                                    )
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Terminal, null) }
                    )
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> CodeEditorView(
                            content = editedContent,
                            onContentChange = { editedContent = it },
                            fileName = currentFile?.name ?: ""
                        )
                        1 -> PreviewView(isRunning = buildStatus is BuildStatus.Running)
                        2 -> TerminalView(
                            output = terminalOutput,
                            onClear = { viewModel.clearTerminal() },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Output", terminalOutput))
                                Toast.makeText(context, "Logs copiados!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewFileDialog) {
        NewFileDialog(
            onDismiss = { showNewFileDialog = false },
            onConfirm = { fileName ->
                viewModel.createNewFile(fileName)
                showNewFileDialog = false
            }
        )
    }

    if (showActionMenu) {
        ActionMenuDialog(
            onDismiss = { showActionMenu = false },
            onExportLogs = {
                val file = File(context.getExternalFilesDir(null), "build_logs.txt")
                file.writeText(terminalOutput)
                Toast.makeText(context, "Logs exportados para ${file.absolutePath}", Toast.LENGTH_LONG).show()
                showActionMenu = false
            },
            onClearBuild = {
                currentProject?.let { project ->
                    val buildDir = File(project.path, "build")
                    if (buildDir.exists()) {
                        buildDir.deleteRecursively()
                        Toast.makeText(context, "Build limpo!", Toast.LENGTH_SHORT).show()
                    }
                }
                showActionMenu = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    projectName: String,
    fileName: String?,
    onNavigationClick: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onMoreActions: () -> Unit,
    isSaveEnabled: Boolean
) {
    TopAppBar(
        title = { 
            Column {
                Text(
                    projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                fileName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Voltar")
            }
        },
        actions = {
            IconButton(onClick = onNavigationClick) {
                Icon(Icons.Default.Folder, "Arquivos")
            }
            IconButton(
                onClick = onSave,
                enabled = isSaveEnabled
            ) {
                Icon(
                    Icons.Default.Save,
                    "Salvar",
                    tint = if (isSaveEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
            IconButton(onClick = onMoreActions) {
                Icon(Icons.Default.MoreVert, "Mais ações")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun EditorBottomBar(
    buildStatus: BuildStatus,
    onBuild: () -> Unit,
    onRun: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBuild,
                enabled = buildStatus !is BuildStatus.Building && buildStatus !is BuildStatus.Running,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Build, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Build", fontSize = 14.sp)
            }
            
            Spacer(Modifier.width(8.dp))
            
            Button(
                onClick = onRun,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (buildStatus is BuildStatus.Running) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    if (buildStatus is BuildStatus.Running) Icons.Default.Stop else Icons.Default.PlayArrow,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (buildStatus is BuildStatus.Running) "Parar" else "Executar",
                    fontSize = 14.sp
                )
            }
            
            if (buildStatus is BuildStatus.Building || buildStatus is BuildStatus.Running) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun DrawerFileExplorer(
    project: com.reactide.app.models.Project?,
    fileTree: List<FileNode>,
    currentFile: File?,
    onFileClick: (FileNode) -> Unit,
    onNewFile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Arquivos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    project?.let {
                        Text(
                            it.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                IconButton(onClick = onNewFile) {
                    Icon(
                        Icons.Default.AddCircle,
                        "Novo arquivo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Divider()
        
        if (fileTree.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nenhum arquivo encontrado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(fileTree) { node ->
                    FileTreeItem(
                        node = node,
                        level = 0,
                        currentFile = currentFile,
                        onFileClick = onFileClick
                    )
                }
            }
        }
    }
}

@Composable
fun FileTreeItem(
    node: FileNode,
    level: Int,
    currentFile: File?,
    onFileClick: (FileNode) -> Unit
) {
    var expanded by remember { mutableStateOf(level == 0) }
    val isSelected = currentFile?.absolutePath == node.path
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (node.isDirectory) {
                    expanded = !expanded
                } else {
                    onFileClick(node)
                }
            },
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
        else 
            Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = (level * 20 + 12).dp,
                    top = 8.dp,
                    bottom = 8.dp,
                    end = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    node.isDirectory && expanded -> Icons.Default.FolderOpen
                    node.isDirectory -> Icons.Default.Folder
                    node.name.endsWith(".tsx") || node.name.endsWith(".ts") -> Icons.Default.Code
                    node.name.endsWith(".json") -> Icons.Default.DataObject
                    node.name.endsWith(".css") -> Icons.Default.Palette
                    node.name.endsWith(".html") -> Icons.Default.Language
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    node.isDirectory -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                node.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
    
    if (node.isDirectory && expanded) {
        node.children.forEach { child ->
            FileTreeItem(
                node = child,
                level = level + 1,
                currentFile = currentFile,
                onFileClick = onFileClick
            )
        }
    }
}

@Composable
fun CodeEditorView(
    content: String,
    onContentChange: (String) -> Unit,
    fileName: String
) {
    if (fileName.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Text(
                    "Selecione um arquivo para editar",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Abra o menu de arquivos para começar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun PreviewView(isRunning: Boolean) {
    if (!isRunning) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Preview,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                )
                Text(
                    "Execute o projeto para visualizar",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Clique em 'Executar' para iniciar o servidor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl("http://localhost:3000")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun TerminalView(
    output: String,
    onClear: () -> Unit,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E1E1E),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Terminal,
                        "Terminal",
                        tint = Color(0xFF4EC9B0),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Terminal",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFCCCCCC),
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(
                            Icons.Default.ContentCopy,
                            "Copiar logs",
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Clear,
                            "Limpar",
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = output.ifEmpty { "$ Aguardando comandos...\n" },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                color = Color(0xFF9CDCFE)
            )
        }
    }
}

@Composable
fun NewFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Arquivo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Digite o nome do arquivo com extensão:")
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nome do arquivo") },
                    placeholder = { Text("App.tsx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (fileName.isNotBlank()) onConfirm(fileName) },
                enabled = fileName.isNotBlank()
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ActionMenuDialog(
    onDismiss: () -> Unit,
    onExportLogs: () -> Unit,
    onClearBuild: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ações") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onExportLogs,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar logs")
                }
                TextButton(
                    onClick = onClearBuild,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Limpar build")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
