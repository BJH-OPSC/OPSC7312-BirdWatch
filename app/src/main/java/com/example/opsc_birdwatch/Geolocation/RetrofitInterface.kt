package com.example.opsc_birdwatch

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {
    @GET("ref/hotspot/geo")
    fun getHotspots(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("dist") distance: Int,
        @Query("fmt") format: String = "json"
    ): Call<List<Hotspot>>
}