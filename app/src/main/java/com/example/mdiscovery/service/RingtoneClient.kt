package com.example.mdiscovery.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

object RingtoneClient {

    private const val TAG = "RingtoneClient"
    private const val COMMAND_RING = "RING"
    private const val COMMAND_STOP = "STOP"
    private const val CONNECT_TIMEOUT = 5000 // 5秒连接超时

    /**
     * 发送响铃命令到指定设备
     */
    fun sendRingCommand(host: String, port: Int) {
        sendCommand(host, port, COMMAND_RING)
    }

    /**
     * 发送停止响铃命令到指定设备
     */
    fun sendStopCommand(host: String, port: Int) {
        sendCommand(host, port, COMMAND_STOP)
    }

    private fun sendCommand(host: String, port: Int, command: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Socket().use { socket ->
                    socket.connect(java.net.InetSocketAddress(host, port), CONNECT_TIMEOUT)
                    socket.getOutputStream().write("$command\n".toByteArray())
                    socket.getOutputStream().flush()
                    Log.d(TAG, "Command sent: $command to $host:$port")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send command to $host:$port", e)
            }
        }
    }
}
