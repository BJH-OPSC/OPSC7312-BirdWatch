package com.example.opsc_birdwatch

import com.google.android.gms.maps.model.Polyline
import com.google.maps.model.DirectionsLeg

//class to store data of polylines
class PolylineData(
    private var polyline: Polyline?,
    private var leg: DirectionsLeg?
) {

    fun getPolyline(): Polyline? {
        return polyline
    }

    fun setPolyline(polyline: Polyline?) {
        this.polyline = polyline
    }

    fun getLeg(): DirectionsLeg? {
        return leg
    }

    fun setLeg(leg: DirectionsLeg?) {
        this.leg = leg
    }

    override fun toString(): String {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}'
    }
}
