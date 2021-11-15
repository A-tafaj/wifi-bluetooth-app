package com.example.aliwifibtapp.services

import android.net.wifi.ScanResult

interface NetworkOnClickListener {
    fun onNetworkClickListener(scanResult: ScanResult) {}

    fun onNetworkLongClickListener(scanResult: ScanResult) {}
}