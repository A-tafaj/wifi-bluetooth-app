package com.example.aliwifibtapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aliwifibtapp.R
import com.example.aliwifibtapp.databinding.WifiItemBinding
import com.example.aliwifibtapp.services.NetworkOnClickListener
import com.example.aliwifibtapp.services.WifiInteractor

class WifiListAdapter(val context: Context, val listener: NetworkOnClickListener) : RecyclerView.Adapter<WifiListAdapter.ViewHolder>() {
    private val TAG = "WifiListAdapter"
    private var wifisList: MutableList<ScanResult> = mutableListOf()
    private var currentlyConnectedWifiSSID = ""
    private var wifiInteractor = WifiInteractor()

    fun updateConnectedWifi(newSSID: String) {
        Log.d(TAG, "old: ${currentlyConnectedWifiSSID}, new = ${newSSID}")

        val currentIndex = wifisList.indexOfFirst { it.SSID == currentlyConnectedWifiSSID.replace("\"", "") }
        notifyItemChanged(currentIndex)

        val newIndex = wifisList.indexOfFirst { it.SSID == newSSID.replace("\"", "") }

        notifyItemChanged(newIndex)
        currentlyConnectedWifiSSID = newSSID.replace("\"", "")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMyListData(wifiList: MutableList<ScanResult>) {
        wifisList = wifiList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.wifi_item, parent, false)
        return ViewHolder(listItem)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: WifiListAdapter.ViewHolder, position: Int) {
        val item = wifisList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return wifisList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: WifiItemBinding = WifiItemBinding.bind(itemView)
        private var wifiSSID: String? = null

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onNetworkClickListener(wifisList[adapterPosition])
                }
            }
            itemView.setOnCreateContextMenuListener { menu, _, _ ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onNetworkLongClickListener(wifisList[adapterPosition])
                    menu?.clear()
                }
                menu?.add(this.adapterPosition, 0, 0, wifisList[adapterPosition].SSID)
                if (!wifisList[adapterPosition].SSID.equals(currentlyConnectedWifiSSID)) {
                    menu?.add(this.adapterPosition, 1, 1, "Connect")
                }
                if (wifiInteractor.checkIfWifiIsSaved(wifisList[adapterPosition].SSID)) {
                    menu?.add(this.adapterPosition, 2, 2, "Forget this network")
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: ScanResult) {
            wifiSSID = item.SSID

            if (item.SSID == currentlyConnectedWifiSSID) {
                binding.ssidTv.setTextColor(Color.BLUE)
                binding.connected.text = "CONNECTED"

            } else {
                binding.ssidTv.setTextColor(Color.GRAY)
                binding.connected.text = ""
            }

            binding.securityTv.text = item.capabilities
            binding.ssidTv.text = item.SSID
            binding.wifiIv.setImageLevel(WifiManager.calculateSignalLevel(item.level, 5))
        }
    }
}