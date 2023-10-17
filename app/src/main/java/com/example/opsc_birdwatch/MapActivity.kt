package com.example.opsc_birdwatch

import android.Manifest
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.opsc_birdwatch.databinding.ActivityMapBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, OnPolylineClickListener {
    // *********CAN ADD FUNCTION TO RESET MAP SO THAT POLYLINES ARE NO LONGER VISIBLE, CURRENTLY HAVE TO CLICK ON A NEW LOCATION AND GET DIRECTIONS TO CLEAR PREVIOUS POLYLINES***************
    private val API_KEY = "AIzaSyAHuVhTH57FC4TbT01iA0uhep_7M5RRX-o"
    private val ERROR_DIALOG_REQUEST = 1
    private val PERMISSIONS_REQUEST_ENABLE_GPS = 2
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private var mLocationPermissionGranted = false
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    val BASE_URL = "https://api.ebird.org/v2/"
    private var mCurrentLocation: Location = Location("dummy_provider")
    private var mGeoApiContext: GeoApiContext? = null
    private var mPolyLinesData: ArrayList<PolylineData> = ArrayList()
    private var selectedDistance: Int = 20
    var mSelectedMarker: Marker? = null
    var mTripMarkers: ArrayList<Marker> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        val customInfoWindowAdapter = CustomInfoWindowAdapter(this)
        mMap.setInfoWindowAdapter(customInfoWindowAdapter)
        if (mGeoApiContext == null){
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build()
        }
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnPolylineClickListener(this)
    }

    private fun calculateDirections(marker: Marker){
        Log.d(TAG, "calculateDirections: calculating directions.")

        val destination = com.google.maps.model.LatLng(
            marker.position.latitude,
            marker.position.longitude
        )
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(true)
        directions.origin(
            com.google.maps.model.LatLng(
                mCurrentLocation.latitude,
                mCurrentLocation.longitude
            )
        )
        Log.d(TAG, "calculateDirections: destination: $destination")
        directions.destination(destination)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {
                    Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString())
                    Log.d(
                        TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration
                    )
                    Log.d(
                        TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance
                    )
                    Log.d(
                        TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString()
                    )
                    addPolylinesToMap(result)
                }

                override fun onFailure(e: Throwable) {
                    Log.e(TAG, "calculateDirections: Failed to get directions: " + e.message)
                }
            })
    }

    private fun removeTripMarkers(){
        for(marker in mTripMarkers){
            marker.remove()
        }
    }

    private fun resetSelectedMarker(){
        if(mSelectedMarker != null){
            mSelectedMarker!!.isVisible = true
            mSelectedMarker = null
            removeTripMarkers()
        }
    }

    private fun addPolylinesToMap(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, "run: result routes: " + result.routes.size)
            if (mPolyLinesData.size > 0) {
                for (polylineData in mPolyLinesData) {
                    polylineData.getPolyline()!!.remove()
                }
                mPolyLinesData.clear()
                mPolyLinesData = ArrayList()
            }
            var duration = 9999999
            for (route in result.routes) {
                Log.d(TAG, "run: leg: " + route.legs[0].toString())
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val newDecodedPath: MutableList<LatLng> = ArrayList()

                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) {
                    newDecodedPath.add(
                        LatLng(
                            latLng.lat,
                            latLng.lng
                        )
                    )
                }
                val polyline: Polyline =
                    mMap.addPolyline(PolylineOptions().addAll(newDecodedPath).width(20F))
                polyline.color = ContextCompat.getColor(this, R.color.gray)
                polyline.isClickable = true
                mPolyLinesData.add(PolylineData(polyline, route.legs[0]))

                val tempDuration = route.legs[0].duration.inSeconds
                if (tempDuration < duration) {
                    duration = tempDuration.toInt()
                    onPolylineClick(polyline)
                    zoomRoute(polyline.points)
                }
                mSelectedMarker?.isVisible = false
            }
        }
    }

    private fun getHotspots(){
        Log.d(TAG, "getHotspots: Called GETHOTSPOTS")
        Log.d(TAG, "mCurrentLocation latidude ${mCurrentLocation.latitude}")
        Log.d(TAG, "mCurrentLocation longidude ${mCurrentLocation.longitude}")

        // Use the user's location in the Retrofit request
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(RetrofitInterface::class.java)

        val retrofitData = retrofitBuilder.getHotspots(
            latitude = mCurrentLocation.latitude,
            longitude = mCurrentLocation.longitude,
            distance = selectedDistance
        )

        retrofitData.enqueue(object : Callback<List<Hotspot>?> {
            override fun onResponse(
                call: Call<List<Hotspot>?>,
                response: Response<List<Hotspot>?>
            ) {
                val responseBody = response.body()!!

                for (hotspot in responseBody) {
                    val hotspotLocation = LatLng(hotspot.lat, hotspot.lng)
                    val hotspotMarkerOptions = MarkerOptions()
                        .position(hotspotLocation)
                        .title(hotspot.locName)
                        .snippet("Last Observed: ${hotspot.latestObsDt}\nSpecies Spotted: ${hotspot.numSpeciesAllTime}")
                    mMap.addMarker(hotspotMarkerOptions)
                }
            }

            override fun onFailure(call: Call<List<Hotspot>?>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }

    private fun getLastKnownLocation(){
        Log.d(TAG, "getLastKnownLocation: called.")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    mCurrentLocation = location
                    Log.d(TAG, "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                    getHotspots()
                } else {
                    Log.e(TAG, "Last known location is null.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting last known location: ${e.message}")
            }
    }

    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun buildAlertMessageNoGps() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("This application requires GPS to work properly, do you want to enable it?")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton("Yes") { dialog, id ->
            val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
        }

        val alert = alertDialogBuilder.create()
        alert.show()
    }

    private fun isMapsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
            getLastKnownLocation()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun isServicesOK(): Boolean {
        Log.d(TAG, "isServicesOK: checking google services version")
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MapActivity)
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it")
            val dialog: Dialog? = GoogleApiAvailability.getInstance()
                .getErrorDialog(this@MapActivity, available, ERROR_DIALOG_REQUEST)
            dialog?.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (mLocationPermissionGranted) {
                    getLastKnownLocation()
                } else {
                    getLocationPermission()
                }
            }
        }
    }

    override fun onResume(){
        super.onResume()
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getLastKnownLocation()
            }else{
                getLocationPermission()
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        Log.d(TAG, "onInfoWindowClick: INFO WINDOW CLICKED")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Directions to ${marker.title}")
            .setMessage("Do you want to calculate directions to this hotspot?")
            .setPositiveButton("Yes") { _, _ ->
                calculateDirections(marker)
                resetSelectedMarker()
                mSelectedMarker = marker
            }
            .setNegativeButton("No") { _, _ ->
            }
        builder.show()
    }

    override fun onPolylineClick(polyline: Polyline) {
        var index = 0
        for (polylineData in mPolyLinesData) {
            index++
            Log.d(TAG, "onPolylineClick: toString: $polylineData")
            if (polyline.id == polylineData.getPolyline()!!.id) {
                polylineData.getPolyline()!!.color =
                    ContextCompat.getColor(this, R.color.blue)
                polylineData.getPolyline()!!.zIndex = 1f

                val endLocation = LatLng(
                    polylineData.getLeg()!!.endLocation.lat,
                    polylineData.getLeg()!!.endLocation.lng
                )

                val markerOptions = MarkerOptions()
                    .position(endLocation)
                    .title("Trip #$index")
                    .snippet("Duration: ${polylineData.getLeg()!!.duration}\nDistance: ${polylineData.getLeg()!!.distance}")
                val marker = mMap.addMarker(markerOptions)
                marker?.showInfoWindow()
                if (marker != null) {
                    mTripMarkers.add(marker)
                }
            } else {
                polylineData.getPolyline()!!.color =
                    ContextCompat.getColor(this, R.color.gray)
                polylineData.getPolyline()!!.zIndex = 0f
            }
        }
    }

    private fun zoomRoute(lstLatLngRoute: List<LatLng?>?) {
        if (lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(latLngPoint!!)
        val routePadding = 120
        val latLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
            600,
            null
        )
    }
}