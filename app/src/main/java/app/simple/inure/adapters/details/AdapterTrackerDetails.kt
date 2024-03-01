package app.simple.inure.adapters.details

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.views.Chip

class AdapterTrackerDetails(private val data: ArrayList<String>) : RecyclerView.Adapter<AdapterTrackerDetails.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_tracker_details, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.chip.chipBackgroundColor = ColorStateList.valueOf(Colors.getColors()[position])
        holder.chip.text = holder.itemView.context.getContextString(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.chip)

        init {
            chip.setChipStrokeColor(Color.TRANSPARENT)
            chip.setTextColor(Color.WHITE)
        }
    }

    /**
     * Advertisement
     * Crash reporting
     * Profiling
     * Identification
     * Location
     * Analytics
     * Activity
     * Service
     * Receiver
     */
    private fun getColorBasedOnCategory(category: String): Int {
        val hsv = FloatArray(3)
        val saturationFactor = 0.8f // decrease saturation by 20%

        return when (category) {
            "Advertisement" -> {
                Color.parseColor("#cb4335") // Red
            }
            "Crash reporting" -> {
                Color.parseColor("#7f8c8d") // Grey
            }
            "Profiling" -> {
                Color.parseColor("#2471a3") // Blue
            }
            "Identification" -> {
                Color.parseColor("#f39c12") // Orange
            }
            "Location" -> {
                Color.parseColor("#27ae60") // Green
            }
            "Analytics" -> {
                Color.parseColor("#8e44ad") // Purple
            }
            "Activity" -> {
                Color.parseColor("#f1c40f") // Yellow
            }
            "Service" -> {
                Color.parseColor("#e74c3c") // Dark Red
            }
            "Receiver" -> {
                Color.parseColor("#2980b9") // Light Blue
            }
            else -> {
                Color.parseColor("#34495e") // Dark Grey
            }
        }
    }

    /**
     * Get the string from the context
     * using the raw category string
     */
    private fun Context.getContextString(category: String): String {
        return when (category) {
            "Advertisement" -> {
                "Advertisement"
            }
            "Crash reporting" -> {
                "Crash reporting"
            }
            "Profiling" -> {
                "Profiling"
            }
            "Identification" -> {
                "Identification"
            }
            "Location" -> {
                getString(R.string.location)
            }
            "Analytics" -> {
                getString(R.string.analytics)
            }
            "Activity" -> {
                getString(R.string.activity)
            }
            "Service" -> {
                getString(R.string.service)
            }
            "Receiver" -> {
                getString(R.string.receiver)
            }
            else -> {
                category
            }
        }
    }
}