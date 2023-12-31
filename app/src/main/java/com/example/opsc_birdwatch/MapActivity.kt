package com.example.opsc_birdwatch

import kotlin.math.roundToInt
import android.Manifest
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
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
import com.google.maps.model.Unit
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

import android.view.MenuItem
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, OnPolylineClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    //declaring class variables
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var sharedPreferences: SharedPreferences
    private val API_KEY = "AIzaSyAHuVhTH57FC4TbT01iA0uhep_7M5RRX-o"
    private val ERROR_DIALOG_REQUEST = 1
    private val PERMISSIONS_REQUEST_ENABLE_GPS = 2
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private var mLocationPermissionGranted = false
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val BASE_URL = "https://api.ebird.org/v2/"
    private var mCurrentLocation: Location = Location("dummy_provider")
    private var mGeoApiContext: GeoApiContext? = null
    private var mPolyLinesData: ArrayList<PolylineData> = ArrayList()
    private var selectedDistance: Int = 20
    private var mSelectedMarker: Marker? = null
    private var mTripMarkers: ArrayList<Marker> = ArrayList()
    private var selectedUnits: Boolean = false
    private var cameraMovedToUserLocation = false
    private var lastHotspotLoadLocation: Location? = null
    private val hotspotReloadDistanceThreshold = 10000
    private var currentLocationCallback: LocationCallback? = null

    // Reference to a Firestore collection
    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "BirdObservations"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        //shared preferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferencesManager = SharedPreferencesManager(applicationContext)
       // selectedUnits=sharedPreferencesManager.getUnit()
        try {
            selectedDistance = sharedPreferencesManager.getMaxDistance()
            selectedUnits=sharedPreferencesManager.getUnit()

        } catch (e: Resources.NotFoundException) {
            selectedDistance =20
            selectedUnits = false
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val navigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navigationView.setOnNavigationItemSelectedListener(this)
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
        getDeviceLocation()
    }

    //function to get direction from current location to marker using DirectionsApi
    private fun calculateDirections(marker: Marker){
        Log.d(TAG, "calculateDirections: calculating directions.")

        val destination = com.google.maps.model.LatLng(
            marker.position.latitude,
            marker.position.longitude
        )
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(true)
        if(!selectedUnits){
            directions.units(Unit.METRIC)
        }else{
            directions.units(Unit.IMPERIAL)
        }

        directions.origin(
            com.google.maps.model.LatLng(
                mCurrentLocation.latitude,
                mCurrentLocation.longitude
            )
        )
        //Log.d(TAG, "calculateDirections: destination: $destination")
        directions.destination(destination)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {
                    addPolylinesToMap(result)
                }

                override fun onFailure(e: Throwable) {
                    //Log.e(TAG, "calculateDirections: Failed to get directions: " + e.message)
                }
            })
    }

    //function to remove trip markers of previously selected directions
    private fun removeTripMarkers(){
        for(marker in mTripMarkers){
            marker.remove()
        }
    }

    //function to re-enable the disabled hotspot marker
    private fun resetSelectedMarker(){
        if(mSelectedMarker != null){
            mSelectedMarker!!.isVisible = true
            mSelectedMarker = null
            removeTripMarkers()
        }
    }

    //function to draw the polylines for the direction's routes on the map
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
                //Log.d(TAG, "run: leg: " + route.legs[0].toString())
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

    //function to get nearby hotspots and store them in a List. Uses retrofit and eBird API
    private fun getHotspots(){
        //convert miles to kilometres if the user has selected imperial
        if(selectedUnits){
            selectedDistance = (selectedDistance*1.609).roundToInt()
        }

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
                //Log.d(TAG, "onFailure: $t")
            }
        })

        fetchBirdData { birdList ->
            for (birdItem in birdList) {
                Log.d(TAG, "getObsMarkers: MARKER BIRD LIST" + birdItem.birdName)
                val birdMarkerOptions = MarkerOptions()
                    .position(LatLng(birdItem.latitude, birdItem.longitude))
                    .title("Bird Observation")
                    .snippet("Last Observed: ${birdItem.date}\nBird Spotted: ${birdItem.birdName}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                mMap.addMarker(birdMarkerOptions)
            }
        }
    }

    //function to fetch the birds stored in firebase
    private fun fetchBirdData(onComplete: (List<BirdDataItem>) -> kotlin.Unit) {
        // Reference to the Firestore collection
        Log.d(TAG, "fetchBirdData: Called")
        try {

            val collectionRef = db.collection(collectionName)
            val user = FirebaseAuth.getInstance().currentUser
            val userID = user?.uid

            if (userID != null) {
                //Log.d("ContentValues", "fetchBirdData: userID $userID")
            } else {
                //Log.d("ContentValues", "fetchBirdData: User is not authenticated")
            }
            val query = collectionRef.whereEqualTo("user", userID)

            query.get()
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        val birdList = mutableListOf<BirdDataItem>()
                        //Log.d(TAG, "fetchBirdData: TASK SUCCESSFUL")
                        for (doc in task.result) {
                            val birdName = doc.getString("BirdName")
                            val latitude = doc.getDouble("Latitude")
                            val longitude = doc.getDouble("Longitude")
                            val date = doc.getString("Date")
                            //Log.d(TAG, "fetchBirdData: ${date} ${birdName} ${latitude} ${longitude}")
                            if (birdName != null && longitude != null && latitude != null && date != null) {

                                val birdDataItem = BirdDataItem(birdName, latitude, longitude, date)
                                birdList.add(birdDataItem)

                            }
                        }
                        onComplete(birdList)

                    } else {
                        //Log.d(MotionEffect.TAG, "fetchBirdData: failure " + task.exception?.message.toString())
                    }

                }
                .addOnFailureListener { e ->
                    //Log.d(MotionEffect.TAG, "fetchBirdData: failure " + e.message.toString())

                }

        } catch (e: Exception) {
            // Handle the exception here
            //Log.e("FirebaseFunctionError", "An error occurred: ${e.message}")
            //Log.v("YourTag", "An error occurred", e)

        }
    }

    //function to enable live location updates and retrieve the user's current location
    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).apply {
            setMinUpdateDistanceMeters(20F)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    mCurrentLocation = location
                    //Log.d(TAG, "New Location - Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    if(!cameraMovedToUserLocation){
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                        cameraMovedToUserLocation = true
                    }
                    if (lastHotspotLoadLocation == null ||
                        location.distanceTo(lastHotspotLoadLocation!!) >= hotspotReloadDistanceThreshold) {
                        // Load hotspot markers only if the user has traveled a significant distance
                        mMap.clear()
                        getHotspots()
                        lastHotspotLoadLocation = location
                    }
                }
            }
        }
        currentLocationCallback = locationCallback
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    //function to check whether google services are enabled
    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    //function to create an alert if the user does not have GPS services enabled
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

    //function to check if gps services are enabled
    private fun isMapsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    //function to request location permissions if the user does not have them enabled for the app
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
            return
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    //function to check if the user has google services available
    private fun isServicesOK(): Boolean {
        //Log.d(TAG, "isServicesOK: checking google services version")
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MapActivity)
        if (available == ConnectionResult.SUCCESS) {
            //Log.d(TAG, "isServicesOK: Google Play Services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //Log.d(TAG, "isServicesOK: an error occurred but we can fix it")
            val dialog: Dialog? = GoogleApiAvailability.getInstance()
                .getErrorDialog(this@MapActivity, available, ERROR_DIALOG_REQUEST)
            dialog?.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    //function to handle the result of the user interacting with the location permissions request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                    getDeviceLocation()
                }
            }
        }
    }

    // Handle the result of a GPS location service request.
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (mLocationPermissionGranted) {
                    return
                } else {
                    getLocationPermission()
                }
            }
        }
    }

    //function to handle the resumption of the map activity
    override fun onResume(){
        super.onResume()
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                return //GELOCATION HERE
            }else{
                getLocationPermission()
            }
        }
    }

    //function to handle the pausing of the map activity. stops location updates
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    //function to clear the locations from the FusedLocationClient and remove location updates
    private fun stopLocationUpdates() {
        mFusedLocationClient.flushLocations()
        currentLocationCallback?.let { mFusedLocationClient.removeLocationUpdates(it) }
    }

    //function to handle the user clicking on the infoWindow of a marker
    override fun onInfoWindowClick(marker: Marker) {
        //Log.d(TAG, "onInfoWindowClick: INFO WINDOW CLICKED")
        if (marker.title != null && marker.title!!.contains("Trip #")) {
            return
        }
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

    //function to handle when the user clicks on a polyline on a route
    override fun onPolylineClick(polyline: Polyline) {
        var index = 0
        for (polylineData in mPolyLinesData) {
            index++
            //Log.d(TAG, "onPolylineClick: toString: $polylineData")
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

    //function to zoom the map camera to show the calculated route clearly
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

    //function for the bottom navigation bar to go to appropriate activities
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_exit -> {
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.menu_home -> {
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.menu_list -> {
                finish()
                startActivity(Intent(this, ObservationsActivity::class.java))
            }
        }
        return true
    }
}
//data class for the firebase bird item
data class BirdDataItem(
    val birdName: String,
    val latitude: Double,
    val longitude: Double,
    val date: String
)
