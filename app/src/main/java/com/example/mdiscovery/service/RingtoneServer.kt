package com.example.mdiscovery.service

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

class RingtoneServer(private val context: Context) {

    companion object {
        private const val TAG = "RingtoneServer"
        private const val COMMAND_RING = "RING"
        private const val COMMAND_STOP = "STOP"
        private const val SOCKET_TIMEOUT = 5000 // 5秒超时
    }

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var ringtone: Ringtone? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(port: Int) {
        if (isRunning) return

        isRunning = true
        scope.launch {
            try {
                serverSocket = ServerSocket(port).apply {
                    soTimeout = SOCKET_TIMEOUT
                }
                Log.d(TAG, "Ringtone server started on port $port")

                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { handleClient(it) }
                    } catch (e: SocketTimeoutException) {
                        // 超时继续循环
                        continue
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e(TAG, "Error accepting connection", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server error", e)
            }
        }
    }

    fun stop() {
        isRunning = false
        stopRingtone()
        scope.cancel()
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server", e)
        }
        Log.d(TAG, "Ringtone server stopped")
    }

    private fun handleClient(socket: Socket) {
        scope.launch {
            try {
                socket.use { client ->
                    val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                    val command = reader.readLine()
                    Log.d(TAG, "Received command: $command")

                    when (command) {
                        COMMAND_RING -> {
                            playRingtone()
                        }
                        COMMAND_STOP -> {
                            stopRingtone()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling client", e)
            }
        }
    }

    private fun playRingtone() {
        Handler(Looper.getMainLooper()).post {
            try {
                // 使用系统默认铃声
                val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringtone = RingtoneManager.getRingtone(context, notification)
                ringtone?.play()
                Log.d(TAG, "Playing ringtone")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing ringtone", e)
            }
        }
    }

    private fun stopRingtone() {
        Handler(Looper.getMainLooper()).post {
            ringtone?.stop()
            ringtone = null
            Log.d(TAG, "Ringtone stopped")
        }
    }
}
