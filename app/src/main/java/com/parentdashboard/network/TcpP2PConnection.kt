package com.parentdashboard.network

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class TcpP2PConnection(
    private val gson: Gson,
    private val scope: CoroutineScope
) {
    private val _incoming = MutableSharedFlow<P2PMessage>(extraBufferCapacity = 32)
    val incoming: SharedFlow<P2PMessage> = _incoming.asSharedFlow()

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var readJob: Job? = null
    private var listenJob: Job? = null
    private val connected = AtomicBoolean(false)

    val isConnected: Boolean get() = connected.get()

    fun listenForClient(
        port: Int = NetworkUtils.P2P_PORT,
        onConnected: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        close()
        listenJob = scope.launch(Dispatchers.IO) {
            val server = ServerSocket(port)
            try {
                val client = server.accept()
                server.close()
                bindSocket(client)
                onConnected()
            } catch (e: Exception) {
                server.close()
                if (isActive) onError(e)
            }
        }
    }

    suspend fun startServer(port: Int = NetworkUtils.P2P_PORT): String? = withContext(Dispatchers.IO) {
        close()
        val server = ServerSocket(port)
        try {
            val client = server.accept()
            server.close()
            bindSocket(client)
            NetworkUtils.getLocalIpAddress()
        } catch (e: Exception) {
            server.close()
            throw e
        }
    }

    suspend fun connect(host: String, port: Int = NetworkUtils.P2P_PORT) = withContext(Dispatchers.IO) {
        close()
        bindSocket(Socket(host, port))
    }

    private fun bindSocket(client: Socket) {
        socket = client
        writer = PrintWriter(client.getOutputStream(), true)
        connected.set(true)
        readJob = scope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(client.getInputStream()))
            while (isActive && connected.get()) {
                val line = reader.readLine() ?: break
                runCatching { gson.fromJson(line, P2PMessage::class.java) }
                    .getOrNull()
                    ?.let { _incoming.tryEmit(it) }
            }
            connected.set(false)
        }
    }

    suspend fun send(message: P2PMessage) = withContext(Dispatchers.IO) {
        if (!connected.get()) return@withContext
        writer?.println(gson.toJson(message))
    }

    fun close() {
        connected.set(false)
        listenJob?.cancel()
        listenJob = null
        readJob?.cancel()
        readJob = null
        runCatching { writer?.close() }
        runCatching { socket?.close() }
        writer = null
        socket = null
    }
}
