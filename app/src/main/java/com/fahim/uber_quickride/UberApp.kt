package com.fahim.uber_quickride

import android.app.Application
import com.fahim.uber_quickride.simulator.Simulator
import com.google.android.libraries.places.api.Places
import com.google.maps.GeoApiContext

class UberApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, getString(R.string.MAPS_API_KEY))
        Simulator.geoApiContext = GeoApiContext.Builder()
            .apiKey(getString(R.string.MAPS_API_KEY))
            .build()
    }
}