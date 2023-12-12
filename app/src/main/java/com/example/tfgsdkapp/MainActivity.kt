package com.example.tfgsdkapp

import UIKit.services.AppErrorCode
import UIKit.services.IEvsAppEvents
import UIKit.services.IEvsCommunicationEvents
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.everysight.evskit.android.Evs
import com.example.tfgsdkapp.databinding.ActivityMainBinding
import com.example.tfgsdkapp.utils.Parameters
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.vodafone.v2x.sdk.android.core.messages.cpm_pdu_descriptions.CpmManagementContainer._stationType
import com.vodafone.v2x.sdk.android.facade.SDKConfiguration
import com.vodafone.v2x.sdk.android.facade.SDKConfiguration.SDKConfigurationBuilder
import com.vodafone.v2x.sdk.android.facade.V2XSDK
import com.vodafone.v2x.sdk.android.facade.enums.MqttClientKind
import com.vodafone.v2x.sdk.android.facade.enums.ServiceMode
import com.vodafone.v2x.sdk.android.facade.enums.StationType
import com.vodafone.v2x.sdk.android.facade.events.BaseEvent
import com.vodafone.v2x.sdk.android.facade.events.EventCamListChanged
import com.vodafone.v2x.sdk.android.facade.events.EventITSLocationListChanged
import com.vodafone.v2x.sdk.android.facade.events.EventListener
import com.vodafone.v2x.sdk.android.facade.events.EventType
import com.vodafone.v2x.sdk.android.facade.events.EventV2XConnectivityStateChanged
import com.vodafone.v2x.sdk.android.facade.exception.InvalidConfigException
import com.vodafone.v2x.sdk.android.facade.models.GpsLocation
import com.vodafone.v2x.sdk.android.facade.records.cam.CAMRecord

class MainActivity : AppCompatActivity(), EventListener, OnMapReadyCallback, IEvsCommunicationEvents, IEvsAppEvents {
    private var mHasPermission = false
    private var mIsInitDone = false
    private var mGoogleMap: GoogleMap? = null
    private var mITSMarker: Marker? = null
    private var mCAMMarkers: ArrayList<Marker?>? = null
    private var sdkConfig: SDKConfiguration? = null
    private lateinit var txtStatus: TextView
    private val glassesScreen = GlassesScreen()
    lateinit var lastLocation: GpsLocation
    var myStationType: Int = 0
    var lastCamList: List<CAMRecord>? = null
    var estaMostrando = false
    var mostrarMensaje: Snackbar? = null //Para mostrar la alerta de vehiculo cercano
    val service = Service()

    //GLASSES
    private val screen = GlassesScreen()


    //Objeto que mantiene los elementos UI y provee acceso a ellos
    private var binding: ActivityMainBinding? = null

    private fun initV2XService() {
        try {
            val parameters = Parameters.getInstance(this);
            val mqttClient = MqttClientKind.HiveMQv3
            val cfg = SDKConfigurationBuilder()
                .withMqttClientKind(mqttClient)
            cfg.withMqttUsername("0069d2b0-6bae-479c-ac3f-633d534f96b2")
            cfg.withMqttPassword("9842b50e-f39c-4d8a-9ef6-2f8a8e59e1d7")
            cfg.withStationType(parameters?.stationType ?: StationType.CYCLIST)
            cfg.withCAMServiceEnabled(true)
            cfg.withCamServiceMode(ServiceMode.TxAndRx)
            cfg.withCAMPublishGroup("510482_1")
            cfg.withCAMSubscribeGroup("510482_1")
            //"v2x/cam/510482_1/g8/+/+/+/+/#" esto lo he cambiado para ver si funciona.
            sdkConfig = cfg.build()
            V2XSDK.getInstance().initV2XService(this.applicationContext, sdkConfig)
            mIsInitDone = true
        } catch (e: InvalidConfigException) {
            e.printStackTrace()
        }
        try {
            V2XSDK.getInstance().startV2XService(0, null)
        } catch (e: InvalidConfigException) {
            throw RuntimeException(e)
        }
        if (V2XSDK.getInstance().isV2XServiceInitialized && V2XSDK.getInstance().isV2XServiceStarted) {
            V2XSDK.getInstance().subscribe(
                this,
                EventType.CAM_LIST_CHANGED,
                EventType.ITS_LOCATION_LIST_CHANGED,
                EventType.V2X_CONNECTIVITY_STATE_CHANGED
            )
            if (!V2XSDK.getInstance().isCAMServiceRunning && sdkConfig!!.isCAMServiceEnabled) {
                try {
                    V2XSDK.getInstance().startCAMService()
                } catch (e: IllegalStateException) {
                }
            }
        }
    }

