package com.example.opsc_birdwatch

//Data class for a hotspot object to be used in eBird API call
data class Hotspot(
    val countryCode: String,
    val lat: Double,
    val latestObsDt: String,
    val lng: Double,
    val locId: String,
    val locName: String,
    val numSpeciesAllTime: Int,
    val subnational1Code: String
)
