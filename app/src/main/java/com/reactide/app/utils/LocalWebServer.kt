package com.reactide.app.utils

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class LocalWebServer(private val rootDir: File, port: Int = 3000) : NanoHTTPD(port) {
    
    private var isStarted = false
    
    override fun serve(session: IHTTPSession): Response {
        var uri = session.uri
        
        if (uri == "/") {
            uri = "/index.html"
        }
        
        val file = File(rootDir, uri.removePrefix("/"))
        
        if (!file.exists() || !file.canonicalPath.startsWith(rootDir.canonicalPath)) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }
        
        if (file.isDirectory) {
            val indexFile = File(file, "index.html")
            if (indexFile.exists()) {
                return serveFile(indexFile)
            }
            return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Directory listing not allowed")
        }
        
        return serveFile(file)
    }
    
    private fun serveFile(file: File): Response {
        val mimeType = getMimeType(file.name)
        val inputStream = FileInputStream(file)
        return newChunkedResponse(Response.Status.OK, mimeType, inputStream)
    }
    
    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".html") -> "text/html"
            fileName.endsWith(".htm") -> "text/html"
            fileName.endsWith(".js") -> "application/javascript"
            fileName.endsWith(".jsx") -> "application/javascript"
            fileName.endsWith(".ts") -> "application/typescript"
            fileName.endsWith(".tsx") -> "application/typescript"
            fileName.endsWith(".css") -> "text/css"
            fileName.endsWith(".json") -> "application/json"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".gif") -> "image/gif"
            fileName.endsWith(".svg") -> "image/svg+xml"
            fileName.endsWith(".ico") -> "image/x-icon"
            fileName.endsWith(".woff") -> "font/woff"
            fileName.endsWith(".woff2") -> "font/woff2"
            fileName.endsWith(".ttf") -> "font/ttf"
            fileName.endsWith(".eot") -> "application/vnd.ms-fontobject"
            else -> "application/octet-stream"
        }
    }
    
    fun startServer() {
        if (!isStarted) {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            isStarted = true
        }
    }
    
    fun stopServer() {
        if (isStarted) {
            stop()
            isStarted = false
        }
    }
    
    fun isRunning(): Boolean = isStarted
}
