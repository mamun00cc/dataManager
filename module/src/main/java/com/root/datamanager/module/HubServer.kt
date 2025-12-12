package com.root.datamanager.module

import java.net.ServerSocket
import kotlin.concurrent.thread

class HubServer(private val port: Int) : Thread() {
    override fun run() {
        try {
            val server = ServerSocket(port)
            while (true) {
                val socket = server.accept()
                thread {
                    try {
                        // Handshake: Client sends package name first
                        val reader = socket.getInputStream().bufferedReader()
                        val pkgName = reader.readLine()
                        
                        if (!pkgName.isNullOrEmpty()) {
                            AgentManager.agents[pkgName] = socket
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
