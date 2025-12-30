package com.reactide.app.utils

import android.content.Context
import com.google.gson.Gson
import com.reactide.app.models.FileNode
import com.reactide.app.models.Project
import java.io.File

object ProjectManager {
    private const val PROJECTS_FILE = "projects.json"
    private var webServer: LocalWebServer? = null
    
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
        File(projectDir, "dist").mkdirs()
        
        val packageJson = File(projectDir, "package.json")
        packageJson.writeText("""
{
  "name": "${name.lowercase().replace(" ", "-")}",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "scripts": {
    "dev": "Simple bundler (built-in)",
    "build": "Simple bundler (built-in)",
    "preview": "Local server on port 3000"
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
    "strict": true
  },
  "include": ["src"]
}
        """.trimIndent())
        
        val indexHtml = File(projectDir, "public/index.html")
        indexHtml.writeText("""
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>$name</title>
  </head>
  <body>
    <div id="root"></div>
  </body>
</html>
        """.trimIndent())
        
        val appTsx = File(projectDir, "src/App.tsx")
        appTsx.writeText("""
import { useState } from 'react';
import './App.css';

function App() {
  const [count, setCount] = useState(0);

  return (
    <div className="App">
      <header className="App-header">
        <h1>Welcome to $name</h1>
        <p>Built with React + TypeScript</p>
        <div className="card">
          <button onClick={() => setCount(count + 1)}>
            count is {count}
          </button>
          <p>Edit <code>src/App.tsx</code> and save to test</p>
        </div>
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
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.App-header {
  padding: 2rem;
  max-width: 600px;
}

.App-header h1 {
  font-size: 3em;
  margin-bottom: 0.5em;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.App-header p {
  font-size: 1.2em;
  margin-bottom: 2em;
  opacity: 0.9;
}

.card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 2em;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

button {
  background: #ffffff;
  color: #667eea;
  border: none;
  border-radius: 8px;
  padding: 0.8em 1.5em;
  font-size: 1.1em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

button:active {
  transform: translateY(0);
}

code {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
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
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
        """.trimIndent())
        
        val readme = File(projectDir, "README.md")
        readme.writeText("""
# $name

Built with **React IDE for Android**

## Features
- ⚡ React 18 + TypeScript
- 🎨 Modern UI with gradients
- 📱 Mobile-optimized
- 🔥 Built-in bundler (no Node.js required!)

## How it works
This project uses React IDE's built-in bundler that:
- Transpiles TypeScript to JavaScript
- Bundles all files
- Serves via local HTTP server
- Uses React from CDN (esm.sh)

## Edit and Test
1. Edit files in `src/`
2. Tap **Build** to bundle
3. Tap **Run** to start server
4. View in **Preview** tab

Enjoy coding on mobile! 🚀
        """.trimIndent())
    }
    
    fun importProject(context: Context, path: String): Project {
        val projectDir = File(path)
        if (!projectDir.exists() || !projectDir.isDirectory) {
            throw IllegalArgumentException("Invalid project path")
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
            onOutput("🔨 Starting build process...\n")
            onOutput("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
            
            val projectDir = project.getDirectory()
            val distDir = File(projectDir, "dist")
            
            if (distDir.exists()) {
                onOutput("🧹 Cleaning old build...\n")
                distDir.deleteRecursively()
            }
            
            onOutput("📦 Bundling JavaScript and CSS...\n")
            val success = SimpleBundler.createProductionBundle(projectDir, distDir)
            
            if (success) {
                val files = distDir.listFiles()?.size ?: 0
                onOutput("\n✅ Build completed successfully!\n")
                onOutput("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                onOutput("📊 Generated $files files in /dist\n")
                onOutput("🚀 Ready to run!\n")
                true
            } else {
                onOutput("\n❌ Build failed\n")
                onOutput("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                onOutput("Please check your code for errors\n")
                false
            }
        } catch (e: Exception) {
            onOutput("\n💥 Error: ${e.message}\n")
            false
        }
    }
    
    fun runProject(context: Context, project: Project, onOutput: (String) -> Unit) {
        try {
            stopProject()
            
            val distDir = File(project.getDirectory(), "dist")
            
            if (!distDir.exists() || distDir.listFiles()?.isEmpty() == true) {
                onOutput("❌ No build found. Please run Build first.\n")
                return
            }
            
            onOutput("🚀 Starting local web server...\n")
            onOutput("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
            
            webServer = LocalWebServer(distDir, 3000)
            webServer?.startServer()
            
            onOutput("✅ Server running!\n")
            onOutput("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            onOutput("🌐 URL: http://localhost:3000\n")
            onOutput("📱 Switch to Preview tab to view\n")
            onOutput("\n💡 Tip: Edit code and rebuild to see changes\n")
            
        } catch (e: Exception) {
            onOutput("❌ Error starting server: ${e.message}\n")
        }
    }
    
    fun stopProject() {
        webServer?.stopServer()
        webServer = null
    }
    
    fun isServerRunning(): Boolean {
        return webServer?.isRunning() == true
    }
}
