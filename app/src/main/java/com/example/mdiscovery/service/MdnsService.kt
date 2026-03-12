package com.example.mdiscovery.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MdnsService(private val context: Context) {

    companion object {
        private const val TAG = "MdnsService"
        private const val SERVICE_TYPE = "_myapp._tcp."
        private const val DEFAULT_PORT = 8888
    }

    private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val ringtoneServer: RingtoneServer = RingtoneServer(context)
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: NsdManager.ResolveListener? = null

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val pendingServices = mutableSetOf<NsdServiceInfo>()

    data class DiscoveredDevice(
        val name: String,
        val host: String?,
        val port: Int,
        val serviceType: String
    )

    /**
     * 注册本设备服务，让其他设备可以发现
     */
    fun registerService(deviceName: String) {
        if (registrationListener != null) {
            Log.w(TAG, "Service already registered")
            return
        }

        // 启动响铃服务器
        ringtoneServer.start(DEFAULT_PORT)

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = deviceName
            serviceType = SERVICE_TYPE
            port = DEFAULT_PORT
        }

        registrationListener = createRegistrationListener()
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    /**
     * 注销服务
     */
    fun unregisterService() {
        // 停止响铃服务器
        ringtoneServer.stop()

        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering service", e)
            }
            registrationListener = null
        }
        _isRegistered.value = false
    }

    /**
     * 开始发现局域网内的设备
     */
    fun startDiscovery() {
        if (discoveryListener != null) {
            Log.w(TAG, "Discovery already started")
            return
        }

        // 清空已发现设备列表
        _discoveredDevices.value = emptyList()
        pendingServices.clear()

        discoveryListener = createDiscoveryListener()
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    /**
     * 停止发现
     */
    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping discovery", e)
            }
            discoveryListener = null
        }
        _isDiscovering.value = false
    }

    /**
     * 释放所有资源
     */
    fun tearDown() {
        stopDiscovery()
        unregisterService()
    }

    private fun createRegistrationListener(): NsdManager.RegistrationListener {
        return object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service registered: ${serviceInfo.serviceName}")
                _isRegistered.value = true
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Service registration failed: $errorCode")
                _isRegistered.value = false
                registrationListener = null
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service unregistered: ${serviceInfo.serviceName}")
                _isRegistered.value = false
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Service unregistration failed: $errorCode")
            }
        }
    }

    private fun createDiscoveryListener(): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "Discovery started: $serviceType")
                _isDiscovering.value = true
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${service.serviceName}, type: ${service.serviceType}")
                // 解析服务获取详细信息
                resolveService(service)
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${service.serviceName}")
                // 从列表中移除
                val currentList = _discoveredDevices.value.toMutableList()
                currentList.removeAll { it.name == service.serviceName }
                _discoveredDevices.value = currentList
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped: $serviceType")
                _isDiscovering.value = false
                discoveryListener = null
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: $errorCode")
                _isDiscovering.value = false
                discoveryListener = null
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: $errorCode")
            }
        }
    }

    private fun resolveService(service: NsdServiceInfo) {
        resolveListener = object : NsdManager.ResolveListener {
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service resolved: ${serviceInfo.serviceName}")
                Log.d(TAG, "  Host: ${serviceInfo.host}")
                Log.d(TAG, "  Port: ${serviceInfo.port}")

                val device = DiscoveredDevice(
                    name = serviceInfo.serviceName,
                    host = serviceInfo.host?.hostAddress,
                    port = serviceInfo.port,
                    serviceType = serviceInfo.serviceType
                )

                val currentList = _discoveredDevices.value.toMutableList()
                // 避免重复添加
                currentList.removeAll { it.name == device.name }
                currentList.add(device)
                _discoveredDevices.value = currentList

                pendingServices.remove(service)
            }

            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                pendingServices.remove(service)
            }
        }

        pendingServices.add(service)
        nsdManager.resolveService(service, resolveListener)
    }
}
