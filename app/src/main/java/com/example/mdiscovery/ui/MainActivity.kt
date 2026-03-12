package com.example.mdiscovery.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mdiscovery.databinding.ActivityMainBinding
import com.example.mdiscovery.service.MdnsService
import com.example.mdiscovery.service.RingtoneClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mdnsService: MdnsService
    private lateinit var deviceAdapter: DeviceAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "部分权限被拒绝，可能影响功能", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 请求必要权限
        requestPermissions()

        // 初始化服务
        mdnsService = MdnsService(this)

        // 设置 RecyclerView
        setupRecyclerView()

        // 设置按钮点击事件
        setupButtons()

        // 收集状态流
        collectFlows()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            // 点击响铃按钮
            device.host?.let { host ->
                RingtoneClient.sendRingCommand(host, device.port)
                Toast.makeText(this, "已向 ${device.name} 发送响铃请求", Toast.LENGTH_SHORT).show()
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceAdapter
        }
    }

    private fun setupButtons() {
        // 注册服务按钮
        binding.btnRegister.setOnClickListener {
            val deviceName = binding.etDeviceName.text.toString().trim()
            if (deviceName.isEmpty()) {
                binding.etDeviceName.error = "请输入设备名称"
                return@setOnClickListener
            }
            mdnsService.registerService(deviceName)
            Toast.makeText(this, "正在注册服务...", Toast.LENGTH_SHORT).show()
        }

        // 注销服务按钮
        binding.btnUnregister.setOnClickListener {
            mdnsService.unregisterService()
            Toast.makeText(this, "服务已注销", Toast.LENGTH_SHORT).show()
        }

        // 开始发现按钮
        binding.btnStartDiscovery.setOnClickListener {
            mdnsService.startDiscovery()
            Toast.makeText(this, "开始扫描设备...", Toast.LENGTH_SHORT).show()
        }

        // 停止发现按钮
        binding.btnStopDiscovery.setOnClickListener {
            mdnsService.stopDiscovery()
            Toast.makeText(this, "已停止扫描", Toast.LENGTH_SHORT).show()
        }
    }

    private fun collectFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 收集注册状态
                launch {
                    mdnsService.isRegistered.collect { isRegistered ->
                        binding.btnRegister.isEnabled = !isRegistered
                        binding.btnUnregister.isEnabled = isRegistered
                        binding.tvRegistrationStatus.text = if (isRegistered) {
                            "状态: 已注册"
                        } else {
                            "状态: 未注册"
                        }
                    }
                }

                // 收集发现状态
                launch {
                    mdnsService.isDiscovering.collect { isDiscovering ->
                        binding.btnStartDiscovery.isEnabled = !isDiscovering
                        binding.btnStopDiscovery.isEnabled = isDiscovering
                        binding.tvDiscoveryStatus.text = if (isDiscovering) {
                            "正在扫描..."
                        } else {
                            "点击开始发现设备"
                        }
                    }
                }

                // 收集发现的设备列表
                launch {
                    mdnsService.discoveredDevices.collect { devices ->
                        deviceAdapter.submitList(devices)
                        binding.tvEmptyState.visibility = if (devices.isEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mdnsService.tearDown()
    }
}
