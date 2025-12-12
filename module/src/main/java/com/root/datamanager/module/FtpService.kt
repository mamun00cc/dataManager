package com.root.datamanager.module

import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.*
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import java.io.InputStream
import java.io.OutputStream

class FtpService(private val port: Int) : Thread() {
    override fun run() {
        try {
            val serverFactory = FtpServerFactory()
            val listenerFactory = ListenerFactory()
            listenerFactory.port = port
            
            serverFactory.addListener("default", listenerFactory.createListener())
            
            // Allow Anonymous Login
            val userManagerFactory = PropertiesUserManagerFactory()
            serverFactory.userManager = userManagerFactory.createUserManager()

            // Set Custom File System
            serverFactory.fileSystem = FileSystemFactory { 
                VirtualFileSystemView()
            }

            val server = serverFactory.createServer()
            server.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// --- Virtual File System Classes ---

class VirtualFileSystemView : FileSystemView {
    override fun getHomeDirectory(): FtpFile = VirtualRoot()
    override fun getWorkingDirectory(): FtpFile = VirtualRoot()
    override fun changeWorkingDirectory(dir: String?): Boolean = true
    override fun isRandomAccessible(): Boolean = false
    override fun dispose() {}

    override fun getFile(file: String?): FtpFile {
        val path = file ?: "/"
        if (path == "/" || path == "./") return VirtualRoot()

        val parts = path.split("/").filter { it.isNotEmpty() }
        if (parts.isEmpty()) return VirtualRoot()

        val pkgName = parts[0]
        
        if (parts.size == 1) {
            return AppRoot(pkgName)
        }

        val internalPath = "/" + parts.drop(1).joinToString("/")
        return RemoteFile(pkgName, internalPath, parts.last())
    }
}

class VirtualRoot : BaseFtpFile() {
    override fun getAbsolutePath() = "/"
    override fun getName() = "/"
    override fun isDirectory() = true
    override fun getSize() = 0L

    override fun listFiles(): List<FtpFile> {
        return AgentManager.agents.keys.map { AppRoot(it) }
    }
}

class AppRoot(private val pkg: String) : BaseFtpFile() {
    override fun getAbsolutePath() = "/$pkg"
    override fun getName() = pkg
    override fun isDirectory() = true
    override fun getSize() = 0L

    override fun listFiles(): List<FtpFile> {
        return fetchRemoteFiles(pkg, ".")
    }
}

class RemoteFile(
    private val pkg: String, 
    private val path: String, 
    private val nameStr: String,
    private val isDir: Boolean = false,
    private val sizeVal: Long = 0L
) : BaseFtpFile() {
    
    override fun getAbsolutePath() = "/$pkg$path"
    override fun getName() = nameStr
    override fun isDirectory() = isDir
    override fun getSize() = sizeVal

    override fun listFiles(): List<FtpFile> {
        if (!isDir) return emptyList()
        return fetchRemoteFiles(pkg, path)
    }
}

// --- Helper Functions & Base Class ---

abstract class BaseFtpFile : FtpFile {
    override fun isHidden() = false
    override fun doesExist() = true
    override fun isReadable() = true
    override fun isWritable() = true 
    override fun isRemovable() = false
    override fun getOwnerName() = "admin"
    override fun getGroupName() = "admin"
    override fun getLinkCount() = 1
    override fun getLastModified() = System.currentTimeMillis()
    override fun createInputStream(offset: Long): InputStream? = null
    override fun createOutputStream(offset: Long): OutputStream? = null
    override fun mkdir() = false
    override fun delete() = false
    override fun move(dest: FtpFile?) = false
    override fun setLastModified(time: Long) = false
    override fun isFile() = !isDirectory
    
    // **FIXED HERE:** This method was missing
    override fun getPhysicalFile(): Any? = null
}

fun fetchRemoteFiles(pkg: String, path: String): List<FtpFile> {
    val cmd = "LIST $path"
    val response = AgentManager.sendCommand(pkg, cmd)
    
    if (response.startsWith("Error") || response.isEmpty()) return emptyList()

    return response.split(";").mapNotNull { entry ->
        val parts = entry.split("|")
        if (parts.size == 3) {
            val name = parts[0]
            val isDir = parts[1].toBoolean()
            val size = parts[2].toLong()
            
            val newPath = if (path == ".") "/$name" else "$path/$name"
            
            RemoteFile(pkg, newPath, name, isDir, size)
        } else null
    }
}
