package com.example.tfgsdkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tfgsdkapp.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vodafone.v2x.sdk.android.facade.SDKConfiguration;
import com.vodafone.v2x.sdk.android.facade.V2XSDK;
import com.vodafone.v2x.sdk.android.facade.enums.MqttClientKind;
import com.vodafone.v2x.sdk.android.facade.enums.ServiceMode;
import com.vodafone.v2x.sdk.android.facade.enums.StationType;
import com.vodafone.v2x.sdk.android.facade.enums.V2XConnectivityState;
import com.vodafone.v2x.sdk.android.facade.enums.V2XServiceState;
import com.vodafone.v2x.sdk.android.facade.enums.V2XServices;
import com.vodafone.v2x.sdk.android.facade.events.BaseEvent;
import com.vodafone.v2x.sdk.android.facade.events.EventCamListChanged;
import com.vodafone.v2x.sdk.android.facade.events.EventITSLocationListChanged;
import com.vodafone.v2x.sdk.android.facade.events.EventListener;
import com.vodafone.v2x.sdk.android.facade.events.EventType;
import com.vodafone.v2x.sdk.android.facade.events.EventV2XConnectivityStateChanged;
import com.vodafone.v2x.sdk.android.facade.exception.InvalidConfigException;
import com.vodafone.v2x.sdk.android.facade.models.GpsLocation;
import com.vodafone.v2x.sdk.android.facade.records.ITSLocationRecord;
import com.vodafone.v2x.sdk.android.facade.records.cam.CAMRecord;


