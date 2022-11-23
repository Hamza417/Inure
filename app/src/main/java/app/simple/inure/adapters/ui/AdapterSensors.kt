package app.simple.inure.adapters.ui

import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.SensorsPreferences
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.Sort
import app.simple.inure.util.SortSensors

class AdapterSensors(private val sensors: MutableList<Sensor>) : RecyclerView.Adapter<VerticalListViewHolder>(), PopupTextProvider {

    private var adapterSensorCallbacks: AdapterSensorCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_sensors, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_sensors, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
            with(holder) {
                name.text = sensors[position].name

                status.text = with(StringBuilder()) {
                    append(sensors[position].minDelay)
                    append(" | ")
                    append(sensors[position].maxDelay)
                    append(" | ")
                    append("${sensors[position].power} mAh")
                    append(" | ")
                    append(sensors[position].maximumRange)
                    append(" | ")
                    append(sensors[position].resolution)
                    this
                }

                type.text = sensors[position].stringType
                vendor.text = sensors[position].vendor
            }
        } else if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_sensors), sensors.size)

            holder.sorting.text = when (SensorsPreferences.getSortStyle()) {
                SortSensors.NAME -> {
                    holder.getString(R.string.name)
                }
                SortSensors.POWER -> {
                    holder.getString(R.string.power)
                }
                SortSensors.MAX_RANGE -> {
                    holder.getString(R.string.maximum_range)
                }
                SortSensors.RESOLUTION -> {
                    holder.getString(R.string.resolution)
                }
                else -> {
                    holder.getString(R.string.unknown)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return sensors.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_name)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_status)
        val type: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_type)
        val vendor: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_version)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_sensor_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_sensors)
        val sorting: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
    }

    override fun getPopupText(position: Int): String {
        return sensors[position].name.substring(0, 1)
    }

    fun setOnAdapterSensorCallbackListener(adapterSensorCallbacks: AdapterSensorCallbacks) {
        this.adapterSensorCallbacks = adapterSensorCallbacks
    }

    companion object {
        interface AdapterSensorCallbacks {
            fun onSortPressed(view: View)
        }
    }
}