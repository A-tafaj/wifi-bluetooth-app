package com.example.aliwifibtapp.activities

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.aliwifibtapp.databinding.ConnectDialogBinding
import com.example.aliwifibtapp.services.NetworkOnClickListener
import com.example.aliwifibtapp.services.WifiInteractor

class ConnectDialog(val scanResult: ScanResult) : DialogFragment(), NetworkOnClickListener {
    private lateinit var binding: ConnectDialogBinding
    private var wifiInteractor = WifiInteractor()
    val wifiCapability = wifiInteractor.checkNetworkCapabilities(scanResult.capabilities)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        activity?.window?.setGravity(Gravity.CENTER)
        binding = ConnectDialogBinding.inflate(layoutInflater)
        binding.wifiNameDialog.text = scanResult.SSID
        initClickListeners()
        return binding.root
    }

    private fun initClickListeners() {
        binding.connectBtn.setOnClickListener {
            wifiInteractor.connectWifi(scanResult.SSID, getPassword(), wifiCapability)
            dialog?.dismiss()
        }
    }

    fun getPassword(): String {
        return binding.passwordEt.text.toString()
    }
}
