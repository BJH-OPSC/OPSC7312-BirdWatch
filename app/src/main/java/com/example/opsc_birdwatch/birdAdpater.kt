package com.example.opsc_birdwatch

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class BirdItem(val img: Int ,val name: String, val dateTime: String, val location: String)

// Reference to a Firestore collection
private val db = FirebaseFirestore.getInstance()
private const val collectionName = "BirdObservations"
private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
    fun fetchBirdData(UserID: String,holder: ViewHolder, position: Int, onComplete: (List<BirdItem>) -> Unit) {
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
                    Log.d(TAG, "fetchBirdData: success ")
                    // Handle the data as needed
                    // For example, you can populate a list of objects with this data
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                // This will be called if there is an issue with retrieving the data
                Log.d(TAG, "fetchBirdData: failure "+ e.message.toString())

            }
    }
}