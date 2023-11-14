package com.example.opsc_birdwatch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

//adapter to handle custom info window for markers
class CustomInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //function to get the contents of the info window
    override fun getInfoContents(p0: Marker): View? {
        val view = inflater.inflate(R.layout.custom_info_window, null)
        val locationNameTextView = view.findViewById<TextView>(R.id.locationNameTextView)
        val observedTextView = view.findViewById<TextView>(R.id.observedTextView)
        val speciesTextView = view.findViewById<TextView>(R.id.speciesTextView)

        // Get the snippet from the marker
        val snippet = p0.snippet

        // Extract the observation date and species count
        val snippetParts = snippet?.split("\n") // Split the snippet using a newline character
        if (snippetParts != null && snippetParts.size >= 2) {
            val latestObserved = snippetParts[0]
            val speciesCount = snippetParts[1]

            // Populate the info window layout with the data
            locationNameTextView.text = p0.title
            observedTextView.text = latestObserved
            speciesTextView.text = speciesCount
        }

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}

