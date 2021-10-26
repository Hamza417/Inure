package app.simple.inure.adapters.details

import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterSensors(private val sensors: MutableList<Sensor>) : RecyclerView.Adapter<AdapterSensors.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_sensors, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        with(holder) {
            name.text = sensors[position].name

            val stringBuilder = StringBuilder()

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
    }

    override fun getItemCount(): Int {
        return sensors.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_name)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_status)
        val type: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_type)
        val vendor: TypeFaceTextView = itemView.findViewById(R.id.adapter_sensor_version)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_sensor_container)
    }
}