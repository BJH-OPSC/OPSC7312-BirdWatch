package com.example.opsc_birdwatch

object UserManager {
    private val users: HashMap<String, String> = HashMap()

    fun addUser(username: String, password: String) {
        users[username] = password
    }

    fun getUserPassword(username: String): String? {
        return users[username]
    }
}