    fun onClickSettingsLogo(view: View?) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Código para que se vea el texto "Connected"
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        //setContentView(R.layout.activity_main);
        val hasPermission = checkLocationPermission()
        if (hasPermission) {
            initV2XService()
            if (!V2XSDK.getInstance().isV2XServiceInitialized) {
                initV2XService()
            }
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this@MainActivity)

        //guardo el stationId que soy yo
        myStationType = V2XSDK.getInstance().sdkConfiguration.stationType.ordinal;

        //NUEVO CODIGO PARA CONECTAR LAS GAFAS:
        val options = hashSetOf("supportSimulator")
        Evs.init(this).startExt(options)
        Evs.instance().comm().setDeviceInfo("udp://192.168.1.146:9321","Simulator")

        checkPerm()
        initSdk()

        Evs.instance().screens().addScreen(screen)

    }

    fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location permission needed")
                    .setMessage("Please give location permission")
                    .setPositiveButton("OK") { dialogInterface, i ->
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    mHasPermission = true
                    if (!mIsInitDone) {
                        initV2XService()
                    }
                }
            }
            if (!mHasPermission) {
                val context = applicationContext
                val text: CharSequence = "Location permission was denied"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
            }
        }
    }

    override fun onMessageBusEvent(baseEvent: BaseEvent) {

        if (baseEvent.eventType == EventType.ITS_LOCATION_LIST_CHANGED) {
            val itsLocationChangedEvent = baseEvent as EventITSLocationListChanged
            if (itsLocationChangedEvent.list.size > 0) {
                val lastItsRecord = itsLocationChangedEvent.list[0]
                val location = lastItsRecord.location
                runOnUiThread { onITSUpdate(location) }
            }
        } else if (baseEvent.eventType == EventType.CAM_LIST_CHANGED) {
            val eventCamListChanged = baseEvent as EventCamListChanged
            runOnUiThread { onCamListChanged(eventCamListChanged) }
            if(eventCamListChanged.list.size > 1 ){
                if(myStationType == _stationType.cyclist) {
                    showWarning(eventCamListChanged) //funcion para ver a que distancia estám
                    //TODO: ver a que velocidad va acercandose
                }
            }
        } else if (baseEvent.eventType == EventType.V2X_CONNECTIVITY_STATE_CHANGED) {
            val eventV2XConnectivityStateChanged = baseEvent as EventV2XConnectivityStateChanged
            setOnUIThread(
                binding!!.v2xConnectivityStateTextView,
                eventV2XConnectivityStateChanged.connectivityState.toString()
            )
        }
    }


    private fun showWarning(eventCamListChanged: EventCamListChanged) {
        var atLeastOneNear = service.compararPosiciones(eventCamListChanged)
        if(atLeastOneNear) {
            if(!estaMostrando){
                val rootView = findViewById<View>(android.R.id.content)
                mostrarMensaje = Snackbar.make(rootView, "Vehiculo cercano", Snackbar.LENGTH_INDEFINITE)
                mostrarMensaje?.show()
                estaMostrando = true
            }
        } else {
            if(estaMostrando) {
                mostrarMensaje?.dismiss()
                estaMostrando = false
            }
        }
    }
    /*private fun compararPosiciones(eventCamListChanged: EventCamListChanged) {
        val camRecords = eventCamListChanged.list
        val myRecord = camRecords.firstOrNull { it.stationID == V2XSDK.getInstance().sdkConfiguration.stationID}
        var atLeastOneNear = false;

        if (myRecord != null) {
            for (record in camRecords) {
                if (record != myRecord) {
                    if (service.estanCerca(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude)) {
                        atLeastOneNear = true
                    }
                }
            }
            if(atLeastOneNear) {
                if(!estaMostrando){
                    val rootView = findViewById<View>(android.R.id.content)
                    mostrarMensaje = Snackbar.make(rootView, "Vehiculo cercano", Snackbar.LENGTH_INDEFINITE)
                    mostrarMensaje?.show()
                    estaMostrando = true
                }
            } else {
                if(estaMostrando) {
                    mostrarMensaje?.dismiss()
                    estaMostrando = false
                }
            }
        }
    }*/


    private fun onCamListChanged(eventCamListChanged: EventCamListChanged) {
        if (mGoogleMap != null) {
            val camRecords = eventCamListChanged.list
            var i = 0
            for (record in camRecords) {
                if (i >= mCAMMarkers!!.size) {
                    break
                }
                val currentMarker = mCAMMarkers!![i]
                val camLatLng = LatLng(record.latitude.toDouble(), record.longitude.toDouble())
                currentMarker!!.position = camLatLng
                currentMarker.rotation = record.headingInDegree
                val isVisible = record.stationID != V2XSDK.getInstance().sdkConfiguration.stationID
                currentMarker.isVisible = isVisible
                //MOSTRAR EL TIPO DE ESTACIÓN QUE ES
                val stationType: StationType = fromValue(record.stationType)
                currentMarker.title = "Station Type: $stationType"

                i++
            }
            val nbToHide = mCAMMarkers!!.size - i
            if (nbToHide <= 0) {
                return
            }
            for (j in 0 until nbToHide) {
                val currentMarker = mCAMMarkers!![i + j]
                currentMarker!!.isVisible = false
            }
        }
    }

    //TO GET THE STATION TYPE CYCLIST, CAR...
    private fun fromValue(value: Int): StationType {
        for (type in StationType.values()) {
            if (type.value == value) {
                return type
            }
        }
        return StationType.UNKNOWN
    }

    private fun onITSUpdate(location: GpsLocation) {
        if (mGoogleMap != null) {
            val bearing = if (location.bearingInDegree != null) location.bearingInDegree else 0.0f
            mITSMarker!!.isVisible = true
            val itsLatLng = LatLng(location.latitude, location.longitude)
            //LatLng centerLatLng = new LatLng(40.333333, -4.187500);
            mITSMarker!!.position = itsLatLng
            mITSMarker!!.rotation = bearing
            val cameraPosition = CameraPosition.Builder()
                .zoom(mGoogleMap!!.cameraPosition.zoom)
                .bearing(0.0f)
                .target(itsLatLng)
                .build()
            mGoogleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap!!.uiSettings.isScrollGesturesEnabled = false
        mGoogleMap!!.moveCamera(CameraUpdateFactory.zoomTo(16.0f))
        val blueArrowBmpDesc = createBitmapDescriptorFromResource(this, R.drawable._334653)
        mITSMarker = createMarker(blueArrowBmpDesc)
        mCAMMarkers = ArrayList()
        val redArrowBmpDesc = createBitmapDescriptorFromResource(this, R.drawable.pointlocation)
        for (i in 0..49) {
            mCAMMarkers!!.add(createMarker(redArrowBmpDesc))
        }
    }

    private fun updateITSLabel(location: GpsLocation) {
        runOnUiThread {
            val newLabelContent = "(" + location.latitude + ", " + location.longitude + ")"
            //mItsTextView.setText(newLabelContent);
        }
    }

    private fun createMarker(icon: BitmapDescriptor): Marker? {
        val option = MarkerOptions()
            .visible(false)
            .position(LatLng(0.0, 0.0))
        val marker = mGoogleMap!!.addMarker(option)
        marker?.setIcon(icon)
        return marker
    }

    //Metodo para mostrar el estado
    private fun setOnUIThread(view: TextView, text: String) {
        runOnUiThread { view.text = text }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val TAG = "MainActivity"
        private fun createBitmapDescriptorFromResource(
            context: Context,
            resourceId: Int
        ): BitmapDescriptor {
            val bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.resources, resourceId),
                100,
                100,
                false
            )
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    //SDK GLASSES FUNCTIONS
    fun initSdk() {
        Evs.init(this).start()
        Evs.startDefaultLogger()//optional
        Evs.instance().registerAppEvents(this)
        with(Evs.instance().comm()){
            registerCommunicationEvents(this@MainActivity)
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

            Log.d(MainActivity.TAG,"validating permissions")
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

    override fun onConnected() {
        runOnUiThread{txtStatus.text = "${Evs.instance().comm().getDeviceName()} is Connected"}
    }

    override fun onDisconnected() {
        runOnUiThread{txtStatus.text = "Disconnected"}
    }

    override fun onError(errCode: AppErrorCode, description: String) {
        runOnUiThread{txtStatus.text = "Error: $errCode"}
    }

    override fun onReady() {
        Evs.instance().display().turnDisplayOn()
    }
}