package com.reactide.app.utils

import android.content.Context
import com.google.gson.Gson
import com.reactide.app.models.FileNode
import com.reactide.app.models.Project
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

object ProjectManager {
    private const val PROJECTS_FILE = "projects.json"
    private var currentProcess: Process? = null
    
    fun getProjects(context: Context): List<Project> {
        val file = File(context.filesDir, PROJECTS_FILE)
        if (!file.exists()) return emptyList()
        
        return try {
            val json = file.readText()
            Gson().fromJson(json, Array<Project>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveProjects(context: Context, projects: List<Project>) {
        val file = File(context.filesDir, PROJECTS_FILE)
        val json = Gson().toJson(projects)
        file.writeText(json)
    }
    
    fun createProject(context: Context, name: String): Project {
        val projectsDir = File(context.getExternalFilesDir(null), "projects")
        projectsDir.mkdirs()
        
        val projectDir = File(projectsDir, name)
        if (projectDir.exists()) {
            throw IllegalArgumentException("Project already exists")
        }
        
        projectDir.mkdirs()
        
        createReactTypeScriptProject(projectDir, name)
        
        val project = Project(name, projectDir.absolutePath)
        val projects = getProjects(context) + project
        saveProjects(context, projects)
        
        return project
    }
    
    private fun createReactTypeScriptProject(projectDir: File, name: String) {
        File(projectDir, "src").mkdirs()
        File(projectDir, "public").mkdirs()
        
        val packageJson = File(projectDir, "package.json")
        packageJson.writeText("""
{
  "name": "${name.lowercase().replace(" ", "-")}",
  "version": "0.1.0",
  "private": true,
  "dependencies": {
    "@types/node": "^20.0.0",
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "typescript": "^5.0.0"
  },
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test",
    "eject": "react-scripts eject"
  },
  "eslintConfig": {
    "extends": [
      "react-app"
    ]
  },
  "browserslist": {
    "production": [
      ">0.2%",
      "not dead",
      "not op_mini all"
    ],
    "development": [
      "last 1 chrome version",
      "last 1 firefox version",
      "last 1 safari version"
    ]
  }
}
        """.trimIndent())
        
        val tsConfig = File(projectDir, "tsconfig.json")
        tsConfig.writeText("""
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
        """.trimIndent())
        
        val indexHtml = File(projectDir, "public/index.html")
        indexHtml.writeText("""
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>$name</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
        """.trimIndent())
        
        val appTsx = File(projectDir, "src/App.tsx")
        appTsx.writeText("""
import React, { useState } from 'react';
import './App.css';

function App() {
  const [count, setCount] = useState(0);

  return (
    <div className="App">
      <header className="App-header">
        <h1>Welcome to $name</h1>
        <p>Built with React + TypeScript</p>
        <div className="card">
          <button onClick={() => setCount((count) => count + 1)}>
            count is {count}
          </button>
        </div>
        <p className="read-the-docs">
          Edit <code>src/App.tsx</code> to get started
        </p>
      </header>
    </div>
  );
}

export default App;
        """.trimIndent())
        
        val appCss = File(projectDir, "src/App.css")
        appCss.writeText("""
.App {
  text-align: center;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.App-header {
  padding: 2rem;
}

.App-header h1 {
  font-size: 3.2em;
  line-height: 1.1;
  margin-bottom: 1rem;
}

.card {
  padding: 2em;
}

button {
  border-radius: 8px;
  border: 1px solid transparent;
  padding: 0.6em 1.2em;
  font-size: 1em;
  font-weight: 500;
  font-family: inherit;
  background-color: #1a1a1a;
  cursor: pointer;
  transition: border-color 0.25s;
}

button:hover {
  border-color: #646cff;
}

button:focus,
button:focus-visible {
  outline: 4px auto -webkit-focus-ring-color;
}

.read-the-docs {
  color: #ddd;
}

code {
  background-color: rgba(0, 0, 0, 0.3);
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: monospace;
}
        """.trimIndent())
        
        val mainTsx = File(projectDir, "src/main.tsx")
        mainTsx.writeText("""
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
        """.trimIndent())
        
        val indexCss = File(projectDir, "src/index.css")
        indexCss.writeText("""
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

code {
  font-family: source-code-pro, Menlo, Monaco, Consolas, 'Courier New',
    monospace;
}
        """.trimIndent())
        
        val viteConfig = File(projectDir, "vite.config.ts")
        viteConfig.writeText("""
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    host: true
  }
})
        """.trimIndent())
    }
    
    fun importProject(context: Context, path: String): Project {
        val projectDir = File(path)
        if (!projectDir.exists() || !projectDir.isDirectory) {
            throw IllegalArgumentException("Invalid project path")
        }
        
        val packageJson = File(projectDir, "package.json")
        if (!packageJson.exists()) {
            throw IllegalArgumentException("Not a valid Node.js project")
        }
        
        val name = projectDir.name
        val project = Project(name, projectDir.absolutePath)
        val projects = getProjects(context) + project
        saveProjects(context, projects)
        
        return project
    }
    
    fun getFileTree(project: Project): List<FileNode> {
        val projectDir = project.getDirectory()
        if (!projectDir.exists()) return emptyList()
        
        return buildFileTree(projectDir, projectDir.absolutePath)
    }
    
    private fun buildFileTree(dir: File, rootPath: String): List<FileNode> {
        val files = dir.listFiles() ?: return emptyList()
        
        return files
            .filter { !it.name.startsWith(".") && it.name != "node_modules" && it.name != "build" && it.name != "dist" }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            .map { file ->
                FileNode(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    children = if (file.isDirectory) buildFileTree(file, rootPath) else emptyList()
                )
            }
    }
    
    fun buildProject(context: Context, project: Project, onOutput: (String) -> Unit): Boolean {
        return try {
            onOutput("Installing dependencies...\n")
            val npmInstall = executeCommand(
                arrayOf("npm", "install"),
                project.getDirectory(),
                onOutput
            )
            
            if (!npmInstall) {
                onOutput("Failed to install dependencies\n")
                return false
            }
            
            onOutput("\nBuilding project...\n")
            val buildResult = executeCommand(
                arrayOf("npm", "run", "build"),
                project.getDirectory(),
                onOutput
            )
            
            if (buildResult) {
                onOutput("\n✓ Build completed successfully\n")
            } else {
                onOutput("\n✗ Build failed\n")
            }
            
            buildResult
        } catch (e: Exception) {
            onOutput("Error: ${e.message}\n")
            false
        }
    }
    
    fun runProject(context: Context, project: Project, onOutput: (String) -> Unit) {
        try {
            stopProject()
            
            onOutput("Starting development server...\n")
            onOutput("Server will be available at http://localhost:3000\n\n")
            
            val processBuilder = ProcessBuilder("npm", "start")
            processBuilder.directory(project.getDirectory())
            processBuilder.redirectErrorStream(true)
            
            currentProcess = processBuilder.start()
            
            Thread {
                try {
                    val reader = BufferedReader(InputStreamReader(currentProcess?.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        onOutput("$line\n")
                    }
                } catch (e: Exception) {
                    onOutput("Error reading process output: ${e.message}\n")
                }
            }.start()
            
        } catch (e: Exception) {
            onOutput("Error starting server: ${e.message}\n")
        }
    }
    
    fun stopProject() {
        currentProcess?.destroyForcibly()
        currentProcess = null
    }
    
    private fun executeCommand(
        command: Array<String>,
        workingDir: File,
        onOutput: (String) -> Unit
    ): Boolean {
        return try {
            val processBuilder = ProcessBuilder(*command)
            processBuilder.directory(workingDir)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                onOutput("$line\n")
            }
            
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            onOutput("Command error: ${e.message}\n")
            false
        }
    }
}
