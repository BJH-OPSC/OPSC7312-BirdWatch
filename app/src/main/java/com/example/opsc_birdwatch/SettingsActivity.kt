package com.example.opsc_birdwatch

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var decisionSwitch: Switch
    private lateinit var maxDistanceEditText: EditText
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    val applyButton = findViewById<Button>(R.id.apply_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferencesManager =
            SharedPreferencesManager(applicationContext) // Initialize SharedPreferencesManager

        //decisionSwitch = findViewById(R.id.decisionSwitch)
        //maxDistanceEditText = findViewById(R.id.MaxDistanceEditText)

        val isImperialEnabled =
            sharedPreferencesManager.getUnit() // Use the SharedPreferencesManager
        val maxDistance = maxDistanceEditText.text.toString().toFloat()

        // Set the initial state of the switch
        decisionSwitch.isChecked = isImperialEnabled

        // Handle switch state changes
        decisionSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the user's preference when the switch state changes
            sharedPreferencesManager.setUnit(isChecked)
            //sharedPreferencesManager.setMaxDistance(maxDistance)
        }
        applyButton.setOnClickListener {
            sharedPreferencesManager.setMaxDistance(maxDistance)

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
