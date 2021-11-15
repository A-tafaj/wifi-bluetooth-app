package com.example.aliwifibtapp.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aliwifibtapp.WifiBtApp
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class BluetoothViewModel : ViewModel() {
    private val TAG = "BluetoothViewModel"
    private lateinit var deviceName: String
    val bluetoothList = MutableLiveData<String>()
    val compositeDisposable = CompositeDisposable()
    private val mBtAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d(TAG, "onReceive: ACTION_FOUND")
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (!device?.name.isNullOrEmpty()) {
                        deviceName = device?.name ?: "Not Named"
                        bluetoothList.postValue(deviceName)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED")
                }
            }
        }
    }

    fun reStartDiscovery() {
        compositeDisposable.add(
            Observable.interval(1, 120, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    startDiscovery()

                }, { throwable ->
                    Log.d(TAG, "No data! ${throwable.localizedMessage}", throwable)
                })
        )
    }

    private fun startDiscovery() {
        if (!mBtAdapter.isEnabled) {
            mBtAdapter.enable()
            mBtAdapter.startDiscovery()
        } else {
            mBtAdapter.startDiscovery()
        }

    }

    private fun intentFilters(): IntentFilter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    }

    init {
        WifiBtApp.getInstance().applicationContext.registerReceiver(receiver, intentFilters())
    }

    fun unregisterReceiver() {
        WifiBtApp.getInstance().applicationContext.unregisterReceiver(receiver)
    }
}