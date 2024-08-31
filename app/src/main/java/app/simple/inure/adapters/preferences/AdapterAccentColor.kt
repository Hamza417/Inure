package app.simple.inure.adapters.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerAccentColor
import app.simple.inure.decorations.corners.DynamicCornerMaterialCardView
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.Utils
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.AppearancePreferences.getCornerRadius
import app.simple.inure.themes.data.MaterialYou
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils.TYPE_HEADER
import app.simple.inure.util.RecyclerViewUtils.TYPE_ITEM
import java.util.Arrays

class AdapterAccentColor(private val list: ArrayList<Pair<Int, String>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var lastSelectedItem = 0
    private var accentColorCallbacks: AccentColorCallbacks? = null

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

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {

        val position1 = holder.bindingAdapterPosition - 1

        if (holder is Holder) {
            holder.color.backgroundTintList = ColorStateList.valueOf(list[position1].first)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                holder.color.outlineSpotShadowColor = list[position1].first
                holder.color.outlineAmbientShadowColor = list[position1].first

                holder.container.outlineSpotShadowColor = list[position1].first
                holder.container.outlineAmbientShadowColor = list[position1].first
            }

            holder.container.setOnClickListener {
                if (list[position1].second == holder.getString(R.string.color_picker)) {
                    accentColorCallbacks?.onAccentColorPicker()
                } else {
                    if (AppearancePreferences.setAccentColor(list[position1].first)) {
                        if (AppearancePreferences.setCustomColor(false)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                AppearancePreferences.setMaterialYouAccent(position1 == MaterialYou.materialYouAdapterIndex)
                            }
                            notifyItemChanged(lastSelectedItem)
                            notifyItemChanged(holder.bindingAdapterPosition)
                            lastSelectedItem = holder.bindingAdapterPosition
                        }
                    }
                }
            }

            holder.name.text = list[position1].second
            holder.hex.text = list[position1].first.toHexColor()

            // holder.container.background = null
            // holder.container.background = getRippleDrawable(holder.container.background, list[position1].first)
            holder.container.rippleColor = ColorStateList.valueOf(list[position1].first)

            if (AppearancePreferences.isCustomColor()) {
                holder.tick.visibility = if (list[position1].second == holder.getString(R.string.color_picker)) {
                    lastSelectedItem = holder.bindingAdapterPosition
                    View.VISIBLE
                } else {
                    View.GONE
                }
            } else {
                holder.tick.visibility = if (list[position1].first == AppearancePreferences.getAccentColor()) {
                    lastSelectedItem = holder.bindingAdapterPosition
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }
        } else if (holder is Header) {
            holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
            holder.title.setTextColor(AppearancePreferences.getAccentColor())
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
        val tick: ThemeIcon = itemView.findViewById(R.id.adapter_accent_check_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.color_name)
        val hex: TypeFaceTextView = itemView.findViewById(R.id.color_hex)
        val container: DynamicCornerMaterialCardView = itemView.findViewById(R.id.color_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_accent_total)
        val title: TypeFaceTextView = itemView.findViewById(R.id.title)
    }

    @Suppress("unused")
    private fun getRippleDrawable(backgroundDrawable: Drawable?, color: Int): RippleDrawable {
        val outerRadii = FloatArray(8)
        val innerRadii = FloatArray(8)
        Arrays.fill(outerRadii, getCornerRadius())
        Arrays.fill(innerRadii, getCornerRadius())
        val shape = RoundRectShape(outerRadii, null, innerRadii)
        val mask = ShapeDrawable(shape)
        val stateList = ColorStateList.valueOf(color)
        val rippleDrawable = RippleDrawable(stateList, backgroundDrawable, mask)
        rippleDrawable.alpha = Utils.alpha
        return rippleDrawable
    }

    fun setAccentColorCallbacks(accentColorCallbacks: AccentColorCallbacks?) {
        this.accentColorCallbacks = accentColorCallbacks
    }

    fun updateAccentColor(context: Context) {
        if (AppearancePreferences.isCustomColor()) {
            val position = 1
            list[position] = Pair(AppearancePreferences.getPickedAccentColor(), context.getString(R.string.color_picker))
            notifyItemChanged(lastSelectedItem)
            notifyItemChanged(position.plus(1))
        } else {
            val position = list.find {
                it.first == AppearancePreferences.getAccentColor()
            }?.let {
                list.indexOf(it)
            } ?: 0

            notifyItemChanged(lastSelectedItem)
            notifyItemChanged(position)
        }
    }

    companion object {
        interface AccentColorCallbacks {
            fun onAccentColorPicker()
        }
    }
}
