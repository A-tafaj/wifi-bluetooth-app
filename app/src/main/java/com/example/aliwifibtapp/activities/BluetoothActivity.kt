package com.example.aliwifibtapp.activities

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aliwifibtapp.adapters.BluetoothListAdapter
import com.example.aliwifibtapp.databinding.ActivityBluetoothBinding
import com.example.aliwifibtapp.viewmodel.BluetoothViewModel

class BluetoothActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothBinding
    private lateinit var bluetoothListAdapter: BluetoothListAdapter
    private val bluetoothViewModel: BluetoothViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeRecyclerView()

        bluetoothViewModel.reStartDiscovery()

        loadBluetoothList()
    }

    fun loadBluetoothList() {
        bluetoothViewModel.bluetoothList.observe(this, { list ->
            bluetoothListAdapter.setMyListData(list)
        })
    }

    private fun initializeRecyclerView() {
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.bluetoothRv.addItemDecoration(dividerItemDecoration)
        bluetoothListAdapter = BluetoothListAdapter()

        with(binding.bluetoothRv) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = bluetoothListAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothViewModel.unregisterReceiver()
        bluetoothViewModel.compositeDisposable.clear()
    }
}