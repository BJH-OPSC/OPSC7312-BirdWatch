package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

        Log.d("HelperClass", "Achievement ID: $bird")
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
        //the number at value is the condition that must be met
        //for single time achievements (login, settings change) its easier to make their conditions = 1
        //then in the check just pass it a 1
        var achievementList = listOf(
            Achievement("Bronze-Travel", "Junior Traveler", "Achieve 5km of travel", listOf(Condition(ConditionType.DISTANCE_TRAVELED, 5.0))),
            Achievement("Silver-Travel", "Experienced Traveler", "Achieve 10km of travel", listOf(Condition(ConditionType.DISTANCE_TRAVELED, 10.0))),
            Achievement("Gold-Travel", "Pro Traveler", "Achieve 15km of travel", listOf(Condition(ConditionType.DISTANCE_TRAVELED, 15.0))),

            Achievement("Bronze-Birds", "Junior Observer", "Observe 5 birds", listOf(Condition(ConditionType.BIRDS_ADDED, 5))),
            Achievement("Silver-Birds", "Experienced Observer", "Observe 10 birds", listOf(Condition(ConditionType.BIRDS_ADDED, 10))),
            Achievement("Gold-Birds", "Pro Observer", "Observe 15 birds", listOf(Condition(ConditionType.BIRDS_ADDED, 15))),

            Achievement("marker_placed", "Explorer", "Place your first marker on the map", listOf(Condition(ConditionType.MARKER_PLACED, 1))),

            Achievement("settings_changed", "Mechanic", "Change the settings to your preferences", listOf(Condition(ConditionType.SETTINGS_CHANGE, 1))),

            Achievement("login_first", "Welcome Aboards", "Logged in for the first time", listOf(Condition(ConditionType.LOGGED_IN, 1))),
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

            //if im understanding correcting then this should save the stuff to the firebase database
            achievementFirestore(achievementId, achievement.isUnlocked)
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




        fun trackDistanceTraveled(distance: Double) {
            checkAndUnlockAchievements(ConditionType.DISTANCE_TRAVELED, distance)
            trackAchievements(ConditionType.DISTANCE_TRAVELED, distance)
        }

        fun trackBirdsAdded(count: Int, context: Context) {
            checkAndUnlockAchievements(ConditionType.BIRDS_ADDED, count)
            trackAchievements(ConditionType.BIRDS_ADDED, count)
            //Log.d("HelperClass", "Achievement ID: birdcount")
            showNotification(context, "Achievement Unlocked!", "You have tracked quite a few birds!")
        }

        fun trackMarkerPlaced() {
            checkAndUnlockAchievements(ConditionType.MARKER_PLACED, 1)
            trackAchievements(ConditionType.MARKER_PLACED, 1)
        }

        fun trackLoginFirst(context: Context) {
            checkAndUnlockAchievements(ConditionType.LOGGED_IN, 1)
            trackAchievements(ConditionType.LOGGED_IN, 1)
            //Log.d("HelperClass", "Achievement ID: login")
            showNotification(context, "Achievement Unlocked!", "You have logged in for the first time!")
        }

        fun trackSettingsChanged(context: Context) {
            checkAndUnlockAchievements(ConditionType.SETTINGS_CHANGE, 1)
            trackAchievements(ConditionType.SETTINGS_CHANGE, 1)
            //Log.d("HelperClass", "Achievement ID: settingschanged")
            showNotification(context, "Achievement Unlocked!", "You have changed your settings! Keep on watching those birds!")
        }



        private fun achievementFirestore(id: String, status: Boolean){
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            //Log.d("User", "user: ${currentUser}")

            if (userId != null) {

                val db = FirebaseFirestore.getInstance()

                for(achievement in HelperClass.AchievementManager.achievementList){

                    val achievementData = hashMapOf(
                        "id" to achievement.id,
                        "isUnlocked" to achievement.isUnlocked,
                        "user" to currentUser.uid
                    )
                    //Log.d("Achievements id:",achievement.id.toString())
                    db.collection("Achievements")
                        .add(achievementData)
                        .addOnSuccessListener { documentReference ->
                            // Document added successfully
                            //Log.d(MotionEffect.TAG, "data saved:success")

                            //------------------had to comment out the alertdialog cos it cant function outside of an activity

                            //val alertDialog = AlertDialog.Builder(this)
                            //alertDialog.setTitle("Successfully Saved")
                            //alertDialog.setMessage("Achievement Saved")
                            //alertDialog.setPositiveButton("OK") { dialog, _ ->
                                // when the user clicks OK
                            //    dialog.dismiss()
                            //}
                            //alertDialog.show()
                        }
                        .addOnFailureListener { e ->
                            // Handle errors
                            //Log.d(MotionEffect.TAG, e.message.toString())
                            //Log.d(MotionEffect.TAG, "data saved:failure")
                            //val alertDialog = AlertDialog.Builder(this)
                            //alertDialog.setTitle("unsuccessfully Saved")
                            //alertDialog.setMessage("Achievement Not Saved")
                            //alertDialog.setPositiveButton("OK") { dialog, _ ->
                                // when the user clicks OK
                            //    dialog.dismiss()
                            //}
                            //alertDialog.show()
                        }
                }

            }

        }

        //-------------------------------------------------------------------------------------------------old stuff
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


        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun showNotification(context: Context, title: String?, message: String?) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showNotification(title, message)
        }

    }

}