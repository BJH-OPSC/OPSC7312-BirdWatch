package com.example.opsc_birdwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.constraintlayout.helper.widget.MotionEffect
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ObservationsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var helperClass: HelperClass

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observations)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListObservationsFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_list)
        }

        getLastKnownLocation()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val currentActivity = this::class.java

                //check whats the current activity, if not the main activity it will start the main activity
                if (currentActivity == ObservationsActivity::class.java){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                }
            }

            R.id.nav_map -> {startActivity(Intent(this, MapActivity::class.java))}

            R.id.nav_list -> {startActivity(Intent(this, ObservationsActivity::class.java))}

            R.id.nav_settings -> {startActivity(Intent(this, SettingsActivity::class.java))}

            R.id.nav_about -> {
                val currentActivity = this::class.java

                if (currentActivity == ObservationsActivity::class.java){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AboutFragment()).commit()
                }
            }
            R.id.nav_login -> {startActivity(Intent(this, SignInActivity::class.java))}

            R.id.nav_logout -> Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //onBackPressed function will determine what to do when the back button on the phone is pressed
    //checks if nav drawer is open, if yes then close it
    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    //method to check if user is logged in
    //if not sends them to the log in page
    //do not want unlogged users to be able to save
    fun isUserLogged(userName: String) : Boolean{

        return true
    }

    private fun getLastKnownLocation() {
        Log.d(ContentValues.TAG, "getLastKnownLocation: called.")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Permission is granted, proceed to get the last known location
            val fragment = ListObservationsFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment_container, fragment, "ListObservationsFragment")
            transaction.commit()
            mFusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(ContentValues.TAG, "New Location - Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                        fragment.getLocation(location)
                        Log.d(ContentValues.TAG, "getLastKnownLocation: PASSED TO FRAGMENT")
                    } else {
                        Log.e(ContentValues.TAG, "Last known location is null.")
                    }
                }
        }
    }
    //-----------------------------------------------------------------------------------------\\
    private fun observationsFirestore(BirdName: Boolean, Location:Double){
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val noteData = hashMapOf(
                "BirdName" to BirdName,
                "Location" to Location,
                "user" to currentUser.uid
            )

            db.collection("BirdObservations")
                .add(noteData)
                .addOnSuccessListener { documentReference ->
                    // Document added successfully
                    Log.d(MotionEffect.TAG, "data saved:success")
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Successfully Saved")
                    alertDialog.setMessage("Settings Saved")
                    alertDialog.setPositiveButton("OK") { dialog, _ ->
                        // when the user clicks OK
                        dialog.dismiss()
                        finish()
                    }
                    alertDialog.show()
                }
                .addOnFailureListener { e ->
                    // Handle errors
                    Log.d(MotionEffect.TAG, e.message.toString())
                    Log.d(MotionEffect.TAG, "data saved:failure")
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("unsuccessfully Saved")
                    alertDialog.setMessage("Settings Not Saved")
                    alertDialog.setPositiveButton("OK") { dialog, _ ->
                        // when the user clicks OK
                        dialog.dismiss()
                        finish()
                    }
                    alertDialog.show()
                }
        }

    }

}
//---------------------------------------------End of File--------------------------------------------------\\