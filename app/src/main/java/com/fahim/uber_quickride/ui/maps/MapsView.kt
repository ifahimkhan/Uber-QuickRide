package com.fahim.uber_quickride.ui.maps

import com.google.android.gms.maps.model.LatLng


interface MapsView {

    fun showNearbyCabs(latLngList: List<LatLng>)

}