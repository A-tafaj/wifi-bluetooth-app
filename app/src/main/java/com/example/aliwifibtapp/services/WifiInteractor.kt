package com.example.aliwifibtapp.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.aliwifibtapp.WifiBtApp

class WifiInteractor {

    private val TAG: String = "WifiInteractor"
    private var wifiConfiguration: WifiConfiguration = WifiConfiguration()

    private val wifiManager = WifiBtApp.getInstance().applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }

    fun connectWifi(ssid: String, password: String? = null, encryptionType: String) {
        val network = wifiManager.addNetwork(createWifiConfiguration(ssid, password, encryptionType))
        wifiManager.enableNetwork(network, true)
        wifiManager.reconnect()
    }

    @SuppressLint("MissingPermission")
    fun forgetWifi(networkId: Int) {
        val list = wifiManager.configuredNetworks
        for (i in list) {
            wifiManager.removeNetwork(networkId)
            wifiManager.saveConfiguration()
        }
    }

    @SuppressLint("MissingPermission")
    fun checkIfWifiIsSaved(clickedNetwork: String): Boolean {
        val wifiManager = WifiBtApp.getInstance().applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val listOfSavedWifi: List<WifiConfiguration> = wifiManager.configuredNetworks
        var isWifiSaved = false

        for (i in listOfSavedWifi) {
            if (i.SSID != null && i.SSID.replace("\"", "") == clickedNetwork) {
                isWifiSaved = true

                Log.d(TAG, "existing network found: " + i.networkId + " " + i.SSID)
                return isWifiSaved
            }
        }

        return isWifiSaved
    }

    @SuppressLint("MissingPermission")
    fun connectExistingWifi(scanResult: ScanResult) {
        wifiConfiguration = getWifiConfiguration(scanResult)[0]
        wifiConfiguration.SSID = scanResult.SSID
        wifiManager.disconnect()
        wifiManager.enableNetwork(wifiConfiguration.networkId, true)
        wifiManager.reconnect()
    }

    @SuppressLint("MissingPermission")
    fun getWifiConfiguration(scanResult: ScanResult): List<WifiConfiguration> {
        return wifiManager.configuredNetworks.filter { wifi -> wifi.SSID == "\"${scanResult.SSID}\"" }
    }

    fun checkNetworkCapabilities(capability: String): String {
        val networkCapabilities = arrayOf("WEP", "PSK", "WAP")

        for (i in networkCapabilities.indices) {
            if (capability.contains(networkCapabilities[i])) {
                return networkCapabilities[i]
            }
        }
        return "OPEN"
    }

    private fun createWifiConfiguration(ssid: String, password: String?, capability: String): WifiConfiguration? {
        wifiConfiguration.SSID = "\"" + ssid + "\""
        when {
            capability.equals("OPEN", ignoreCase = true) -> {
                configureOpenNetworks(wifiConfiguration)
            }
            capability.equals("WEP", ignoreCase = true) -> {
                configureWEP(wifiConfiguration, password!!)
            }
            capability.equals("PSK", ignoreCase = true) -> {
                configureWPA(wifiConfiguration, password!!)
            }
            else -> {
                return null
            }
        }
        return wifiConfiguration
    }

    private fun configureOpenNetworks(wifiConfiguration: WifiConfiguration): WifiConfiguration {
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        wifiConfiguration.allowedAuthAlgorithms.clear()
        return wifiConfiguration
    }

    private fun configureWEP(wifiConfiguration: WifiConfiguration, password: String): WifiConfiguration {
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)

        wifiConfiguration.wepKeys[0] = password
        wifiConfiguration.wepTxKeyIndex = 0

        return wifiConfiguration
    }

    private fun configureWPA(wifiConfiguration: WifiConfiguration, password: String): WifiConfiguration {
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)

        wifiConfiguration.preSharedKey = "\"" + password + "\""

        return wifiConfiguration
    }
}