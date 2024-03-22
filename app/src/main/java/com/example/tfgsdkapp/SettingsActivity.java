package com.example.tfgsdkapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfgsdkapp.databinding.ActivitySettingsBinding;
import com.example.tfgsdkapp.utils.JavaMapUtils;
import com.example.tfgsdkapp.utils.Parameters;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.vodafone.v2x.sdk.android.facade.enums.StationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private Parameters parameters;
    private Map<String, StationType> stationTypeMap;
    private ArrayAdapter<String> stationTypeSpinnerAdapter;
    private String preSavedStationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        parameters = Parameters.getInstance(this);
        setContentView(binding.getRoot());
        initTypeMap();
        setupStationTypeSpinner();
        setupPreloadedValues();
        setupButtons();
    }

    private void initTypeMap() {
        stationTypeMap = new HashMap<>();
        stationTypeMap.put("Peatón", StationType.PEDESTRIAN);
        stationTypeMap.put("Ciclista", StationType.CYCLIST);
        stationTypeMap.put("Ciclomotor", StationType.MOPED);
        stationTypeMap.put("Motocicleta", StationType.MOTORCYCLE);
        stationTypeMap.put("Coche", StationType.PASSENGER_CAR);
        stationTypeMap.put("Autobús", StationType.BUS);
        stationTypeMap.put("Camión ligero", StationType.LIGHT_TRUCK);
        stationTypeMap.put("Camión pesado", StationType.HEAVY_TRUCK);
        stationTypeMap.put("Camión tráiler", StationType.TRAILER);
        stationTypeMap.put("Vehiculos especiales", StationType.SPECIAL_VEHICLES);
        stationTypeMap.put("Tranvía", StationType.TRAM);
        stationTypeMap.put("Sistema RSU", StationType.ROAD_SIDE_UNITS);
        stationTypeMap.put("Animal", StationType.ANIMAL);
        stationTypeMap.put("Desconocido", StationType.UNKNOWN);
    }

    private void setupStationTypeSpinner() {
        stationTypeSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, new ArrayList<>(stationTypeMap.keySet()));
        binding.spStationType.setAdapter(stationTypeSpinnerAdapter);
    }

    private void setupPreloadedValues() {
        StationType stationType = parameters.getStationType();
        preSavedStationType = JavaMapUtils.getKeyByValue(stationTypeMap, stationType);
        if (preSavedStationType == null) {
            preSavedStationType = getString(R.string.passenger_car);
        }
        int preSavedStationTypePosition = stationTypeSpinnerAdapter.getPosition(preSavedStationType);
        binding.spStationType.post(() -> binding.spStationType.setSelection(preSavedStationTypePosition));
    }

    private void setupButtons() {
        binding.btCancel.setOnClickListener(v -> SettingsActivity.this.finish());

        binding.btApply.setOnClickListener(v -> {
            String inputStationType = binding.spStationType.getSelectedItem().toString().trim();
            if (!preSavedStationType.equals(inputStationType)) {
                boolean isStationTypeSet = parameters.setStationType(Objects.requireNonNull(stationTypeMap.get(inputStationType)));
                if (isStationTypeSet) {
                    ProcessPhoenix.triggerRebirth(SettingsActivity.this);
                }
            } else {
                finish();
            }
        });

    }

}