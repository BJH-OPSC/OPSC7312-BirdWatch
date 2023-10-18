package com.example.opsc_birdwatch


class HelperClass {

    data class Bird(val name: String, val dateTime: String, val location: String)

    private val BirdMap: HashMap<String, Bird> = HashMap()

    fun addToList(usersName: String, name: String, dateTime: String, location: String){

        val bird = Bird(name, dateTime, location)
        BirdMap[usersName] = bird
    }
}