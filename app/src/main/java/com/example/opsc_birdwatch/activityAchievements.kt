package com.example.opsc_birdwatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
            // Update the RecyclerView with the new data
            //adapter.updateData(helperClass.AchievementManager.getUnlockedAchievements() + helperClass.AchievementManager.getAllAchievements())


        }
    }
}