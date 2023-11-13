package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.util.Log
import androidx.constraintlayout.helper.widget.MotionEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HelperClass {

    data class Bird(val name: String, val dateTime: String, val location: String)

    public val BirdMap: HashMap<String, Bird> = HashMap()

    fun addToList(usersName: String, name: String, dateTime: String, location: String){


        val bird = Bird(name, dateTime, location)
        //BirdMap[usersName] = bird
        BirdMap.put(usersName, bird)
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
        val conditions: List<Condition>,
        var isUnlocked: Boolean = false
    )

    data class Condition(
        val type: ConditionType,
        val value: Any
    )

    enum class ConditionType {
        DISTANCE_TRAVELED,
        BIRDS_ADDED,
        MARKER_PLACED,
        LOGGED_IN,
        SETTINGS_CHANGE
    }

    private data class AchievementProgress(
        var isUnlocked: Boolean = false
        // Add other progress-related properties if needed
    )

    val achievementList: MutableList<Achievement> = mutableListOf()

    object AchievementManager {
        var fetchedAchievements: List<Achievement> = emptyList()

        private val unlockedAchievements = mutableListOf<Achievement>()

        private val unlockedAchievementsMap = mutableMapOf<String, AchievementProgress>()


        // Define a list of achievements
        var achievementList = listOf(
            Achievement("Bronze-Travel", "Junior Traveler", "Achieve 5km of travel", listOf(Condition(ConditionType.DISTANCE_TRAVELED, 5.0))),
            Achievement("Silver-Travel", "Experienced Traveler", "Achieve 10km of travel", listOf(Condition(ConditionType.DISTANCE_TRAVELED, 10.0))),
            Achievement("Gold-Travel", "Pro Traveler", "Achieve 15km of travel", listOf(Condition(ConditionType.DISTANCE_TRAVELED, 15.0))),

            Achievement("Bronze-Birds", "Junior Observer", "Observe 5 birds", listOf(Condition(ConditionType.BIRDS_ADDED, 5))),
            Achievement("Silver-Birds", "Experienced Observer", "Observe 10 birds", listOf(Condition(ConditionType.BIRDS_ADDED, 10))),
            Achievement("Gold-Birds", "Pro Observer", "Observe 15 birds", listOf(Condition(ConditionType.BIRDS_ADDED, 15))),

            Achievement("marker_placed", "Explorer", "Place your first marker on the map", listOf(Condition(ConditionType.MARKER_PLACED, 1))),

            Achievement("settings_changed", "Mechanic", "Change the settings to your preferences", listOf(Condition(ConditionType.SETTINGS_CHANGE, 1))),
            // Add other achievements as needed
        )

        private fun trackAchievements(achievementId: String, type: ConditionType, userValue: Double) {
            val achievement = achievementList.find { it.id == achievementId } ?: return
            val achievementProgress = unlockedAchievementsMap.getOrPut(achievementId) { AchievementProgress() }

            if (!achievementProgress.isUnlocked && meetsCondition(achievement, type, userValue)) {
                newUnlockAchievement(achievementId, achievement)
                achievementProgress.isUnlocked = true
            }
        }

        private fun trackAchievements(type: ConditionType, userValue: Number) {
            achievementList.forEach { achievement ->
                if (!achievement.isUnlocked && meetsCondition(achievement, type, userValue.toDouble())) {
                    unlockAchievement(achievement)
                }
            }
        }

        private fun newUnlockAchievement(achievementId: String, achievement: Achievement) {
            achievement.isUnlocked = true
            // Logic to handle unlocking achievement
            println("Achievement Unlocked: $achievementId")
        }

        private fun meetsCondition(achievement: Achievement, type: ConditionType, userValue: Double): Boolean {
            val condition = achievement.conditions.find { it.type == type } ?: return false
            return userValue >= when (condition.value) {
                is Int -> (condition.value as Int).toDouble()
                is Double -> condition.value as Double
                else -> throw IllegalArgumentException("Unsupported condition value type")
            }
        }



        //-------------------------------------------------------------------------------------------------old stuff
        fun trackDistanceTraveled(distance: Double) {
            checkAndUnlockAchievements(ConditionType.DISTANCE_TRAVELED, distance)
            trackAchievements(ConditionType.DISTANCE_TRAVELED, distance)
        }

        fun trackBirdsAdded(count: Int) {
            checkAndUnlockAchievements(ConditionType.BIRDS_ADDED, count)
            trackAchievements(ConditionType.BIRDS_ADDED, count)
        }

        fun trackMarkerPlaced() {
            checkAndUnlockAchievements(ConditionType.MARKER_PLACED, 1)
            trackAchievements(ConditionType.MARKER_PLACED, 1)
        }

        fun trackLoginFirst() {
            checkAndUnlockAchievements(ConditionType.LOGGED_IN, 1)
            trackAchievements(ConditionType.LOGGED_IN, 1)
        }

        fun trackSettingsChanged() {
            checkAndUnlockAchievements(ConditionType.SETTINGS_CHANGE, 1)
            trackAchievements(ConditionType.SETTINGS_CHANGE, 1)
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
                achievement.isUnlocked = true
                println("Achievement Unlocked: ${achievement.name}")
            }
        }

        fun getUnlockedAchievements(): List<Achievement> {
            return unlockedAchievements.toList()
        }

        fun getAllAchievements(): List<Achievement> {
            return achievementList.toList()
        }




    }

}