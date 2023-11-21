package com.example.tfgsdkapp

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.vodafone.v2x.sdk.android.facade.models.GpsLocation
import com.vodafone.v2x.sdk.android.facade.records.cam.CAMRecord
import kotlin.math.pow

class Service {

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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

    fun estanCerca(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean {
        val distancia = calcularDistancia(lat1, lon1, lat2, lon2)
        return distancia <= 50 // Comprueba si la distancia es menor o igual a 100 metros
    }

}