import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements EventListener, OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean mHasPermission = false;
    private boolean mIsInitDone = false;
    private GoogleMap mGoogleMap = null;
    private Marker mITSMarker;
    private ArrayList<Marker> mCAMMarkers;

    private SDKConfiguration sdkConfig;
    //Objeto que mantiene los elementos UI y provee acceso a ellos

    private ActivityMainBinding binding;

    private void initV2XService() {
        try {

            MqttClientKind mqttClient = MqttClientKind.HiveMQv3;
            SDKConfiguration.SDKConfigurationBuilder cfg = new SDKConfiguration.SDKConfigurationBuilder()
                    .withMqttClientKind(mqttClient);
            cfg.withMqttUsername("0069d2b0-6bae-479c-ac3f-633d534f96b2");
            cfg.withMqttPassword("9842b50e-f39c-4d8a-9ef6-2f8a8e59e1d7");
            cfg.withStationType(StationType.CYCLIST);
            cfg.withCAMServiceEnabled(true);
            cfg.withCamServiceMode(ServiceMode.TxAndRx);
            cfg.withCAMPublishGroup("510482_1");
            cfg.withCAMSubscribeGroup("510482_1");
            //"v2x/cam/510482_1/g8/+/+/+/+/#" esto lo he cambiado para ver si funciona.
            sdkConfig = cfg.build();
            V2XSDK.getInstance().initV2XService(this.getApplicationContext(), sdkConfig);

            this.mIsInitDone = true;
        } catch (InvalidConfigException e) {
            e.printStackTrace();
        }

        try {
            V2XSDK.getInstance().startV2XService(0, null);
        } catch (InvalidConfigException e) {
            throw new RuntimeException(e);
        }

        if (V2XSDK.getInstance().isV2XServiceInitialized() && V2XSDK.getInstance().isV2XServiceStarted()) {
            V2XSDK.getInstance().subscribe(this,
                    EventType.CAM_LIST_CHANGED,
                    EventType.ITS_LOCATION_LIST_CHANGED,
                    EventType.V2X_CONNECTIVITY_STATE_CHANGED
            );
            if (!V2XSDK.getInstance().isCAMServiceRunning() && sdkConfig.isCAMServiceEnabled()) {
                try {
                    V2XSDK.getInstance().startCAMService();
                } catch (IllegalStateException e) {
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Código para que se vea el texto "Connected"
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        //setContentView(R.layout.activity_main);

        boolean hasPermission = checkLocationPermission();
        if (hasPermission) {
            initV2XService();
            if (!V2XSDK.getInstance().isV2XServiceInitialized()) {
                initV2XService();
            }
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MainActivity.this);

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location permission needed")
                        .setMessage("Please give location permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION
                                );
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION
                );
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mHasPermission = true;

                    if (!mIsInitDone) {
                        initV2XService();
                    }
                }
            }

            if (!mHasPermission) {
                Context context = getApplicationContext();
                CharSequence text = "Location permission was denied";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    @Override
    public void onMessageBusEvent(BaseEvent baseEvent) {

        if (baseEvent.getEventType() == EventType.ITS_LOCATION_LIST_CHANGED) {
            EventITSLocationListChanged itsLocationChangedEvent = (EventITSLocationListChanged) baseEvent;
            if (itsLocationChangedEvent.getList().size() > 0) {
                ITSLocationRecord lastItsRecord = itsLocationChangedEvent.getList().get(0);
                GpsLocation location = lastItsRecord.getLocation();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onITSUpdate(location);
                    }
                });
            }
        } else if (baseEvent.getEventType() == EventType.CAM_LIST_CHANGED) {
                EventCamListChanged eventCamListChanged = (EventCamListChanged) baseEvent;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCamListChanged(eventCamListChanged);
                    }
                });
            } else if (baseEvent.getEventType() == EventType.V2X_CONNECTIVITY_STATE_CHANGED) {
                    EventV2XConnectivityStateChanged eventV2XConnectivityStateChanged = (EventV2XConnectivityStateChanged) baseEvent;
                    setOnUIThread(binding.v2xConnectivityStateTextView, eventV2XConnectivityStateChanged.getConnectivityState().toString());

            }

    }

    private void onCamListChanged(EventCamListChanged eventCamListChanged) {
        if (this.mGoogleMap != null) {
            List<CAMRecord> camRecords = eventCamListChanged.getList();
            int i = 0;
            for (CAMRecord record: camRecords) {
                if (i >= mCAMMarkers.size()) {
                    break;
                }
                Marker currentMarker = mCAMMarkers.get(i);
                LatLng camLatLng = new LatLng(record.getLatitude(), record.getLongitude());
                currentMarker.setPosition(camLatLng);
                currentMarker.setRotation(record.getHeadingInDegree());
                boolean isVisible =
                        record.getStationID() != V2XSDK.getInstance().getSdkConfiguration().getStationID();
                currentMarker.setVisible(isVisible);
                i++;
            }

            int nbToHide = mCAMMarkers.size() - i;
            if (nbToHide <= 0) {
                return;
            }

            for (int j = 0; j < nbToHide; j++) {
                Marker currentMarker = mCAMMarkers.get(i + j);
                currentMarker.setVisible(false);
            }
        }
    }

    private void onITSUpdate(GpsLocation location) {
        if (this.mGoogleMap != null) {

            float bearing = location.getBearingInDegree() != null ?
                    location.getBearingInDegree() : 0.0f;

            mITSMarker.setVisible(true);
            LatLng itsLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            //LatLng centerLatLng = new LatLng(40.333333, -4.187500);
            mITSMarker.setPosition(itsLatLng);
            mITSMarker.setRotation(bearing);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .zoom(mGoogleMap.getCameraPosition().zoom)
                    .bearing(0.0f)
                    .target(itsLatLng)
                    .build();

            this.mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(16.0f));

        BitmapDescriptor blueArrowBmpDesc =
                createBitmapDescriptorFromResource(this, R.drawable._334653);

        mITSMarker = createMarker(blueArrowBmpDesc);
        mCAMMarkers = new ArrayList<Marker>();

        BitmapDescriptor redArrowBmpDesc =
                createBitmapDescriptorFromResource(this, R.drawable.pointlocation);

        for (int i = 0; i < 50; i++) {
            mCAMMarkers.add(createMarker(redArrowBmpDesc));
        }
    }

    private void updateITSLabel(GpsLocation location) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String newLabelContent = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
                //mItsTextView.setText(newLabelContent);
            }
        });
    }

    private Marker createMarker(BitmapDescriptor icon) {
        final MarkerOptions option =
                new MarkerOptions()
                        .visible(false)
                        .position(new LatLng(0.0, 0.0));

        Marker marker = this.mGoogleMap.addMarker(option);

        if (marker != null) {
            marker.setIcon(icon);
        }

        return marker;
    }

    private static BitmapDescriptor createBitmapDescriptorFromResource(Context context, int resourceId) {
        Bitmap bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.getResources(), resourceId),
                100,
                100,
                false);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //Metodo para mostrar el estado
    private void setOnUIThread(TextView view, String text) {
        runOnUiThread(() -> view.setText(text));
    }

}