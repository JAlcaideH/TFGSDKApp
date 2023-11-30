package com.example.tfgsdkapp

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.vodafone.v2x.sdk.android.facade.V2XSDK
import com.vodafone.v2x.sdk.android.facade.events.EventCamListChanged
import com.vodafone.v2x.sdk.android.facade.models.GpsLocation
import com.vodafone.v2x.sdk.android.facade.records.cam.CAMRecord
import kotlin.math.pow

class Service {
    var estaMostrando = false
    var mostrarMensaje: Snackbar? = null

    fun calcularDistancia(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Double {
        val radioTierra = 6371e3 // Radio de la Tierra en metros
        val radLat1 = lat1 * Math.PI / 180 // Convertir grados a radianes
        val radLat2 = lat2 * Math.PI / 180
        val deltaLat = (lat2 - lat1) * Math.PI / 180
        val deltaLon = (lon2 - lon1) * Math.PI / 180

        val a = Math.sin(deltaLat / 2).pow(2.0) + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(
            deltaLon / 2
        ).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return radioTierra * c // Devuelve la distancia en metros
    }

    fun estanCerca(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Boolean {
        val distancia = calcularDistancia(lat1, lon1, lat2, lon2)
        return distancia <= 50 // Comprueba si la distancia es menor o igual a 100 metros
    }


    fun compararPosiciones(eventCamListChanged: EventCamListChanged) : Boolean {
        val camRecords = eventCamListChanged.list
        val myRecord =
            camRecords.firstOrNull { it.stationID == V2XSDK.getInstance().sdkConfiguration.stationID }
        var atLeastOneNear = false;

        if (myRecord != null) {
            for (record in camRecords) {
                if (record != myRecord) {
                    if (estanCerca(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude)) {
                        atLeastOneNear = true
                    }
                }
            }
        }
        return atLeastOneNear
    }
}