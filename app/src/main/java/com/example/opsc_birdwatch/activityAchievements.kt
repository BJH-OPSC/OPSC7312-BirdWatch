package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc_birdwatch.HelperClass.Achievement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.grpc.LoadBalancer.Helper

class activityAchievements : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AchievementAdapter

    var num1: Int = 0
    var num2: Double = 0.0
    val num3: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val achievements = /*helperClass.AchievementManager.getUnlockedAchievements() + */ HelperClass.AchievementManager.getAllAchievements()
        Log.d("AchievementAdapter", "Data size: ${achievements.size}")
        adapter = AchievementAdapter(achievements)
        recyclerView.adapter = adapter

        val buttonTriggerActions = findViewById<Button>(R.id.button)
        buttonTriggerActions.setOnClickListener {



            // Testing scenario
            HelperClass.AchievementManager.trackDistanceTraveled(num2) // Assume the condition is 5.0 km
            HelperClass.AchievementManager.trackBirdsAdded(num1) // Assume the condition is 5 birds
            HelperClass.AchievementManager.trackMarkerPlaced()

            num1 += 9
            num2 += 8.0
            Log.d("Numbers 1", "num1: ${num1}")
            //testing


            adapter.notifyDataSetChanged()
            achievementFirestore()
            // Update the RecyclerView with the new data
            //adapter.updateData(helperClass.AchievementManager.getUnlockedAchievements() + helperClass.AchievementManager.getAllAchievements())


        }
    }

    private fun achievementFirestore(){
        val currentUser = FirebaseAuth.getInstance().currentUser

        Log.d("User", "user: ${currentUser}")
        if (currentUser != null) {

            val db = FirebaseFirestore.getInstance()

            for(achievement in HelperClass.AchievementManager.achievementList){

                val achievementData = hashMapOf(
                    "id" to achievement.id,
                    "isUnlocked" to achievement.isUnlocked,
                    "user" to currentUser.uid
                )

                db.collection("Achievements")
                    .add(achievementData)
                    .addOnSuccessListener { documentReference ->
                        // Document added successfully
                        Log.d(MotionEffect.TAG, "data saved:success")
                        val alertDialog = AlertDialog.Builder(this)
                        alertDialog.setTitle("Successfully Saved")
                        alertDialog.setMessage("Achievement Saved")
                        alertDialog.setPositiveButton("OK") { dialog, _ ->
                            // when the user clicks OK
                            dialog.dismiss()
                        }
                        alertDialog.show()
                    }
                    .addOnFailureListener { e ->
                        // Handle errors
                        Log.d(MotionEffect.TAG, e.message.toString())
                        Log.d(MotionEffect.TAG, "data saved:failure")
                        val alertDialog = AlertDialog.Builder(this)
                        alertDialog.setTitle("unsuccessfully Saved")
                        alertDialog.setMessage("Achievement Not Saved")
                        alertDialog.setPositiveButton("OK") { dialog, _ ->
                            // when the user clicks OK
                            dialog.dismiss()
                        }
                        alertDialog.show()
                    }
            }


        }

    }
}