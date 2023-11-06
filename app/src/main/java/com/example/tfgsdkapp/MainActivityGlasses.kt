@file:JvmName("MainActivityGlasses")
package com.example.tfgsdkapp

import UIKit.services.AppErrorCode
import UIKit.services.IEvsAppEvents
import UIKit.services.IEvsCommunicationEvents
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.everysight.evskit.android.Evs
class MainActivityGlasses : Activity(), IEvsCommunicationEvents, IEvsAppEvents {

    private val glassesScreen = GlassesScreen()
    private lateinit var txtStatus: TextView
    companion object{
        private const val TAG = "HelloWorldActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPerm()

        initSdk()

        Evs.instance().screens().addScreen(glassesScreen)
        //txtStatus = findViewById(R.id.txtStatus)

        //findViewById<Button>(R.id.btnConfigure).setOnClickListener{
        //    Evs.instance().showUI("configure")
        //}
        //findViewById<Button>(R.id.btnSettings).setOnClickListener{
        //    Evs.instance().showUI("settings")
        //}

    }

    override fun onDestroy() {
        super.onDestroy()
        Evs.instance().unregisterAppEvents(this)
        Evs.instance().comm().unregisterCommunicationEvents(this)
        Evs.instance().stop()
    }

     fun initSdk() {
        Evs.init(this).start()
        Evs.startDefaultLogger()//optional
        Evs.instance().registerAppEvents(this)
        with(Evs.instance().comm()){
            registerCommunicationEvents(this@MainActivityGlasses)
            if(hasConfiguredDevice()) connect()
        }

    }

     fun checkPerm() {
        var permissionsRequested = ArrayList<String>()
        permissionsRequested.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsRequested.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsRequested.add(Manifest.permission.BLUETOOTH_CONNECT)
        }else{
            permissionsRequested.add(Manifest.permission.BLUETOOTH)
        }
        permissionsRequested = validatePermissions(permissionsRequested)
        if (permissionsRequested.size > 0) {

            Log.d(TAG,"validating permissions")
            val req = permissionsRequested.toTypedArray()
            requestPermissions(req, 666)
        }
    }

     fun validatePermissions(permissionsRequested: ArrayList<String>): ArrayList<String> {
        val permissionsToRequest = ArrayList<String>()
        for (permission in permissionsRequested) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }
        return permissionsToRequest
    }

    override fun onAdapterStateChanged(isEnabled: Boolean) {
        runOnUiThread{txtStatus.text = "Adapter enabled=$isEnabled"}
    }

    override fun onConnected() {
        runOnUiThread{txtStatus.text = "${Evs.instance().comm().getDeviceName()} is Connected"}
    }

    override fun onConnecting() {
        runOnUiThread{txtStatus.text = "Connecting ${Evs.instance().comm().getDeviceName()}"}
    }

    override fun onDisconnected() {
        runOnUiThread{txtStatus.text = "Disconnected"}
    }

    override fun onFailedToConnect() {
        runOnUiThread{txtStatus.text = "${Evs.instance().comm().getDeviceName()} Failed to connect"}
    }

    override fun onReady() {
        Evs.instance().display().turnDisplayOn()
    }

    override fun onError(errCode: AppErrorCode, description: String) {
        runOnUiThread{txtStatus.text = "Error: $errCode"}
    }

}