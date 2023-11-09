package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.location.Geocoder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListObservationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListObservationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: birdAdapter
    lateinit var helperClass: HelperClass

    private var mCurrentLocation: Location = Location("dummy_provider")
    private lateinit var editText: EditText

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_list_observations, container, false)

        editText = view.findViewById(R.id.editTextBird)

        //helperClass.BirdMap
        val birdList = getBirdData()

        val addButton = view.findViewById<Button>(R.id.btnAdd)
        val refButton = view.findViewById<Button>(R.id.btnRefresh)

        addButton?.setOnClickListener {
            btnAddClick()
        }

        refButton?.setOnClickListener {
            btnRefreshClick()
        }

        helperClass = HelperClass()
        recyclerView = view.findViewById(R.id.recyclerView)

        adapter = birdAdapter(birdList)
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        return view

    }


    private fun getBirdData(): List<BirdItem> {
        val birdList = mutableListOf<BirdItem>()
        return birdList
    }

    fun getLocation(location: Location?){
        Log.d(TAG, "getLocation: FRAGMENT GETLOCATION HAS RUN")
        if (location != null) {
            mCurrentLocation = location
            Log.d(TAG, "getLocation: MCURRENTLOCATION HAS THE LOCATION ${mCurrentLocation.latitude} AND ${mCurrentLocation.longitude}")
        }
    }

    private fun getLocationName(location: Location): String {
        val geocoder = Geocoder(requireContext())
        var returnName = ""
        try {
            Log.d(TAG, "New Location - Latitude: ${location.latitude}, Longitude: ${location.longitude}")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            Log.d(TAG, "ADDRESSES: ${addresses?.get(0)}")
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val cityName = address.locality
                    val addressString = address.getAddressLine(0)
                    Log.d(TAG, "ADDRESS: ${address.countryName} AND $addressString")
                    returnName = "$cityName, $addressString"
                    Log.d(TAG, "getLocationName THIS IS THE NAME: $returnName")
                } else {
                    Log.d(TAG, "getLocationNameFromCoordinates: COULD NOT GET LOCATION NAME")
                    returnName = ""
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getLocationNameFromCoordinates: EXCEPTION $e")
        }
        return returnName
    }

    fun btnAddClick(){
        if(editText.text.isNotEmpty()){
            val name = editText.text.toString()

            //put getLocation here pls
            val loc = getLocationName(mCurrentLocation)
            Log.d(mCurrentLocation.longitude.toString(),mCurrentLocation.latitude.toString())
            observationsFirestore(name,mCurrentLocation) // Calls the function to save the observation
            val date = getCurrentDateTime()
            saveEntry(name, loc, date)
        }else{
            Toast.makeText(requireContext(),"Invalid Input", Toast.LENGTH_LONG).show()
        }
    }

    fun btnRefreshClick(){
        val birdList = updateList(helperClass.BirdMap)
        // Notify the adapter that the data has changed
        adapter = birdAdapter(birdList)
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        Toast.makeText(requireContext(),"Refreshed!", Toast.LENGTH_SHORT).show()
    }


    fun updateList(birdMap: HashMap<String, HelperClass.Bird>): List<BirdItem>{
        val birdList = mutableListOf<BirdItem>()

        if(birdMap.isNotEmpty()){
            for ((birdName, bird) in birdMap) {
                // Use the birdName as the name, and get other details from the Bird object
                birdList.add(BirdItem(R.drawable.bird_svgrepo_com, birdName, bird.dateTime, bird.location))
            }
        }else{
            Toast.makeText(requireContext(),"No Saved Observations", Toast.LENGTH_LONG).show()
        }
        return birdList
    }

    fun saveEntry(name: String, location: String, date: String){
        helperClass.addToList(name, name, location, date)
        Toast.makeText(requireContext(),"Saved!", Toast.LENGTH_SHORT).show()
    }
    //---------------------------------------------------------------------------------------\\
    fun getCurrentDateTime(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(cal.time)
    }
    private fun observationsFirestore(BirdName: String, location: Location){
        val currentUser = FirebaseAuth.getInstance().currentUser
        val OA = ObservationsActivity()
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val observationData = hashMapOf(
                "BirdName" to BirdName,
                "Latitude" to location.latitude,
                "Longitude" to location.longitude,
                "user" to currentUser.uid
            )

            db.collection("BirdObservations")
                .add(observationData)
                .addOnSuccessListener { documentReference ->
                    // Document added successfully
                    Log.d(MotionEffect.TAG, "data saved:success")
                    val alertDialog = AlertDialog.Builder(requireActivity())
                    alertDialog.setTitle("Successfully Saved")
                    alertDialog.setMessage("Observation Saved")
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
                    val alertDialog = AlertDialog.Builder(requireActivity())
                    alertDialog.setTitle("unsuccessfully Saved")
                    alertDialog.setMessage("Observation Not Saved")
                    alertDialog.setPositiveButton("OK") { dialog, _ ->
                        // when the user clicks OK
                        dialog.dismiss()
                    }
                    alertDialog.show()
                }
        }

    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListObservationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListObservationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}