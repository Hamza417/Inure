package app.simple.inure.adapters.details

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.glide.util.ImageLoader.loadGraphics
import app.simple.inure.util.StringUtils.optimizeToColoredString

class AdapterGraphics(val path: String, val list: MutableList<String>) : RecyclerView.Adapter<AdapterGraphics.Holder>() {

    private lateinit var graphicsCallbacks: GraphicsCallbacks
    private var xOff = 0f
    private var yOff = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_graphics, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.xml.text = list[position].optimizeToColoredString(holder.itemView.context, "/")
        holder.graphics.loadGraphics(path, list[position])

        holder.container.setOnTouchListener { _, event ->
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    xOff = event.x
                    yOff = event.y
                }
            }
            false
        }

        holder.container.setOnClickListener {
            graphicsCallbacks.onGraphicsClicked(path, list[position], holder.container, xOff, yOff)
        }

        holder.container.setOnLongClickListener {
            graphicsCallbacks.onGraphicsLongPressed(list[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnResourceClickListener(resourceCallbacks: GraphicsCallbacks) {
        this.graphicsCallbacks = resourceCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_graphics_container)
        val graphics: ImageView = itemView.findViewById(R.id.adapter_graphics_iv)
        val xml: TypeFaceTextView = itemView.findViewById(R.id.adapter_graphics_name)
    }

    interface GraphicsCallbacks {
        fun onGraphicsClicked(path: String, filePath: String, view: ViewGroup, xOff: Float, yOff: Float)
        fun onGraphicsLongPressed(filePath: String)
    }
}