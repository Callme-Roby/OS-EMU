package com.osemu.app.core

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * Minimal local HTTP server to serve ROM files and HTML to the WebView.
 * Runs on localhost so only the app can access it.
 */
class LocalWebServer(private val port: Int = 0) {

    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool()
    private var running = false

    var actualPort: Int = 0
        private set

    private var htmlContent: String = ""
    private var romFile: File? = null

    fun setContent(html: String, rom: File?) {
        htmlContent = html
        romFile = rom
    }

    fun start() {
        if (running) return
        running = true

        executor.execute {
            try {
                serverSocket = ServerSocket(port)
                actualPort = serverSocket!!.localPort

                while (running) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        executor.execute { handleClient(client) }
                    } catch (e: Exception) {
                        if (running) continue
                    }
                }
            } catch (e: Exception) {
                // Server failed to start
            }
        }

        // Wait for server to be ready
        Thread.sleep(100)
    }

    fun stop() {
        running = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = reader.readLine() ?: return

            val path = requestLine.split(" ").getOrNull(1) ?: "/"

            // Read remaining headers
            var line = reader.readLine()
            while (line != null && line.isNotEmpty()) {
                line = reader.readLine()
            }

            when {
                path == "/" || path == "/index.html" -> {
                    serveHtml(socket)
                }
                path.startsWith("/rom/") -> {
                    serveRom(socket)
                }
                else -> {
                    serve404(socket)
                }
            }
        } catch (e: Exception) {
            // Connection error
        } finally {
            try { socket.close() } catch (e: Exception) { }
        }
    }

    private fun serveHtml(socket: Socket) {
        val body = htmlContent.toByteArray(Charsets.UTF_8)
        val out = socket.getOutputStream()
        val header = buildString {
            append("HTTP/1.1 200 OK\r\n")
            append("Content-Type: text/html; charset=utf-8\r\n")
            append("Content-Length: ${body.size}\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Connection: close\r\n")
            append("\r\n")
        }
        out.write(header.toByteArray())
        out.write(body)
        out.flush()
    }

    private fun serveRom(socket: Socket) {
        val file = romFile
        if (file == null || !file.exists()) {
            serve404(socket)
            return
        }

        val out = socket.getOutputStream()
        val header = buildString {
            append("HTTP/1.1 200 OK\r\n")
            append("Content-Type: application/octet-stream\r\n")
            append("Content-Length: ${file.length()}\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Connection: close\r\n")
            append("\r\n")
        }
        out.write(header.toByteArray())

        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                out.write(buffer, 0, bytesRead)
            }
        }
        out.flush()
    }

    private fun serve404(socket: Socket) {
        val out = PrintWriter(socket.getOutputStream(), true)
        out.print("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n")
        out.flush()
    }
}
