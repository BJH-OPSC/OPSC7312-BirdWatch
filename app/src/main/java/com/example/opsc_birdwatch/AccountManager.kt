package com.example.opsc_birdwatch
import android.content.Context
import android.content.SharedPreferences
object AccountManager {
    private val Account: HashMap<String, String> = HashMap()

    val MeasurementUnit: Boolean = true
    val MaxDistance = 0;
    fun addUser(username: String, password: String) {
        Account[username] = password
    }

    fun getUserPassword(username: String): String? {
        return Account[username]
    }

    //---------------------------------------------------------------------------------\\


}
