package com.root.datamanager.client

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var logTv: TextView
    // কানেক্ট করা অ্যাপগুলোর লিস্ট (PackageName -> Socket)
    private val connectedApps = ConcurrentHashMap<String, Socket>()
    private var activeTarget: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logTv = TextView(this).apply { text = "Starting Servers...\n" }
        setContentView(ScrollView(this).apply { addView(logTv) })

        startAgentServer() // পোর্ট 9001 (প্যাচ করা অ্যাপের জন্য)
        startTermuxServer() // পোর্ট 9000 (আপনার জন্য)
    }

    private fun log(msg: String) {
        Handler(Looper.getMainLooper()).post { logTv.append("$msg\n") }
    }

    // ১. এজেন্ট সার্ভার: প্যাচ করা অ্যাপ এখানে কানেক্ট করবে
    private fun startAgentServer() {
        thread {
            try {
                val server = ServerSocket(9001)
                log("Agent Server listening on 9001...")
                while (true) {
                    val socket = server.accept()
                    thread { handleAgent(socket) }
                }
            } catch (e: Exception) { log("Agent Error: ${e.message}") }
        }
    }

    private fun handleAgent(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            // কানেক্ট হওয়ার পর প্রথম লাইন হবে প্যাকেজ নেম
            val packageName = reader.readLine()
            if (packageName != null) {
                connectedApps[packageName] = socket
                log("New App Connected: $packageName")
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ২. টার্মাক্স সার্ভার: আপনি এখানে কানেক্ট করবেন
    private fun startTermuxServer() {
        thread {
            try {
                val server = ServerSocket(9000)
                log("Termux Server listening on 9000...")
                while (true) {
                    val socket = server.accept()
                    thread { handleTermuxSession(socket) }
                }
            } catch (e: Exception) { log("Termux Error: ${e.message}") }
        }
    }

    private fun handleTermuxSession(socket: Socket) {
        val out = PrintWriter(socket.getOutputStream(), true)
        val input = BufferedReader(InputStreamReader(socket.getInputStream()))

        out.println("--- InsideOut Hub ---")
        out.println("Running Apps: ${connectedApps.keys.size}")
        
        var currentAppSocket: Socket? = null
        var currentAppWriter: PrintWriter? = null
        var currentAppReader: BufferedReader? = null

        out.print("hub $ ")
        out.flush()

        var line: String?
        while (input.readLine().also { line = it } != null) {
            val cmd = line!!.trim()

            // যদি আমরা কোনো অ্যাপের ভেতরে ঢুকে থাকি, সব কমান্ড সেখানে পাঠাবো
            if (currentAppSocket != null) {
                if (cmd == "exit_app") {
                    currentAppSocket = null
                    out.println("Disconnected from app. Back to Hub.")
                } else {
                    // অ্যাপের কাছে কমান্ড পাঠানো
                    currentAppWriter?.println(cmd)
                    // অ্যাপ থেকে রেসপন্স আসা পর্যন্ত অপেক্ষা (Simple blocking read)
                    // (Real-world এ এটা অ্যাসিঙ্ক্রোনাস হওয়া উচিত, কিন্তু এখনকার জন্য চলবে)
                    val response = currentAppReader?.readLine()?.replace("###NEWLINE###", "\n")
                    out.println(response)
                }
            } else {
                // আমরা হাব-এ আছি
                when {
                    cmd == "ls" -> {
                        out.println("Available Apps:")
                        connectedApps.keys.forEach { out.println(" - $it") }
                    }
                    cmd.startsWith("cd ") -> {
                        val targetPkg = cmd.substring(3).trim()
                        if (connectedApps.containsKey(targetPkg)) {
                            currentAppSocket = connectedApps[targetPkg]
                            currentAppWriter = PrintWriter(currentAppSocket!!.getOutputStream(), true)
                            currentAppReader = BufferedReader(InputStreamReader(currentAppSocket!!.getInputStream()))
                            out.println("Switched to $targetPkg (Type 'exit_app' to return)")
                        } else {
                            out.println("App not found or not running.")
                        }
                    }
                    cmd == "exit" -> break
                    else -> out.println("Commands: ls, cd <package_name>, exit")
                }
            }
            if (currentAppSocket == null) out.print("hub $ ") else out.print("shell $ ")
            out.flush()
        }
        socket.close()
    }
}
