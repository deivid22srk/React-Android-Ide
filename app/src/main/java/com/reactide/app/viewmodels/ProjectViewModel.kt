package com.reactide.app.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reactide.app.models.BuildStatus
import com.reactide.app.models.FileNode
import com.reactide.app.models.Project
import com.reactide.app.utils.ProjectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProjectViewModel : ViewModel() {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    private val _fileTree = MutableStateFlow<List<FileNode>>(emptyList())
    val fileTree: StateFlow<List<FileNode>> = _fileTree.asStateFlow()

    private val _currentFile = MutableStateFlow<File?>(null)
    val currentFile: StateFlow<File?> = _currentFile.asStateFlow()

    private val _fileContent = MutableStateFlow("")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    private val _buildStatus = MutableStateFlow<BuildStatus>(BuildStatus.Idle)
    val buildStatus: StateFlow<BuildStatus> = _buildStatus.asStateFlow()

    private val _terminalOutput = MutableStateFlow("")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()

    var showCreateDialog by mutableStateOf(false)
    var showImportDialog by mutableStateOf(false)

    fun loadProjects(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _projects.value = ProjectManager.getProjects(context)
            }
        }
    }

    fun createProject(context: Context, name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val project = ProjectManager.createProject(context, name)
                    _projects.value = _projects.value + project
                    _currentProject.value = project
                    loadFileTree()
                } catch (e: Exception) {
                    addTerminalOutput("Error creating project: ${e.message}")
                }
            }
        }
    }

    fun importProject(context: Context, path: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val project = ProjectManager.importProject(context, path)
                    _projects.value = _projects.value + project
                    _currentProject.value = project
                    loadFileTree()
                } catch (e: Exception) {
                    addTerminalOutput("Error importing project: ${e.message}")
                }
            }
        }
    }

    fun openProject(project: Project) {
        _currentProject.value = project
        loadFileTree()
    }

    private fun loadFileTree() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _currentProject.value?.let { project ->
                    _fileTree.value = ProjectManager.getFileTree(project)
                }
            }
        }
    }

    fun openFile(file: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _currentFile.value = file
                if (file.exists() && file.isFile) {
                    _fileContent.value = file.readText()
                }
            }
        }
    }

    fun saveFile(content: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _currentFile.value?.let { file ->
                    file.writeText(content)
                    _fileContent.value = content
                    addTerminalOutput("File saved: ${file.name}")
                }
            }
        }
    }

    fun buildProject(context: Context) {
        viewModelScope.launch {
            _buildStatus.value = BuildStatus.Building
            addTerminalOutput("\nBuilding project...\n")
            
            withContext(Dispatchers.IO) {
                try {
                    _currentProject.value?.let { project ->
                        val result = ProjectManager.buildProject(context, project) { output ->
                            addTerminalOutput(output)
                        }
                        _buildStatus.value = if (result) {
                            BuildStatus.Success("Build completed successfully")
                        } else {
                            BuildStatus.Error("Build failed")
                        }
                    }
                } catch (e: Exception) {
                    _buildStatus.value = BuildStatus.Error(e.message ?: "Unknown error")
                    addTerminalOutput("Build error: ${e.message}\n")
                }
            }
        }
    }

    fun runProject(context: Context) {
        viewModelScope.launch {
            _buildStatus.value = BuildStatus.Running
            addTerminalOutput("\nStarting development server...\n")
            
            withContext(Dispatchers.IO) {
                try {
                    _currentProject.value?.let { project ->
                        ProjectManager.runProject(context, project) { output ->
                            addTerminalOutput(output)
                        }
                    }
                } catch (e: Exception) {
                    _buildStatus.value = BuildStatus.Error(e.message ?: "Unknown error")
                    addTerminalOutput("Run error: ${e.message}\n")
                }
            }
        }
    }

    fun stopProject() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ProjectManager.stopProject()
                _buildStatus.value = BuildStatus.Idle
                addTerminalOutput("\nServer stopped\n")
            }
        }
    }

    private fun addTerminalOutput(output: String) {
        _terminalOutput.value += output
    }

    fun clearTerminal() {
        _terminalOutput.value = ""
    }

    fun createNewFile(fileName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _currentProject.value?.let { project ->
                    val file = File(project.getSrcDirectory(), fileName)
                    file.parentFile?.mkdirs()
                    file.createNewFile()
                    loadFileTree()
                }
            }
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                file.deleteRecursively()
                loadFileTree()
                if (_currentFile.value == file) {
                    _currentFile.value = null
                    _fileContent.value = ""
                }
            }
        }
    }
}
