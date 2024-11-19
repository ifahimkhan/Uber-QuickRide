package com.fahim.uber_quickride

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fahim.uber_quickride.databinding.ActivityMapsBinding
import com.fahim.uber_quickride.simulator.WebSocket
import com.fahim.uber_quickride.simulator.WebSocketListener
import com.fahim.uber_quickride.utils.Constants
import com.fahim.uber_quickride.utils.MapUtils
import com.fahim.uber_quickride.utils.PermissionUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, WebSocketListener {
    companion object {
        private const val TAG = "MapsActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var webSocket: WebSocket
    private var currentLatLng: LatLng? = null
    private val nearbyCabMarkerList = arrayListOf<Marker>()
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initWebSocket()
    }

    override fun onStart() {
        super.onStart()
        if (currentLatLng == null) {
            when {
                PermissionUtils.isAccessFineLocationGranted(this) -> {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }

                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                }

                else -> {
                    PermissionUtils.requestAccessFineLocationPermission(
                        this,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        webSocket.disconnect()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    private fun initWebSocket() {
        webSocket = WebSocket(this)
        webSocket.connect()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onConnect() {
        Log.d(TAG, "onConnect")
    }

    override fun onDisconnect() {
        Log.d(TAG, "onDisconnect")
    }

    override fun onError(error: String) {
        Log.d(TAG, "onError error : $error")
    }

    override fun onMessage(data: String) {
        Log.d(TAG, "onMessage data : $data")
        runOnUiThread {
            val jsonObject = JSONObject(data)
            when (jsonObject.getString(Constants.TYPE)) {
                Constants.NEAR_BY_CABS -> {
                    handleOnMessageNearbyCabs(jsonObject)
                }
            }
        }
    }

    private fun handleOnMessageNearbyCabs(jsonObject: JSONObject) {
        nearbyCabMarkerList.clear()
        val jsonArray = jsonObject.getJSONArray(Constants.LOCATIONS)
        for (i in 0 until jsonArray.length()) {
            val lat = (jsonArray.get(i) as JSONObject).getDouble(Constants.LAT)
            val lng = (jsonArray.get(i) as JSONObject).getDouble(Constants.LNG)
            val latLng = LatLng(lat, lng)
            val nearbyCabMarker = addCarMarkerAndGet(latLng)
            nearbyCabMarkerList.add(nearbyCabMarker)
        }
    }

    private fun addCarMarkerAndGet(latLng: LatLng): Marker {
        return mMap.addMarker(
            MarkerOptions().position(latLng).flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this)))
        )!!
    }

    private fun enableMyLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
    }

    private fun setUpLocationListener() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        // for getting the current location update after every 2 seconds
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (currentLatLng == null) {
                    for (location in locationResult.locations) {
                        currentLatLng = LatLng(location.latitude, location.longitude)
                        enableMyLocationOnMap()
                        moveCamera(currentLatLng)
                        animateCamera(currentLatLng)
                        requestNearbyCabs()
                    }
                }
                // Few more things we can do here:
                // For example: Update the location of user on server
            }
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    private fun moveCamera(latLng: LatLng?) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng!!))
    }

    private fun animateCamera(latLng: LatLng?) {
        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(
                    latLng!!
                ).zoom(15.5f).build()
            )
        )
    }

    private fun requestNearbyCabs() {
        val jsonObject = JSONObject()
        jsonObject.put(Constants.TYPE, Constants.NEAR_BY_CABS)
        jsonObject.put(Constants.LAT, currentLatLng!!.latitude)
        jsonObject.put(Constants.LNG, currentLatLng!!.longitude)
        webSocket.sendMessage(jsonObject.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }

                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


}