package com.example.opsc_birdwatch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var boolTrue: Boolean = false
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
        // Add your bird data to the list
        birdList.add(BirdItem(R.drawable.bird_svgrepo_com, "Bird 1", "Date 1", "Location 1"))


        // Add more birds as needed
        return birdList
    }

    fun btnAddClick(){
        val name = editText.text.toString()
        val loc = "here"
        val date = getCurrentDateTime()
        saveEntry(name, name, date)
    }

    fun btnRefreshClick(){
        val birdList = updateList(helperClass.BirdMap)


        // Notify the adapter that the data has changed


        adapter = birdAdapter(birdList)
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        Toast.makeText(requireContext(),"Refreshed!", Toast.LENGTH_LONG).show()
    }


    fun updateList(birdMap: HashMap<String, HelperClass.Bird>): List<BirdItem>{
        val birdList = mutableListOf<BirdItem>()

        if(birdMap.isNotEmpty()){
            for ((birdName, bird) in birdMap) {
                // Use the birdName as the name, and get other details from the Bird object
                birdList.add(BirdItem(R.drawable.bird_svgrepo_com, birdName, bird.dateTime, bird.location))
            }
        }

        return birdList
    }

    fun saveEntry(name: String, location: String, date: String){
        helperClass.addToList(name, name, location, date)
        Toast.makeText(requireContext(),"Saved!", Toast.LENGTH_LONG).show()
    }

    fun getCurrentDateTime(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(cal.time)
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