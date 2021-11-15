package com.example.aliwifibtapp.viewmodel

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aliwifibtapp.WifiBtApp
import com.example.aliwifibtapp.services.WifiInteractor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class WifiViewModel : ViewModel() {
    private val TAG = "WifiViewModel"

    private lateinit var result: List<ScanResult>

    private var wifiManager: WifiManager = WifiBtApp.getInstance().getWifiManager()

    val compositeDisposable = CompositeDisposable()
    var networksList = MutableLiveData<ArrayList<ScanResult>>()
    var wifiInteractor = WifiInteractor()
    var connectedNetworkSSID = MutableLiveData<String>()

    private var stateWifiReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val connectivityManager = context?.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = connectivityManager.activeNetworkInfo

                if (activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_WIFI && activeNetwork.isConnected) {
                    val info = WifiBtApp.getInstance().getWifiManager().connectionInfo
                    val ssid = info.ssid
                    connectedNetworkSSID.postValue(ssid)
                    Log.d(TAG, "connectedTo: ${getNetworkInfo().ssid}")
                } else {
                    Log.d(TAG, "Connection failed!")
                }
            }
        }
    }
    private var wifiReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(c: Context, intent: Intent) {
            result = wifiManager.scanResults
            result = result.distinctBy { it.SSID }

            networksList.postValue(result as ArrayList<ScanResult>?)
        }
    }

    fun getNetworkInfo(): WifiInfo {

        return wifiManager.connectionInfo
    }

    init {
        turnOnWifi()
        WifiBtApp.getInstance().applicationContext.registerReceiver(stateWifiReceiver, createFilters())
        WifiBtApp.getInstance().applicationContext.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    fun createFilters(): IntentFilter {
        return IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }

    fun scanWifi() {
        wifiManager.startScan()
    }

    fun reScanWifi() {
        compositeDisposable.add(
            Observable.interval(1, 30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (wifiManager.isWifiEnabled) {
                        scanWifi()
                    } else {
                        Toast.makeText(WifiBtApp.getInstance().applicationContext, "wifi turned off", Toast.LENGTH_SHORT).show()
                    }
                }, { throwable ->
                    Log.d(TAG, "No data! ${throwable.localizedMessage}", throwable)
                })
        )
    }

    fun unregisterReciever() {
        WifiBtApp.getInstance().applicationContext.unregisterReceiver(stateWifiReceiver)
        WifiBtApp.getInstance().applicationContext.unregisterReceiver(wifiReceiver)
    }

    fun checkNetworkState(): Boolean {
        if (!wifiInteractor.isNetworkAvailable(WifiBtApp.getInstance().applicationContext)) {
            return true
        }
        return false
    }

    fun turnOnWifi() {
        if (!checkNetworkState()) {
            wifiManager.setWifiEnabled(true)
        }
    }
}