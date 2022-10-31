package app.simple.inure.adapters.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ThemeStateIcon
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class AdapterTheme : RecyclerView.Adapter<VerticalListViewHolder>() {

    private val list = arrayListOf(
            -1,
            ThemeConstants.LIGHT_THEME,
            ThemeConstants.SOAPSTONE,
            -1,
            ThemeConstants.DARK_THEME,
            ThemeConstants.AMOLED,
            ThemeConstants.SLATE,
            ThemeConstants.OIL,
            ThemeConstants.HIGH_CONTRAST,
            -1,
            ThemeConstants.FOLLOW_SYSTEM,
            ThemeConstants.DAY_NIGHT,
    )

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(-1)
            list.add(ThemeConstants.MATERIAL_YOU)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_type_face, parent, false))
            }
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_app_theme, parent, false))
            }
            RecyclerViewUtils.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_divider_app_theme, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        when (holder) {
            is Holder -> {
                holder.textView.text = holder.itemView.context.getThemeName(list[position])

                if (AppearancePreferences.getTheme() == list[position]) {
                    holder.icon.visible(false)
                } else {
                    holder.icon.invisible(false)
                }

                holder.container.setOnClickListener {
                    if (AppearancePreferences.setTheme(list[position])) {
                        when (list[position]) {
                            ThemeConstants.LIGHT_THEME,
                            ThemeConstants.SOAPSTONE -> {
                                AppearancePreferences.setLastLightTheme(list[position])
                            }
                            ThemeConstants.HIGH_CONTRAST,
                            ThemeConstants.DARK_THEME,
                            ThemeConstants.SLATE,
                            ThemeConstants.AMOLED,
                            ThemeConstants.OIL -> {
                                AppearancePreferences.setLastDarkTheme(list[position])
                            }
                            ThemeConstants.MATERIAL_YOU -> {
                                AppearancePreferences.setLastLightTheme(list[position])
                                AppearancePreferences.setLastDarkTheme(list[position])
                            }
                        }

                        notifyDataSetChanged()
                    }
                }
            }
            is Header -> {
                holder.title.text = holder.itemView.context.getString(R.string.application_theme)
                holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
            }
            is Divider -> {
                holder.type.text = when (position) {
                    0 -> holder.getString(R.string.light)
                    3 -> holder.getString(R.string.dark)
                    9 -> holder.getString(R.string.auto)
                    12 -> "Material"
                    else -> holder.getString(R.string.unknown)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewUtils.TYPE_HEADER
        } else if (list[position - 1] == -1) {
            RecyclerViewUtils.TYPE_DIVIDER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    private fun Context.getThemeName(theme: Int): String {
        return when (theme) {
            ThemeConstants.LIGHT_THEME -> getString(R.string.light)
            ThemeConstants.SOAPSTONE -> getString(R.string.soapstone)
            ThemeConstants.DARK_THEME -> getString(R.string.dark)
            ThemeConstants.AMOLED -> getString(R.string.amoled)
            ThemeConstants.SLATE -> getString(R.string.slate)
            ThemeConstants.OIL -> getString(R.string.oil)
            ThemeConstants.HIGH_CONTRAST -> getString(R.string.high_contrast)
            ThemeConstants.FOLLOW_SYSTEM -> getString(R.string.follow_system)
            ThemeConstants.DAY_NIGHT -> getString(R.string.day_night)
            ThemeConstants.MATERIAL_YOU -> "Material You"
            else -> "throw IllegalArgumentException( Value"
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val textView: TypeFaceTextView = itemView.findViewById(R.id.adapter_typeface_textview)
        val icon: ThemeIcon = itemView.findViewById(R.id.adapter_typeface_check_icon)
        val container: LinearLayout = itemView.findViewById(R.id.adapter_typeface_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_type_face_total)
        val icon: ThemeStateIcon = itemView.findViewById(R.id.theme_icon)
    }

    inner class Divider(itemView: View) : VerticalListViewHolder(itemView) {
        val type: TypeFaceTextView = itemView.findViewById(R.id.theme_type)
    }
}