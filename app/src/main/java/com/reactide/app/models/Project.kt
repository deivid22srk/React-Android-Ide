package com.reactide.app.models

import java.io.File

data class Project(
    val name: String,
    val path: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
) {
    fun getDirectory(): File = File(path)
    
    fun getPackageJson(): File = File(path, "package.json")
    
    fun getSrcDirectory(): File = File(path, "src")
    
    fun getPublicDirectory(): File = File(path, "public")
    
    fun exists(): Boolean = getDirectory().exists()
}

data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList()
)
