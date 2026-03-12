package com.example.mdiscovery.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mdiscovery.R
import com.example.mdiscovery.service.MdnsService
import com.google.android.material.button.MaterialButton

class DeviceAdapter(
    private val onRingClick: (MdnsService.DiscoveredDevice) -> Unit
) : ListAdapter<MdnsService.DiscoveredDevice, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position), onRingClick)
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val tvDeviceHost: TextView = itemView.findViewById(R.id.tvDeviceHost)
        private val tvDevicePort: TextView = itemView.findViewById(R.id.tvDevicePort)
        private val btnRing: MaterialButton = itemView.findViewById(R.id.btnRing)

        fun bind(device: MdnsService.DiscoveredDevice, onRingClick: (MdnsService.DiscoveredDevice) -> Unit) {
            tvDeviceName.text = device.name
            tvDeviceHost.text = device.host ?: "Unknown"
            tvDevicePort.text = "Port: ${device.port}"

            btnRing.setOnClickListener {
                onRingClick(device)
            }
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<MdnsService.DiscoveredDevice>() {
        override fun areItemsTheSame(
            oldItem: MdnsService.DiscoveredDevice,
            newItem: MdnsService.DiscoveredDevice
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: MdnsService.DiscoveredDevice,
            newItem: MdnsService.DiscoveredDevice
        ): Boolean {
            return oldItem == newItem
        }
    }
}
