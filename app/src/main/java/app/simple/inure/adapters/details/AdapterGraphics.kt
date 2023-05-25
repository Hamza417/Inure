package app.simple.inure.adapters.details

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadGraphics
import app.simple.inure.preferences.GraphicsPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.StringUtils.highlightExtensions
import app.simple.inure.util.StringUtils.optimizeToColoredString

class AdapterGraphics(val path: String, var list: MutableList<String>, var keyword: String) : RecyclerView.Adapter<AdapterGraphics.Holder>(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var graphicsCallbacks: GraphicsCallbacks
    private var xOff = 0f
    private var yOff = 0f
    private var isHighlighted: Boolean = GraphicsPreferences.isExtensionsHighlighted()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_graphics, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = if (isHighlighted) {
            list[position].optimizeToColoredString("/").highlightExtensions()
        } else {
            list[position].optimizeToColoredString("/")
        }

        holder.image.loadGraphics(path, list[position])

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

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        GlideApp.with(holder.image).clear(holder.image)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: MutableList<String>, keyword: String) {
        this.keyword = keyword
        this.list = list
        notifyDataSetChanged()
    }

    fun setOnResourceClickListener(resourceCallbacks: GraphicsCallbacks) {
        this.graphicsCallbacks = resourceCallbacks
    }

    fun unregister() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_graphics_container)
        val image: ImageView = itemView.findViewById(R.id.adapter_graphics_iv)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_graphics_name)

        init {
            name.enableSelection()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GraphicsPreferences.extensionHighlight -> {
                isHighlighted = GraphicsPreferences.isExtensionsHighlighted().also {
                    notifyDataSetChanged()
                }
            }
        }
    }

    interface GraphicsCallbacks {
        fun onGraphicsClicked(path: String, filePath: String, view: ViewGroup, xOff: Float, yOff: Float)
        fun onGraphicsLongPressed(filePath: String)
    }
}