package com.example.opsc_birdwatch

import android.content.Context

class NotificationService(private val context: Context) {

    fun showNotification(){

    }
    companion object{
        const val CHANNEL_ID = "notif_channel"
    }
}