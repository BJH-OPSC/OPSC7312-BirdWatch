package com.example.opsc_birdwatch

object AccountManager {
    private val Account: HashMap<String, String> = HashMap()

    fun addUser(username: String, password: String) {
        Account[username] = password
    }

    fun getUserPassword(username: String): String? {
        return Account[username]
    }
}
