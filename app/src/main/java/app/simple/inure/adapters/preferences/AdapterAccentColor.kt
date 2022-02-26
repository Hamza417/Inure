package app.simple.inure.adapters.preferences

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerAccentColor
import app.simple.inure.decorations.overscroll.RecyclerViewConstants.TYPE_HEADER
import app.simple.inure.decorations.overscroll.RecyclerViewConstants.TYPE_ITEM
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.Utils
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.AppearancePreferences.getCornerRadius
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.ConditionUtils.isZero
import org.jetbrains.annotations.NotNull
import java.util.*

class AdapterAccentColor(private val list: ArrayList<Pair<Int, String>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var palettesAdapterCallbacks: PalettesAdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_accent_colors, parent, false))
            }
            TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_accent_color, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        if (holder is Holder) {
            holder.color.backgroundTintList = ColorStateList.valueOf(list[position].first)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                holder.color.outlineSpotShadowColor = list[position].first
                holder.color.outlineAmbientShadowColor = list[position].first
            }

            holder.container.setOnClickListener {
                palettesAdapterCallbacks.onColorPressed(list[position].first)
            }

            holder.name.text = list[position].second
            holder.hex.text = list[position].first.toHexColor()

            holder.container.background = null
            holder.container.background = getRippleDrawable(holder.container.background, list[position].first)

            holder.tick.visibility = if (list[position].first == AppearancePreferences.getAccentColor()) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        } else if (holder is Header) {
            holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            TYPE_HEADER
        } else TYPE_ITEM
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val color: DynamicCornerAccentColor = itemView.findViewById(R.id.adapter_palette_color)
        val tick: ImageView = itemView.findViewById(R.id.adapter_accent_check_icon)
        val name: TextView = itemView.findViewById(R.id.color_name)
        val hex: TextView = itemView.findViewById(R.id.color_hex)
        val container: LinearLayout = itemView.findViewById(R.id.color_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TextView = itemView.findViewById(R.id.adapter_accent_total)
    }

    fun setOnPaletteChangeListener(palettesAdapterCallbacks: PalettesAdapterCallbacks) {
        this.palettesAdapterCallbacks = palettesAdapterCallbacks
    }

    private fun getRippleDrawable(backgroundDrawable: Drawable?, color: Int): RippleDrawable {
        val outerRadii = FloatArray(8)
        val innerRadii = FloatArray(8)
        Arrays.fill(outerRadii, getCornerRadius().toFloat())
        Arrays.fill(innerRadii, getCornerRadius().toFloat())
        val shape = RoundRectShape(outerRadii, null, innerRadii)
        val mask = ShapeDrawable(shape)
        val stateList = ColorStateList.valueOf(color)
        val rippleDrawable = RippleDrawable(stateList, backgroundDrawable, mask)
        rippleDrawable.alpha = Utils.alpha
        return rippleDrawable
    }

    companion object {
        interface PalettesAdapterCallbacks {
            fun onColorPressed(@NotNull source: Int)
        }
    }
}