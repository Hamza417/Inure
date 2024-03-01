package app.simple.inure.adapters.details

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.views.Chip

class AdapterTrackerDetails(private val data: ArrayList<String>) : RecyclerView.Adapter<AdapterTrackerDetails.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_tracker_details, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.chip.chipBackgroundColor = ColorStateList.valueOf(getColorBasedOnCategory(data[position]))
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
                Color.colorToHSV(Color.parseColor("#FF6347"), hsv) // Tomato
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Crash reporting" -> {
                Color.colorToHSV(Color.parseColor("#40E0D0"), hsv) // Turquoise
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Profiling" -> {
                Color.colorToHSV(Color.parseColor("#800080"), hsv) // Purple
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Identification" -> {
                Color.colorToHSV(Color.parseColor("#FFD700"), hsv) // Gold
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Location" -> {
                Color.colorToHSV(Color.parseColor("#228B22"), hsv) // Forest Green
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Analytics" -> {
                Color.colorToHSV(Color.parseColor("#FF4500"), hsv) // Orange Red
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Activity" -> {
                Color.colorToHSV(Color.parseColor("#6A5ACD"), hsv) // Slate Blue
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Service" -> {
                Color.colorToHSV(Color.parseColor("#2E8B57"), hsv) // Sea Green
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            "Receiver" -> {
                Color.colorToHSV(Color.parseColor("#DB7093"), hsv) // Pale Violet Red
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
            }
            else -> {
                Color.colorToHSV(Color.parseColor("#808080"), hsv) // Gray for unknown category
                hsv[1] *= saturationFactor
                Color.HSVToColor(hsv)
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