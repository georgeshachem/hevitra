package com.georgeshachem.hevitra

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val hevitra_wifi_ssid = String.format("\"%s\"", "HevitraSensor")
    val hevitra_wifi_password = String.format("\"%s\"", "HevitraPassword")

    private val wifiManager: WifiManager
        get() = getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val wifiScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                val netInfo: NetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
                if (ConnectivityManager.TYPE_WIFI == netInfo.getType ()) {
                    if (netInfo.isConnected ()){
                        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val info = wifiManager.connectionInfo
                        val ssid = info.ssid
                        if ( ssid == hevitra_wifi_ssid ){
                            sensor_status_text.text = "Connected to sensor"
                        }
                    }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setupPermissions()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //start button
        btn_connect.setOnClickListener {
            val wifiConfig = WifiConfiguration()
            wifiConfig.SSID = hevitra_wifi_ssid
            wifiConfig.preSharedKey = hevitra_wifi_password

            val wifiManager =
                getSystemService(Context.WIFI_SERVICE) as WifiManager
            val netId = wifiManager.addNetwork(wifiConfig)
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
        }
        //end button

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        this.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            sensor_status_text.text = "Failed to scan for sensors"
        }

    }


    private fun scanSuccess() {
        val results = wifiManager.scanResults
        for (item in results) {
            if (item.SSID == "HevitraSensor"){
                sensor_status_text.text = "A sensor has been found!"
                btn_connect.isEnabled = true
                btn_connect.isVisible = true
                return
            }
        }
        sensor_status_text.text = "No sensor found!"

    }

    private fun scanFailure() {
        sensor_status_text.text = "Failed to get scan results"
    }

    private val locationRequestCode = 99
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            locationRequestCode)
    }

}
