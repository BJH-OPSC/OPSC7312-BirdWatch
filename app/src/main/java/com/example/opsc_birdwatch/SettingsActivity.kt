package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var decisionSwitch: Switch
    private lateinit var maxDistanceEditText: EditText
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private var maxDistance = 20
    private lateinit var drawerLayout: DrawerLayout
    val db = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferencesManager = SharedPreferencesManager(applicationContext) // Initialize SharedPreferencesManager

        decisionSwitch = findViewById(R.id.decisionSwitch)
        maxDistanceEditText = findViewById(R.id.MaxDistanceEditText)
        var applyButton = findViewById<Button>(R.id.apply_button)
        val isImperialEnabled = sharedPreferencesManager.getUnit() // Use the SharedPreferencesManager
        val maxDistanceInt = maxDistanceEditText.text.toString()
        // Set the initial state of the switch
        decisionSwitch.isChecked = isImperialEnabled
        // Handle switch state changes
        var isImperial = false;
        decisionSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the user's preference when the switch state changes
            sharedPreferencesManager.setUnit(isChecked)
            isImperial= isChecked;
            //sharedPreferencesManager.setMaxDistance(maxDistance)
        }
        //checks if imperial
        var maxInt = 500
        if (sharedPreferencesManager.getUnit() == true) {
            maxInt = 310
        }


        maxDistanceEditText.setText(maxDistance.toString())
        applyButton.setOnClickListener {
            val maxDistanceString = maxDistanceEditText.text.toString()
            if (maxDistanceString.isNotBlank() && maxDistanceString.toInt() <= maxInt) {
                try {
                    settingsFirestore(isImperial,maxDistance)
                    maxDistance = maxDistanceString.toInt()
                    sharedPreferencesManager.setMaxDistance(maxDistance)
                    Toast.makeText(this, "Max distance saved: $maxDistance", Toast.LENGTH_SHORT).show()

                    //grant settings changed achievement
                    HelperClass.AchievementManager.trackSettingsChanged()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid max distance", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter max distance that's less than 500km/310mi", Toast.LENGTH_SHORT).show()
            }
            Log.d(TAG, "onCreate: MAX DISTANCE INT THING $maxDistanceInt")
            Log.d(TAG, "onCreate: MAX DISTANCE IS ${sharedPreferencesManager.getMaxDistance()}")
        }
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setCheckedItem(R.id.nav_settings)


    }
//----------------------------------------------------------------------------------------------------------\\
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val currentActivity = this::class.java

                //check whats the current activity, if not the main activity it will start the main activity
                if (currentActivity == SettingsActivity::class.java){
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

                if (currentActivity == SettingsActivity::class.java){
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
//------------------------------------------------------------------------------------------\\
    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun settingsFirestore(system: Boolean, maxDistance:Int){
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val settingsData = hashMapOf(
                "ImperialSystem" to system,
                "MaxDistance" to maxDistance,
                "user" to currentUser.uid
            )

            db.collection("Settings")
                .add(settingsData)
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
//------------------------------------------------End of File-----------------------------------------\\