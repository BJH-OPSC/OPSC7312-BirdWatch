package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc_birdwatch.HelperClass.Achievement
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.grpc.LoadBalancer.Helper

class activityAchievements : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AchievementAdapter

    private lateinit var drawerLayout: DrawerLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val achievements = HelperClass.AchievementManager.getAllAchievements()
        Log.d("AchievementAdapter", "Data size: ${achievements.size}")
        adapter = AchievementAdapter(achievements)
        recyclerView.adapter = adapter



        fetchAchievements()
        adapter.notifyDataSetChanged()

        val buttonTriggerActions = findViewById<Button>(R.id.button)
        buttonTriggerActions.setOnClickListener {

            //achievementFirestore()

            // Testing scenario
            //HelperClass.AchievementManager.trackDistanceTraveled(5.0) // Assume the condition is 5.0 km
            //HelperClass.AchievementManager.trackBirdsAdded(5) // Assume the condition is 5 birds
            //HelperClass.AchievementManager.trackMarkerPlaced()

            // Update the RecyclerView with the new data

        }

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setCheckedItem(R.id.nav_home)
    }

    private fun achievementFirestore(id: String, status: Boolean){
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        Log.d("User", "user: ${currentUser}")

        if (userId != null) {

            val db = FirebaseFirestore.getInstance()

            for(achievement in HelperClass.AchievementManager.achievementList){

                val achievementData = hashMapOf(
                    "id" to achievement.id,
                    "isUnlocked" to achievement.isUnlocked,
                    "user" to currentUser.uid
                )
                Log.d("Achievements id:",achievement.id.toString())
                db.collection("Achievements")
                    .add(achievementData)
                    .addOnSuccessListener { documentReference ->
                        // Document added successfully
                        Log.d(MotionEffect.TAG, "data saved:success")
                        val alertDialog = AlertDialog.Builder(this)
                        //alertDialog.setTitle("Successfully Saved")
                        //alertDialog.setMessage("Achievement Saved")
                        alertDialog.setPositiveButton("OK") { dialog, _ ->
                            // when the user clicks OK
                            dialog.dismiss()
                        }
                        //alertDialog.show()
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

    fun fetchAchievements() {
        try { // Reference to the Firestore collection
            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("Achievements")

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            if (userId != null) {
                Log.d("ContentValues", "fetchAchievement: userID $userId")
            } else {
                Log.d("ContentValues", "fetchAchievement: User is not authenticated")
            }
            // Query the collection based on the "user" field
            collectionRef.whereEqualTo("user", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                   val fetchedAchievements = mutableListOf<Achievement>()

                    for (doc in querySnapshot) {
                        // doc.data contains the document data
                        val achievementID = doc.getString("id")
                        val achievementStatus = doc.getBoolean("isUnlocked")

                        Log.d("ContentValues", "$achievementID" + "$achievementStatus")

                        // Update the corresponding achievement in the list
                        if (achievementID != null && achievementStatus != null) {
                            HelperClass.AchievementManager.achievementList =
                                updateAchievementStatus(achievementID, achievementStatus)

                            // Notify the adapter that the data has changed
                            adapter.updateData(HelperClass.AchievementManager.achievementList)
                        }
                    }

                    //the fetched achievements in the AchievementManager
                    HelperClass.AchievementManager.fetchedAchievements = fetchedAchievements

                    // Notify the adapter with the updated data
                    adapter.updateData(HelperClass.AchievementManager.achievementList + fetchedAchievements)
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    // This will be called if there is an issue with retrieving the data
                    Log.d(MotionEffect.TAG, "fetchAchievement: failure " + e.message.toString())
                }
        }catch (e: Exception){
            Log.d(ContentValues.TAG, "fetchAchievement: "+e.message.toString())
        }
    }

    //only needs to work in this class
    //just for changing the appearance of the achievements
    private fun updateAchievementStatus(achievementID: String, isUnlocked: Boolean): List<HelperClass.Achievement> {
        val updatedList = HelperClass.AchievementManager.achievementList.map { achievement ->
            if (achievement.id == achievementID) {
                achievement.copy(isUnlocked = isUnlocked)
            } else {
                achievement
            }
        }

        return updatedList
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val currentActivity = this::class.java

                //check whats the current activity, if not the main activity it will start the main activity
                if (currentActivity == activityAchievements::class.java){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                }
            }

            R.id.nav_map -> {startActivity(Intent(this, MapActivity::class.java))}

            R.id.nav_list -> {startActivity(Intent(this, ObservationsActivity::class.java))}

            R.id.nav_settings -> {startActivity(Intent(this, SettingsActivity::class.java))}

            R.id.nav_about -> {
                val currentActivity = this::class.java

                if (currentActivity == activityAchievements::class.java){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AboutFragment()).commit()
                }
            }

            R.id.nav_login -> {startActivity(Intent(this, SignInActivity::class.java))}

            R.id.nav_logout -> Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}