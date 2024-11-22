package com.fahim.uber_quickride.ui.maps

import android.util.Log
import com.fahim.uber_quickride.data.network.NetworkService
import com.fahim.uber_quickride.simulator.WebSocket
import com.fahim.uber_quickride.simulator.WebSocketListener
import com.fahim.uber_quickride.utils.Constants
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class MapsPresenter(private val networkService: NetworkService) : WebSocketListener {
    companion object {
        private const val TAG = "MapsPresenter"
    }

    private var view: MapsView? = null
    private lateinit var webSocket: WebSocket

    fun onAttach(view: MapsView?) {
        this.view = view
        webSocket = networkService.createWebSocket(this)
        webSocket.connect()
    }

    fun onDetach() {
        webSocket.disconnect()
        view = null
    }

    fun requestNearbyCabs(latLng: LatLng) {
        val jsonObject = JSONObject()
        jsonObject.put(Constants.TYPE, Constants.NEAR_BY_CABS)
        jsonObject.put(Constants.LAT, latLng.latitude)
        jsonObject.put(Constants.LNG, latLng.longitude)
        webSocket.sendMessage(jsonObject.toString())
    }

    private fun handleOnMessageNearbyCabs(jsonObject: JSONObject) {
        val nearbyCabLocations = arrayListOf<LatLng>()
        val jsonArray = jsonObject.getJSONArray(Constants.LOCATIONS)
        for (i in 0 until jsonArray.length()) {
            val lat = (jsonArray.get(i) as JSONObject).getDouble(Constants.LAT)
            val lng = (jsonArray.get(i) as JSONObject).getDouble(Constants.LNG)
            val latLng = LatLng(lat, lng)
            nearbyCabLocations.add(latLng)
        }
        view?.showNearbyCabs(nearbyCabLocations)
    }

    override fun onConnect() {
        Log.e(TAG, "onConnect:")
    }

    override fun onDisconnect() {
        Log.e(TAG, "onDisconnect:")
    }

    override fun onError(error: String) {
        Log.e(TAG, "onError: $error")
    }

    override fun onMessage(data: String) {

        Log.e(TAG, "onMessage: $data")
        val jsonObject = JSONObject(data)
        when(jsonObject.getString(Constants.TYPE)){
            Constants.NEAR_BY_CABS ->{
                handleOnMessageNearbyCabs(jsonObject)
            }
        }
    }

}