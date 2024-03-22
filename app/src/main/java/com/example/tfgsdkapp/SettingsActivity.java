package com.example.tfgsdkapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfgsdkapp.databinding.ActivitySettingsBinding;
import com.example.tfgsdkapp.utils.AppPreferences;
import com.example.tfgsdkapp.utils.JavaMapUtils;
import com.example.tfgsdkapp.utils.Parameters;
import com.vodafone.v2x.sdk.android.facade.enums.StationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private Parameters parameters;
    private Map<String, StationType> stationTypeMap;
    private ArrayAdapter<String> stationTypeSpinnerAdapter;
    private String preSavedStationType;

    public EditText editText;

    private String apiKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        parameters = Parameters.getInstance(this);
        setContentView(binding.getRoot());
        initTypeMap();
        setupPreloadedValues();
        setupButtons();
    }

    private void initTypeMap() {
        stationTypeMap = new HashMap<>();
        stationTypeMap.put(getString(R.string.pedestrian), StationType.PEDESTRIAN);
        stationTypeMap.put(getString(R.string.cyclist), StationType.CYCLIST);
        stationTypeMap.put(getString(R.string.moped), StationType.MOPED);
        stationTypeMap.put(getString(R.string.motorcycle), StationType.MOTORCYCLE);
        stationTypeMap.put(getString(R.string.passenger_car), StationType.PASSENGER_CAR);
        stationTypeMap.put(getString(R.string.bus), StationType.BUS);
        stationTypeMap.put(getString(R.string.light_truck), StationType.LIGHT_TRUCK);
        stationTypeMap.put(getString(R.string.heavy_truck), StationType.HEAVY_TRUCK);
        stationTypeMap.put(getString(R.string.trailer), StationType.TRAILER);
        stationTypeMap.put(getString(R.string.special_vehicles), StationType.SPECIAL_VEHICLES);
        stationTypeMap.put(getString(R.string.tram), StationType.TRAM);
        stationTypeMap.put(getString(R.string.road_side_units), StationType.ROAD_SIDE_UNITS);
        stationTypeMap.put(getString(R.string.animal), StationType.ANIMAL);
        stationTypeMap.put(getString(R.string.unknown), StationType.UNKNOWN);
    }

    private void setupPreloadedValues() {
        StationType stationType = parameters.getStationType();
        preSavedStationType = JavaMapUtils.getKeyByValue(stationTypeMap, stationType);
        if (preSavedStationType == null) {
            preSavedStationType = getString(R.string.cyclist);
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    private void setupButtons() {
        binding.btCancel.setOnClickListener(v -> SettingsActivity.this.finish());

        binding.btApply.setOnClickListener(v -> {
            editText = findViewById(R.id.editText);
            apiKey = editText.getText().toString();
            AppPreferences.INSTANCE.setApiKeyMapBox(apiKey);
            SettingsActivity.this.finish();
        });

    }

}