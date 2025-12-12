package com.root.datamanager.module

import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

object AgentManager {
    // Maps Package Name -> Active Socket
    val agents = ConcurrentHashMap<String, Socket>()

    // Sends a command to the Agent and waits for a single line response
    fun sendCommand(pkg: String, cmd: String): String {
        val socket = agents[pkg] ?: return "Error: Agent disconnected"
        return try {
            val out = PrintWriter(socket.getOutputStream(), true)
            val reader = socket.getInputStream().bufferedReader()
            
            // Send command
            out.println(cmd)
            
            // Read response (Blocking)
            // Expecting response in one line with specific delimiters
            val response = reader.readLine()
            if (response != null) {
                 response.replace("###NEWLINE###", "\n")
            } else {
                agents.remove(pkg)
                "Error: No response"
            }
        } catch (e: Exception) {
            agents.remove(pkg)
            "Error: ${e.message}"
        }
    }
}
