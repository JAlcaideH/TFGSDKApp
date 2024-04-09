package com.jahtfg.safecycle;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.jahtfg.safecycle.databinding.ActivitySettingsBinding;
import com.jahtfg.safecycle.utils.JavaMapUtils;
import com.jahtfg.safecycle.utils.Parameters;
import com.jahtfg.safecycle.utils.AppPreferences;
import com.vodafone.v2x.sdk.android.facade.enums.StationType;

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