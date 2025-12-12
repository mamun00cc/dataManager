package com.root.datamanager.module

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ShellClient(private val dataDir: File) : Thread() {
    override fun run() {
        while (true) {
            try {
                // Hub (Manager App) এর সাথে কানেক্ট করার চেষ্টা
                val socket = Socket("localhost", 9001)
                val out = PrintWriter(socket.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                // ১. পরিচয় (Package Name) পাঠানো
                out.println(dataDir.parentFile.name) // e.g., com.target.app

                // ২. কমান্ডের জন্য অপেক্ষা করা
                var cmd: String?
                while (input.readLine().also { cmd = it } != null) {
                    val result = executeCommand(cmd!!)
                    // মাল্টিলাইন আউটপুটকে এক লাইনে এনকোড করে পাঠানো (Hub এ ডিকোড হবে)
                    out.println(result.replace("\n", "###NEWLINE###"))
                }
                socket.close()
            } catch (e: Exception) {
                // কানেক্ট না হলে ২ সেকেন্ড অপেক্ষা করে আবার চেষ্টা
                Thread.sleep(2000)
            }
        }
    }

    private fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("/system/bin/sh", "-c", command),
                null,
                dataDir
            )
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) output.append(line).append("\n")
            while (errorReader.readLine().also { line = it } != null) output.append(line).append("\n")
            
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
