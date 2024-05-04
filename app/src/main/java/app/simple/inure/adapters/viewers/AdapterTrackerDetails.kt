package app.simple.inure.adapters.viewers

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
    @Suppress("unused")
    private fun getColorBasedOnCategory(category: String): Int {
        return when (category) {
            "Advertisement" -> {
                0xFFCB4335.toInt() // Red
            }
            "Crash reporting" -> {
                0xFF7F8C8D.toInt() // Grey
            }
            "Profiling" -> {
                0xFF2471A3.toInt() // Blue
            }
            "Identification" -> {
                0xFFF39C12.toInt() // Orange
            }
            "Location" -> {
                0xFF27AE60.toInt() // Green
            }
            "Analytics" -> {
                0xFF8E44AD.toInt() // Purple
            }
            "Activity" -> {
                0xFFF1C40F.toInt() // Yellow
            }
            "Service" -> {
                0xFFE74C3C.toInt() // Dark Red
            }
            "Receiver" -> {
                0xFF2980B9.toInt() // Light Blue
            }
            else -> {
                0xFF34495E.toInt() // Dark Grey
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
