package com.example.opsc_birdwatch
import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    fun isImperialSystemEnabled(): Boolean {
        return sharedPreferences.getBoolean("isImperialEnabled", false)
    }

    fun setUnit(isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isImperialEnabled", isEnabled)
        editor.apply()
    }
    // Set a float value
    fun setMaxDistance(defaultValue: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat("MaxDistance", defaultValue)
        editor.apply()
    }

    fun getUnit(): Boolean {
        return sharedPreferences.getBoolean("isImperialEnabled", false)
    }


    // Get a float value, with a default value if not found
    fun getMaxDistance(defaultValue: Float): Float {
        return sharedPreferences.getFloat("MaxDistance", defaultValue)
    }
}