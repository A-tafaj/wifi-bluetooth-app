package com.example.aliwifibtapp.activities

import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aliwifibtapp.adapters.WifiListAdapter
import com.example.aliwifibtapp.databinding.ActivityWifiBinding
import com.example.aliwifibtapp.services.NetworkOnClickListener
import com.example.aliwifibtapp.services.WifiInteractor
import com.example.aliwifibtapp.viewmodel.WifiViewModel
import io.reactivex.disposables.CompositeDisposable
import java.util.*

private const val TAG = "WifiActivity"

class WifiActivity : AppCompatActivity(), NetworkOnClickListener {
    private val MY_PERMISSIONS_ACCESS_COARSE_LOCATION: Int = 1
    private lateinit var wifiManager: WifiManager
    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: ActivityWifiBinding
    private lateinit var wifiListAdapter: WifiListAdapter
    private lateinit var clickedNetwork: ScanResult
    var wifiInteractor = WifiInteractor()
    private var connectedNetwork: String? = null
    private val wifiViewModel: WifiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeRecyclerView()

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiViewModel.turnOnWifi()

        checkPermission()

        wifiViewModel.reScanWifi()

        observeViewModel()
    }

    private fun initializeRecyclerView() {
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.wifiRv.addItemDecoration(dividerItemDecoration)
        wifiListAdapter = WifiListAdapter(this, this)

        with(binding.wifiRv) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = wifiListAdapter
        }
    }

    fun observeViewModel() {
        loadWifiList()
        wifiViewModel.connectedNetworkSSID.observe(this, { ssid ->
            wifiListAdapter.updateConnectedWifi(ssid)

        })
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                if (wifiInteractor.isNetworkAvailable(this)) {

                    connectedNetwork = wifiViewModel.getNetworkInfo().ssid
                    //TODO (DO THE SAME AS ONNETWORKCLICK, IF IS THE ALREADY CONNECTED)
                    if (wifiInteractor.checkIfWifiIsSaved(clickedNetwork.SSID)) {
                        wifiInteractor.connectExistingWifi(clickedNetwork)
                    } else {
                        showConnectDialog(clickedNetwork)
                    }

                    Log.d(TAG, "onContextItemSelected: ${clickedNetwork.SSID}")
                    wifiListAdapter.updateConnectedWifi(connectedNetwork!!)
                }

                return true
            }
            2 -> {
                val networkId = wifiInteractor.getWifiConfiguration(clickedNetwork)[0].networkId
                wifiInteractor.forgetWifi(networkId)
                Log.d(TAG, "onContextItemSelected: $clickedNetwork")
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onNetworkLongClickListener(scanResult: ScanResult) {
        super.onNetworkLongClickListener(scanResult)
        clickedNetwork = scanResult
    }


    fun showConnectDialog(scanResult: ScanResult) {
        val dialog = ConnectDialog(scanResult)
        dialog.show(supportFragmentManager, TAG)
    }


    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this@WifiActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@WifiActivity, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_COARSE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                wifiViewModel.reScanWifi()
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                return
            }
            else -> throw IllegalStateException("Unexpected value: $requestCode")
        }
    }

    override fun onNetworkClickListener(scanResult: ScanResult) {
        clickedNetwork = scanResult
        connectedNetwork = wifiViewModel.getNetworkInfo().ssid.replace("\"", "")

        Log.d(TAG, "onNetworkClickListener: $connectedNetwork scanResult = ${scanResult.SSID} -1")

        if (connectedNetwork == scanResult.SSID) {
            Toast.makeText(this, "Already connected with $connectedNetwork", Toast.LENGTH_LONG).show()
        } else {
            if (wifiInteractor.checkIfWifiIsSaved(clickedNetwork.SSID)) {
                wifiInteractor.connectExistingWifi(clickedNetwork)
            } else {
                showConnectDialog(clickedNetwork)
            }
        }
    }

    fun loadWifiList() {
        wifiViewModel.networksList.observe(this, { list ->
            wifiListAdapter.setMyListData(list)
            wifiListAdapter.updateConnectedWifi(wifiViewModel.getNetworkInfo().ssid)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiViewModel.unregisterReciever()
        wifiViewModel.compositeDisposable.clear()
    }
}
