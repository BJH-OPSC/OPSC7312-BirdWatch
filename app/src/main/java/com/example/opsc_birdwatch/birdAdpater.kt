package com.example.opsc_birdwatch

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class BirdItem(val img: Int ,val name: String, val dateTime: String, val location: String)

// Reference to a Firestore collection
val db = FirebaseFirestore.getInstance()
const val collectionName = "BirdObservations"
val auth: FirebaseAuth = FirebaseAuth.getInstance()

class birdAdapter(private  val itemList: List<BirdItem>):
    RecyclerView.Adapter<birdAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        val tvName: TextView = itemView.findViewById(R.id.txtName)
        val tvDate: TextView = itemView.findViewById(R.id.txtDate)
        val tvLocation: TextView = itemView.findViewById(R.id.txtLocation)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.birds_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.ivImage.setImageResource(item.img)
        holder.tvName.text = item.name
        holder.tvDate.text = item.dateTime
        holder.tvLocation.text = item.location
    }
    private fun fetchBirdData(onComplete: (List<BirdDataItem>) -> kotlin.Unit) {
        // Reference to the Firestore collection
        Log.d(TAG, "fetchBirdData: Called")
        try {

            val collectionRef = db.collection(collectionName)
            val userID = auth.currentUser
            Log.d(TAG, "fetchBirdData: $userID")
            val query = collectionRef.whereEqualTo("user", userID)

            // Query the collection based on the "user" field (assuming "user" is the correct field name)

            query.get()
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        val birdList = mutableListOf<BirdDataItem>()
                        Log.d(TAG, "fetchBirdData: TASK SUCCESSFUL")
                        for (doc in task.result) {
                            // doc.data contains the document data
                            val birdName = doc.getString("BirdName")
                            val latitude = doc.getDouble("Latitude")
                            val longitude = doc.getDouble("Longitude")
                            val date = doc.getString("Date")
                            Log.d(TAG, "fetchBirdData: $date $birdName $latitude $longitude")
                            if (birdName != null && longitude != null && latitude != null && date != null) {
                                //val birdDataItem = BirdDataItem(birdName, latitude.toDouble(), longitude.toDouble(), date)
                                // markerBirdList.add(birdDataItem)

                                val birdDataItem = BirdDataItem(birdName, latitude, longitude, date)
                                birdList.add(birdDataItem)

                            }
                        }
                        onComplete(birdList)

                    } else {
                        Log.d(
                            MotionEffect.TAG,
                            "fetchBirdData: failure " + task.exception?.message.toString()
                        )
                    }

                }
                .addOnFailureListener { e ->
                    Log.d(MotionEffect.TAG, "fetchBirdData: failure " + e.message.toString())

                }

        } catch (e: Exception) {
            // Handle the exception here
            Log.e("FirebaseFunctionError", "An error occurred: ${e.message}")
            Log.v("YourTag", "An error occurred", e)

        }
    }
}