package com.jahtfg.safecycle

import com.vodafone.v2x.sdk.android.facade.V2XSDK
import com.vodafone.v2x.sdk.android.facade.events.EventCamListChanged
import kotlin.math.pow
import com.google.maps.GeoApiContext
import com.google.maps.DirectionsApi
import com.google.maps.model.TravelMode
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.vodafone.v2x.sdk.android.core.messages.cpm_pdu_descriptions.CpmManagementContainer._stationType
import retrofit2.Response

class Service {

    private var contador = 0

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

    private fun estanCerca(lat1: Float, lon1: Float, lat2: Float, lon2: Float, station: Long): Boolean {
        val distancia = calcularDistancia(lat1, lon1, lat2, lon2)
        if(distancia > 120){
            return false
        }
        if(listaDistancias.size <= 1) {
            contador ++
            listaDistancias.add(stationConDist(station,distancia))
            return false
        } else {
            if (contador % 2 == 0) {
                val segundoElemento = listaDistancias.removeAt(1)
                listaDistancias[0] = segundoElemento
                listaDistancias.add(stationConDist(station, distancia))
            }
            contador++
        }
        if ((listaDistancias.size == 2) && (!(listaDistancias.get(0).station == listaDistancias.get(1).station) || !(listaDistancias.get(0).dist > listaDistancias.get(1).dist)) ) {
            return false
        }
        println("Distancia actual: $distancia")
        return true
    }

    private fun compararVelocidad(vel1: Float, vel2: Float): Boolean {
        return vel2 > 20.0 + vel1
    }

    private fun estacionNoValida(stationTypeInput: Int): Boolean {

        val estacionesValidas = setOf(_stationType.passengerCar, _stationType.bus, _stationType.lightTruck,
            _stationType.heavyTruck,_stationType.trailer,_stationType.specialVehicles)

        return !estacionesValidas.contains(stationTypeInput)
    }

    data class stationConDist(val station: Long, val dist: Double)
    val listaDistancias = mutableListOf<stationConDist>()

    fun compararPosiciones(eventCamListChanged: EventCamListChanged) : Int {
        val startTime = System.currentTimeMillis()
        val camRecords = eventCamListChanged.list
        val myRecord =
            camRecords.firstOrNull { it.stationID == V2XSDK.getInstance().sdkConfiguration.stationID }
        var atLeastOneNear = 0;

        if (myRecord != null) {
            for (record in camRecords) {
                if(estacionNoValida(record.stationType)) {
                    continue
                }
                if (record != myRecord) {
                    if(compararVelocidad(myRecord.speedInKmH,record.speedInKmH)) {
                        val direccion = compararAnguloDir(myRecord.headingInDegree,record.headingInDegree)
                        if (direccion != 0) {
                            if (estanCerca(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude,record.stationID)) {
                                /*if(calcularDistanciaCarretera(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude) < 50) {
                                    println("Velocidad actual: ${record.speedInKmH}")
                                    atLeastOneNear = direccion
                                }*/
                                if(calculateDistanceMapBox(myRecord.latitude, myRecord.longitude, record.latitude, record.longitude) < 120) {
                                    println("Velocidad actual: ${record.speedInKmH}")
                                    atLeastOneNear = direccion
                                }
                                //atLeastOneNear = direccion
                                val endTime = System.currentTimeMillis()
                                val elapsedTime = endTime - startTime
                                println("El proceso tardó $elapsedTime milisegundos.")
                            } else {
                                continue
                            }
                        }
                    }
                }
            }
        }
        return atLeastOneNear
    }

    private fun compararAnguloDir(myAngulo: Float, otroAngulo: Float) : Int {
        var diff = otroAngulo - myAngulo
        if (diff < 0) {
            diff += 360
        }
        return if(diff <= 45) {
            1 // incide desde atras
        } else if (diff <= 135) {
            2 //incide desde izquierda
        } else if (diff <= 225) {
            0 //incide desde alante, se puede cerrar mas el angulo desde atras para abrir los laterales
        } else if (diff <= 315) {
            3 //incide desde derecha
        } else if (diff <= 360) {
            1 //incide desde atras
        } else {
            0
        }
    }

    private fun calculateDistanceMapBox(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Float {

        var responseDist: Float = 0F
        val client = MapboxDirections.builder()
            .origin(Point.fromLngLat(lon2.toDouble(),lat2.toDouble()))
            .destination(Point.fromLngLat(lon1.toDouble(),lat1.toDouble()))
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(BuildConfig.apikeyMapBox)
            .build()

        val response: Response<DirectionsResponse> = client.executeCall()
        return if(response != null) {
            responseDist = response.body()?.routes()?.firstOrNull()?.distance()?.toFloat() ?: 1000F
            println("Distanc actual MapBox: ${responseDist}")
            responseDist
        } else {
            1000F
        }
    }

    private fun calcularDistanciaCarretera(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Float {

        val context = GeoApiContext.Builder()
            .apiKey(BuildConfig.apikeyGoogle)
            .build()

        val directionsResult = DirectionsApi.newRequest(context)
            .mode(TravelMode.DRIVING)
            .origin("$lat2,$lon2")
            .destination("$lat1,$lon1")
            .await()

        val distanciaEnCarretera = directionsResult.routes[0].legs[0].distance.inMeters.toFloat()

        println("Distanc actual GoogleMaps: ${distanciaEnCarretera}")
        return distanciaEnCarretera
    }
}