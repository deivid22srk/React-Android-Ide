package com.reactide.app.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.reactide.app.models.BuildStatus
import com.reactide.app.models.FileNode
import com.reactide.app.viewmodels.ProjectViewModel
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
    var showFileTree by remember { mutableStateOf(true) }
    var editedContent by remember(currentFile) { mutableStateOf(fileContent) }
    
    LaunchedEffect(fileContent) {
        editedContent = fileContent
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(currentProject?.name ?: "Editor")
                        currentFile?.let {
                            Text(
                                it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFileTree = !showFileTree }) {
                        Icon(
                            if (showFileTree) Icons.Default.MenuOpen else Icons.Default.Menu,
                            contentDescription = "Toggle File Tree"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.saveFile(editedContent) },
                        enabled = currentFile != null
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.buildProject(context) },
                        enabled = buildStatus !is BuildStatus.Building && buildStatus !is BuildStatus.Running,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Build")
                    }
                    
                    Button(
                        onClick = { 
                            if (buildStatus is BuildStatus.Running) {
                                viewModel.stopProject()
                            } else {
                                viewModel.runProject(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (buildStatus is BuildStatus.Running) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            if (buildStatus is BuildStatus.Running) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (buildStatus is BuildStatus.Running) "Parar" else "Executar")
                    }
                }
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showFileTree) {
                Surface(
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    FileTreeView(
                        fileTree = fileTree,
                        onFileClick = { file ->
                            if (!file.isDirectory) {
                                viewModel.openFile(File(file.path))
                            }
                        }
                    )
                }
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Editor") },
                        icon = { Icon(Icons.Default.Code, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Preview") },
                        icon = { Icon(Icons.Default.Preview, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Terminal") },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = null) }
                    )
                }
                
                when (selectedTab) {
                    0 -> CodeEditorView(
                        content = editedContent,
                        onContentChange = { editedContent = it },
                        fileName = currentFile?.name ?: ""
                    )
                    1 -> PreviewView(
                        isRunning = buildStatus is BuildStatus.Running
                    )
                    2 -> TerminalView(
                        output = terminalOutput,
                        onClear = { viewModel.clearTerminal() }
                    )
                }
            }
        }
    }
}

@Composable
fun FileTreeView(
    fileTree: List<FileNode>,
    onFileClick: (FileNode) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(fileTree) { node ->
            FileTreeItem(
                node = node,
                level = 0,
                onFileClick = onFileClick
            )
        }
    }
}

@Composable
fun FileTreeItem(
    node: FileNode,
    level: Int,
    onFileClick: (FileNode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (node.isDirectory) {
                    expanded = !expanded
                } else {
                    onFileClick(node)
                }
            }
            .padding(start = (level * 16).dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                node.isDirectory && expanded -> Icons.Default.FolderOpen
                node.isDirectory -> Icons.Default.Folder
                node.name.endsWith(".tsx") || node.name.endsWith(".ts") -> Icons.Default.Code
                node.name.endsWith(".json") -> Icons.Default.DataObject
                node.name.endsWith(".css") -> Icons.Default.Palette
                else -> Icons.Default.InsertDriveFile
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (node.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            node.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    
    if (node.isDirectory && expanded) {
        node.children.forEach { child ->
            FileTreeItem(
                node = child,
                level = level + 1,
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Selecione um arquivo para editar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
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

@Composable
fun PreviewView(isRunning: Boolean) {
    if (!isRunning) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Preview,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Execute o projeto para visualizar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Terminal",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onClear) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = output.ifEmpty { "Terminal output will appear here..." },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                color = Color(0xFFCCCCCC)
            )
        }
    }
}
