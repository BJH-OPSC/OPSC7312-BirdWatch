package com.example.opsc_birdwatch

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementAdapter(private var achievements: List<HelperClass.Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.bind(achievement, achievement.id.toString(), achievement.isUnlocked)
    }

    override fun getItemCount(): Int = achievements.size

    fun updateData(newAchievements: List<HelperClass.Achievement>) {
        achievements = newAchievements.toList()
        notifyDataSetChanged()
        Log.d("AchievementAdapter", "Data updated. New size: ${achievements.size}")
    }

    fun resetImagesToGreyscale() {
        for (achievement in achievements) {
            // Apply greyscale color filter for all achievements
            achievement.isUnlocked = false // Update the unlocked status to false
        }
        notifyDataSetChanged() // Notify adapter that the data has changed
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewAchievement: ImageView = itemView.findViewById(R.id.imageViewAchievement)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        private val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)

        fun bind(achievement: HelperClass.Achievement, achID: String, achStatus: Boolean) {


            // Check if the achievement is in the fetched achievements list
            //val isFetched = HelperClass.AchievementManager.fetchedAchievements.any { it.id == achievement.id }

            // Extract level prefix from achievement name
            val levelPrefix = getLevelPrefix(achID)
            val isUnlocked = achStatus

            // Set image resource based on level prefix
            val imageResource = when (levelPrefix) {
                "gold" -> R.drawable.goldtrophyimg
                "silver" -> R.drawable.silvertrophyimg
                "bronze" -> R.drawable.bronzetrophyimg
                else -> R.drawable.goldtrophyimg
            }

            if (achievement.isUnlocked) {
                imageViewAchievement.setImageResource(imageResource)
                imageViewAchievement.colorFilter = null // Remove color filter for unlocked achievements
            } else {
                // Apply greyscale color filter for locked achievements
                val colorMatrix = ColorMatrix()
                colorMatrix.setSaturation(0f) // 0 means greyscale

                val colorFilter = ColorMatrixColorFilter(colorMatrix)
                imageViewAchievement.setImageResource(imageResource)
                imageViewAchievement.colorFilter = colorFilter
            }

            textViewName.text = achievement.name
            textViewDescription.text = achievement.description
            textViewStatus.text = if (achievement.isUnlocked) "Unlocked" else "Locked"
        }

        private fun getLevelPrefix(achievementName: String): String {
            val prefix = achievementName.substringBefore('-').toLowerCase()
            Log.d("AchievementAdapter", "Achievement ID: $achievementName, Level Prefix: $prefix")
            return prefix
        }


    }

}