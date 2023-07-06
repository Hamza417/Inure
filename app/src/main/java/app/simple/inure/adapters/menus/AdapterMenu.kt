package app.simple.inure.adapters.menus

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.AppInformationPreferences

class AdapterMenu(val list: List<Pair<Int, Int>>, layoutStyle: Int) : RecyclerView.Adapter<AdapterMenu.Holder>() {

    private var adapterMenuCallbacks: AdapterMenuCallbacks? = null

    private val menuLayout = when (layoutStyle) {
        AppInformationPreferences.MENU_LAYOUT_GRID -> R.layout.adapter_app_info_menu_grid
        AppInformationPreferences.MENU_LAYOUT_HORIZONTAL -> R.layout.adapter_app_info_menu_horizontal
        else -> R.layout.adapter_app_info_menu_grid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(menuLayout, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = holder.itemView.context.getString(list[position].second)
        holder.icon.setImageResource(list[position].first)
        holder.text.setText(list[position].second)

        if (AccessibilityPreferences.isColorfulIcons()) {
            holder.icon.imageTintList = ColorStateList.valueOf(Colors.getColors()[position])
        }

        holder.container.setOnClickListener {
            adapterMenuCallbacks?.onAppInfoMenuClicked(list[position].second, holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_app_info_menu_icon)
        val text: TypeFaceTextView = itemView.findViewById(R.id.adapter_app_info_menu_text)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.adapter_app_info_menu_container)

        init {
            text.isSelected = true
        }
    }

    fun setOnAppInfoMenuCallback(adapterMenuCallbacks: AdapterMenuCallbacks) {
        this.adapterMenuCallbacks = adapterMenuCallbacks
    }

    interface AdapterMenuCallbacks {
        fun onAppInfoMenuClicked(source: Int, icon: ImageView)
    }
}