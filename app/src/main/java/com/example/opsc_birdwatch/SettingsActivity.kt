package com.example.opsc_birdwatch

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var decisionSwitch: Switch
    private lateinit var maxDistanceEditText: EditText
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferencesManager = SharedPreferencesManager(applicationContext) // Initialize SharedPreferencesManager

        decisionSwitch = findViewById(R.id.decisionSwitch)
        maxDistanceEditText = findViewById(R.id.MaxDistanceEditText)

        val isImperialEnabled = sharedPreferencesManager.getUnit() // Use the SharedPreferencesManager
        val maxDistanceFloat = maxDistanceEditText.text.toString()
        val maxDistance = if (maxDistanceFloat.isNotBlank()) {
            try {
                maxDistanceFloat.toFloat()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid max distance", Toast.LENGTH_SHORT).show()

                20.0f
            }
        } else {
            Toast.makeText(this, "Please enter max distance", Toast.LENGTH_SHORT).show()
            20.0f
        }
        // Set the initial state of the switch
        decisionSwitch.isChecked = isImperialEnabled

        // Handle switch state changes
        decisionSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the user's preference when the switch state changes
            sharedPreferencesManager.setUnit(isChecked)
            sharedPreferencesManager.setMaxDistance(maxDistance)
        }

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }
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

}
//------------------------------------------------End of File-----------------------------------------\\