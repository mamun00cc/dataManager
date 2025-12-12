package com.root.datamanager.module

import java.io.File
import java.io.PrintWriter
import java.net.Socket

class AgentClient(private val dataDir: File) : Thread() {
    override fun run() {
        while (true) {
            try {
                // Connect to Host (MT Manager)
                val socket = Socket("127.0.0.1", 9001)
                val out = PrintWriter(socket.getOutputStream(), true)
                val reader = socket.getInputStream().bufferedReader()

                // 1. Send Identity
                out.println(dataDir.parentFile?.name ?: "unknown.pkg")

                // 2. Command Loop
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val cmd = line!!.trim()
                    
                    if (cmd.startsWith("LIST ")) {
                        handleList(cmd.substring(5), out)
                    } else {
                        out.println("Unknown Command")
                    }
                }
                socket.close()
            } catch (e: Exception) {
                // Retry connection after 3 seconds
                Thread.sleep(3000)
            }
        }
    }

    private fun handleList(path: String, out: PrintWriter) {
        try {
            val targetDir = if (path == ".") dataDir else File(path)
            
            if (!targetDir.exists()) {
                out.println("Error: Not Found")
                return
            }

            val files = targetDir.listFiles()
            if (files == null) {
                out.println("")
                return
            }

            // Format: name|isDir|size;name|isDir|size
            val response = files.joinToString(";") { file ->
                "${file.name}|${file.isDirectory}|${file.length()}"
            }
            out.println(response) // Send as single line
        } catch (e: Exception) {
            out.println("Error: ${e.message}")
        }
    }
}
