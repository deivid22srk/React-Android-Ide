package com.reactide.app.utils

import java.io.File

object SimpleBundler {
    
    fun createProductionBundle(projectDir: File, outputDir: File): Boolean {
        try {
            outputDir.mkdirs()
            
            val srcDir = File(projectDir, "src")
            val publicDir = File(projectDir, "public")
            
            if (!srcDir.exists()) {
                return false
            }
            
            val indexHtml = createIndexHtml(projectDir)
            File(outputDir, "index.html").writeText(indexHtml)
            
            val jsBundle = bundleJavaScript(srcDir)
            File(outputDir, "bundle.js").writeText(jsBundle)
            
            val cssBundle = bundleCSS(srcDir)
            if (cssBundle.isNotEmpty()) {
                File(outputDir, "bundle.css").writeText(cssBundle)
            }
            
            if (publicDir.exists()) {
                copyDirectory(publicDir, outputDir)
            }
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    private fun createIndexHtml(projectDir: File): String {
        val publicIndex = File(projectDir, "public/index.html")
        
        val baseHtml = if (publicIndex.exists()) {
            publicIndex.readText()
        } else {
            """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>${projectDir.name}</title>
              </head>
              <body>
                <div id="root"></div>
              </body>
            </html>
            """.trimIndent()
        }
        
        return baseHtml
            .replace("</head>", "<link rel=\"stylesheet\" href=\"/bundle.css\" /></head>")
            .replace("</body>", "<script type=\"module\" src=\"/bundle.js\"></script></body>")
    }
    
    private fun bundleJavaScript(srcDir: File): String {
        val jsFiles = mutableListOf<File>()
        collectFiles(srcDir, jsFiles, listOf(".js", ".jsx", ".ts", ".tsx"))
        
        val bundle = StringBuilder()
        
        bundle.appendLine("// React IDE Simple Bundle")
        bundle.appendLine("// This is a simplified bundle for preview purposes")
        bundle.appendLine()
        
        bundle.appendLine("""
            // React and ReactDOM from CDN
            import React from 'https://esm.sh/react@18.2.0';
            import ReactDOM from 'https://esm.sh/react-dom@18.2.0/client';
            
        """.trimIndent())
        
        jsFiles.sortedBy { 
            when {
                it.name.contains("main", ignoreCase = true) -> 3
                it.name.contains("app", ignoreCase = true) -> 2
                it.name.contains("index", ignoreCase = true) -> 1
                else -> 0
            }
        }.reversed().forEach { file ->
            bundle.appendLine("// File: ${file.name}")
            
            var content = file.readText()
            
            content = transpileTypeScript(content)
            
            content = transformJSXImports(content)
            
            bundle.appendLine(content)
            bundle.appendLine()
        }
        
        bundle.appendLine("""
            // Auto-initialize React app
            const root = document.getElementById('root');
            if (root && typeof App !== 'undefined') {
                const reactRoot = ReactDOM.createRoot(root);
                reactRoot.render(React.createElement(App));
            }
        """.trimIndent())
        
        return bundle.toString()
    }
    
    private fun bundleCSS(srcDir: File): String {
        val cssFiles = mutableListOf<File>()
        collectFiles(srcDir, cssFiles, listOf(".css"))
        
        val bundle = StringBuilder()
        bundle.appendLine("/* React IDE CSS Bundle */")
        bundle.appendLine()
        
        cssFiles.forEach { file ->
            bundle.appendLine("/* File: ${file.name} */")
            bundle.appendLine(file.readText())
            bundle.appendLine()
        }
        
        return bundle.toString()
    }
    
    private fun transpileTypeScript(content: String): String {
        var result = content
        
        result = result.replace(Regex("\\binterface\\s+\\w+\\s*\\{[^}]*\\}"), "")
        result = result.replace(Regex(":\\s*\\w+(<[^>]+>)?(\\[\\])?"), "")
        result = result.replace(Regex("\\bas\\s+\\w+"), "")
        result = result.replace(Regex("<[^>]+>(?=\\()"), "")
        
        return result
    }
    
    private fun transformJSXImports(content: String): String {
        var result = content
        
        result = result.replace(
            Regex("import\\s+React(,\\s*\\{[^}]*\\})?\\s+from\\s+['\"]react['\"];?"),
            "// React imported from CDN"
        )
        result = result.replace(
            Regex("import\\s+ReactDOM\\s+from\\s+['\"]react-dom(/client)?['\"];?"),
            "// ReactDOM imported from CDN"
        )
        
        result = result.replace(
            Regex("import\\s+([^\\s]+)\\s+from\\s+['\"]\\./([^'\"]+)['\"];?")
        ) { matchResult ->
            "// import ${matchResult.groupValues[1]} from ./${matchResult.groupValues[2]}"
        }
        
        return result
    }
    
    private fun collectFiles(dir: File, collector: MutableList<File>, extensions: List<String>) {
        dir.listFiles()?.forEach { file ->
            when {
                file.isDirectory && !file.name.startsWith(".") -> {
                    collectFiles(file, collector, extensions)
                }
                file.isFile && extensions.any { file.name.endsWith(it) } -> {
                    collector.add(file)
                }
            }
        }
    }
    
    private fun copyDirectory(source: File, target: File) {
        if (!source.exists()) return
        
        source.listFiles()?.forEach { file ->
            val targetFile = File(target, file.name)
            when {
                file.isDirectory -> {
                    targetFile.mkdirs()
                    copyDirectory(file, targetFile)
                }
                file.isFile && file.name != "index.html" -> {
                    file.copyTo(targetFile, overwrite = true)
                }
            }
        }
    }
}
