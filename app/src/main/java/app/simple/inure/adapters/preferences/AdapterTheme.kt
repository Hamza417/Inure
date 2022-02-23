package app.simple.inure.adapters.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class AdapterTheme : RecyclerView.Adapter<VerticalListViewHolder>() {

    private val list = arrayListOf(
            ThemeConstants.LIGHT_THEME,
            ThemeConstants.DARK_THEME,
            ThemeConstants.AMOLED,
            ThemeConstants.SLATE,
            ThemeConstants.HIGH_CONTRAST,
            ThemeConstants.FOLLOW_SYSTEM,
            ThemeConstants.DAY_NIGHT
    )

    var onTouch: (x: Float, y: Float) -> Unit = { _: Float, _: Float -> }
    private var oldPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_type_face, parent, false))
            }
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_typeface, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        when (holder) {
            is Holder -> {
                holder.textView.text = holder.itemView.context.getThemeName(list[position])

                holder.textView.setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                            val location = IntArray(2)
                            view.getLocationOnScreen(location)
                            val x = location[0] + holder.textView.measuredWidth / 2
                            val y = location[1] + holder.textView.measuredHeight / 2

                            if (AppearancePreferences.isTransparentStatusDisabled()) {
                                onTouch.invoke(event.rawX, event.rawY - StatusBarHeight.getStatusBarHeight(holder.itemView.resources))
                            } else {
                                onTouch.invoke(x.toFloat(), y.toFloat())
                            }
                        }
                    }
                    false
                }

                if (AppearancePreferences.getTheme() == list[position]) {
                    holder.icon.visible(false)
                    oldPosition = holder.absoluteAdapterPosition
                } else {
                    holder.icon.invisible(false)
                }

                holder.container.setOnClickListener {
                    if (AppearancePreferences.setTheme(list[position])) {
                        when (list[position]) {
                            ThemeConstants.HIGH_CONTRAST,
                            ThemeConstants.DARK_THEME,
                            ThemeConstants.SLATE,
                            ThemeConstants.AMOLED -> {
                                AppearancePreferences.setLastDarkTheme(list[position])
                            }
                        }

                        notifyItemChanged(oldPosition)
                        notifyItemChanged(holder.absoluteAdapterPosition)
                    }
                }
            }
            is Header -> {
                holder.title.text = holder.itemView.context.getString(R.string.application_theme)
                holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewConstants.TYPE_HEADER
        } else RecyclerViewConstants.TYPE_ITEM
    }

    private fun Context.getThemeName(theme: Int): String {
        return when (theme) {
            ThemeConstants.LIGHT_THEME -> getString(R.string.light)
            ThemeConstants.DARK_THEME -> getString(R.string.dark)
            ThemeConstants.AMOLED -> getString(R.string.amoled)
            ThemeConstants.SLATE -> getString(R.string.slate)
            ThemeConstants.HIGH_CONTRAST -> getString(R.string.high_contrast)
            ThemeConstants.FOLLOW_SYSTEM -> getString(R.string.follow_system)
            ThemeConstants.DAY_NIGHT -> getString(R.string.day_night)
            else -> throw IllegalArgumentException("Unknown Action")
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val textView: TypeFaceTextView = itemView.findViewById(R.id.adapter_typeface_textview)
        val icon: ImageView = itemView.findViewById(R.id.adapter_typeface_check_icon)
        val container: LinearLayout = itemView.findViewById(R.id.adapter_typeface_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}