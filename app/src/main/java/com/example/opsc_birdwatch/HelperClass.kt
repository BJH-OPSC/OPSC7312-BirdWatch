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


    data class Achievement(
        val id: String,
        val name: String,
        val description: String,
        val conditions: List<Condition>
    )

    data class Condition(
        val type: ConditionType,
        val target: String,
        val value: Any // Use Any to handle both Int and Double
    )

    enum class ConditionType {
        DISTANCE_TRAVELED,
        BIRDS_ADDED,
        MARKER_PLACED
    }

    object AchievementManager {
        private val unlockedAchievements = mutableListOf<Achievement>()

        // Define a list of achievements
        private val achievementList = listOf(
            Achievement(
                id = "distance_5km",
                name = "Novice Traveler",
                description = "Travel 5 kilometers",
                conditions = listOf(Condition(ConditionType.DISTANCE_TRAVELED, "", 5))
            ),
            Achievement(
                id = "birds_5",
                name = "Bird Spotter",
                description = "Add 5 birds to the observation list",
                conditions = listOf(Condition(ConditionType.BIRDS_ADDED, "", 5))
            ),
            Achievement(
                id = "marker_placed",
                name = "Navigator",
                description = "Place your first marker on the map",
                conditions = listOf(Condition(ConditionType.MARKER_PLACED, "", 1))
            )
            // Add more achievements as needed
        )

        fun trackDistanceTraveled(distance: Number) {
            checkAndUnlockAchievements(ConditionType.DISTANCE_TRAVELED, distance)
        }

        fun trackBirdsAdded(count: Int) {
            checkAndUnlockAchievements(ConditionType.BIRDS_ADDED, count)
        }

        fun trackMarkerPlaced() {
            checkAndUnlockAchievements(ConditionType.MARKER_PLACED, 1)
        }

        private fun checkAndUnlockAchievements(type: ConditionType, value: Number) {
            val relevantAchievements = achievementList.filter { it.conditions.any { it.type == type } }

            relevantAchievements.forEach { achievement ->
                achievement.conditions.filter { it.type == type }.forEach { condition ->
                    val conditionValue = when (condition.value) {
                        is Int -> (condition.value as Int).toDouble()
                        is Double -> condition.value as Double
                        else -> throw IllegalArgumentException("Unsupported condition value type")
                    }

                    val userValue = value.toDouble()

                    if (userValue >= conditionValue) {
                        unlockAchievement(achievement)
                    }
                }
            }
        }

        private fun unlockAchievement(achievement: Achievement) {
            if (!unlockedAchievements.contains(achievement)) {
                unlockedAchievements.add(achievement)
                // You can notify the user, update UI, or store the achievement.
            }
        }

        fun getUnlockedAchievements(): List<Achievement> {
            return unlockedAchievements.toList()
        }
    }

}