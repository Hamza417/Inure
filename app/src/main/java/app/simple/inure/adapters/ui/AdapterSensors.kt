package app.simple.inure.adapters.ui

import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterSensors(private val sensors: MutableList<Sensor>) : RecyclerView.Adapter<VerticalListViewHolder>(), PopupTextProvider {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_sensors_header, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
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
        }
    }

    override fun getItemCount(): Int {
        return sensors.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewConstants.TYPE_HEADER
        } else RecyclerViewConstants.TYPE_ITEM
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
    }

    override fun getPopupText(position: Int): String {
        return sensors[position].name.substring(0, 1)
    }
}