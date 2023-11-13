package com.example.opsc_birdwatch


class HelperClass {

    data class Bird(val name: String, val dateTime: String, val location: String)

    public val BirdMap: HashMap<String, Bird> = HashMap()

    fun addToList(usersName: String, name: String, dateTime: String, location: String){

        val bird = Bird(name, dateTime, location)
        BirdMap[usersName] = bird
    }
  /*  fun fetchBirdData(UserID: String, holder: birdAdapter.ViewHolder, position: Int, onComplete: (List<BirdItem>) -> Unit) {
        // Reference to the Firestore collection
        val collectionRef = db.collection(collectionName)
        val userID = auth.currentUser

        // Query the collection based on the "birdname" field
        collectionRef.whereEqualTo("user", UserID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val birdItemList = mutableListOf<BirdItem>()
                for (doc in querySnapshot) {
                    // doc.data contains the document data
                    val birdName = doc.getString("BirdName")
                    val latitude = doc.getDouble("Latitude")
                    val longitude = doc.getDouble("Longitude")
                    val date = doc.getString("Date")
                    mCurrentLocation = Location(latitude,longitude)
                    addToList(userID.toString(),birdName.toString(),date.toString(),)
                    /* if (birdName != null && latitude != null && longitude != null) {
                         val birdItem = BirdItem(
                            // R.drawable.bird_image, // Replace with the appropriate image resource

                             //   birdName,
                            // "", // You can add a timestamp here if needed
                            // "Lat: $latitude, Long: $longitude"
                         )
                         birdItemList.add(birdItem)
                     }*/
                }
                onComplete(birdItemList)
            }
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot) {
                    // doc.data contains the document data
                    val data = doc.data
                    Log.d(MotionEffect.TAG, "fetchBirdData: success ")
                    // Handle the data as needed
                    // For example, you can populate a list of objects with this data
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                // This will be called if there is an issue with retrieving the data
                Log.d(MotionEffect.TAG, "fetchBirdData: failure "+ e.message.toString())

            }
    }*/

}