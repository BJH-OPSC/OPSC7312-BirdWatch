package com.example.opsc_birdwatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class BirdItem(val img: Int ,val name: String, val dateTime: String, val location: String)

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
}