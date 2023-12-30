package com.example.tfgsdkapp

import com.google.android.material.snackbar.Snackbar
import com.vodafone.v2x.sdk.android.facade.V2XSDK
import com.vodafone.v2x.sdk.android.facade.events.EventCamListChanged
import kotlin.math.pow
import com.google.maps.GeoApiContext
import com.google.maps.DirectionsApi
import com.google.maps.model.TravelMode

class Service {

    private fun calcularDistancia(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Double {
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

    private fun estanCerca(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Boolean {
        val distancia = calcularDistancia(lat1, lon1, lat2, lon2)
        println("Distancia actual: $distancia")
        return distancia <= 50 // Comprueba si la distancia es menor o igual a 100 metros
    }

    //Comparamos si la velocidad de otro dispositivo es mayor a la mia
    private fun compareVelocity(vel1: Float, vel2: Float): Boolean {
        return vel2 > vel1
    }


    fun compararPosiciones(eventCamListChanged: EventCamListChanged) : Boolean {
        val camRecords = eventCamListChanged.list
        val myRecord =
            camRecords.firstOrNull { it.stationID == V2XSDK.getInstance().sdkConfiguration.stationID }
        var atLeastOneNear = false;

        if (myRecord != null) {
            for (record in camRecords) {
                if (record != myRecord) {
                    if(compareVelocity(myRecord.speedInKmH,record.speedInKmH)) {
                        if (compararAngulo(myRecord.headingInDegree,record.headingInDegree)) {
                            if (estanCerca(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude)) {
                                if(calcularDistanciaCarretera(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude) < 50) {
                                    println("Velocidad actual: ${record.speedInKmH}")
                                    println("Distancia en carretera: ${calcularDistanciaCarretera(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude)}")
                                    atLeastOneNear = true
                                }
                            }
                        }
                    }
                }
            }
        }
        return atLeastOneNear
    }

    private fun compararAngulo(myAngulo: Float, otroAngulo: Float) : Boolean {
        val diff = (otroAngulo - myAngulo + 360) % 360
        val diffNormalized = if (diff > 180) diff - 360 else diff
        println("angulo actual: ${diffNormalized}")
        return diffNormalized in -100f..100f
    }

    private fun calcularDistanciaCarretera(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Float {
        val apiKey = "AIzaSyAP8GhkfWaCV2XWWt5sXGWzzlT5fx1uIHM"
        val context = GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()

        val directionsResult = DirectionsApi.newRequest(context)
            .mode(TravelMode.DRIVING)
            .origin("$lat1,$lon1")
            .destination("$lat2,$lon2")
            .await()

        val distanciaEnCarretera = directionsResult.routes[0].legs[0].distance.inMeters.toFloat()

        return distanciaEnCarretera
    }
}