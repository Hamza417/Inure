package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.models.BootManagerModel
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.SortBatteryOptimization

class AdapterBootManager(private val components: ArrayList<BootManagerModel>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterCallbacks: AdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_boot_manager, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_boot_manager, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
            holder.icon.transitionName = components[position].packageName
            holder.icon.loadAppIcon(components[position].packageName, components[position].isEnabled)
            holder.name.text = components[position].name
            holder.packageId.text = components[position].packageName
            holder.name.setStrikeThru(components[position].isEnabled)

            holder.data.text = with(StringBuilder()) {
                append(holder.context.getString(R.string.total, components[position].enabledComponents.size + components[position].disabledComponents.size))
                append(" | ")
                append(holder.context.getString(R.string.n_enabled, components[position].enabledComponents.size))
                append(" | ")
                append(holder.context.getString(R.string.n_disabled, components[position].disabledComponents.size))
                toString()
            }

            holder.container.setOnClickListener {
                adapterCallbacks?.onBootComponentClicked(it,
                                                         components[holder.bindingAdapterPosition.minus(1)],
                                                         holder.bindingAdapterPosition.minus(1),
                                                         holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks?.onBootComponentLongClicked(it,
                                                             components[holder.bindingAdapterPosition.minus(1)],
                                                             holder.bindingAdapterPosition.minus(1),
                                                             holder.icon)
                true
            }
        } else if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), components.size)

            holder.category.text = when (BatteryOptimizationPreferences.getBatteryOptimizationCategory()) {
                PopupAppsCategory.USER -> {
                    holder.getString(R.string.user)
                }
                PopupAppsCategory.SYSTEM -> {
                    holder.getString(R.string.system)
                }
                PopupAppsCategory.BOTH -> {
                    with(StringBuilder()) {
                        append(holder.getString(R.string.user))
                        append(" | ")
                        append(holder.getString(R.string.system))
                    }
                }
                else -> {
                    holder.getString(R.string.unknown)
                }
            }

            holder.sorting.text = when (BatteryOptimizationPreferences.getBatteryOptimizationSortStyle()) {
                SortBatteryOptimization.NAME -> {
                    holder.getString(R.string.name)
                }
                SortBatteryOptimization.PACKAGE_NAME -> {
                    holder.getString(R.string.package_name)
                }
                SortBatteryOptimization.SIZE -> {
                    holder.getString(R.string.app_size)
                }
                SortBatteryOptimization.INSTALL_DATE -> {
                    holder.getString(R.string.install_date)
                }
                else -> {
                    holder.getString(R.string.unknown)
                }
            }
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            GlideApp.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return components.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun getAppType(type: String): Int {
        return when (type) {
            BatteryOptimizationModel.TYPE_SYSTEM -> {
                R.string.system
            }
            BatteryOptimizationModel.TYPE_USER -> {
                R.string.user
            }
            BatteryOptimizationModel.TYPE_SYSTEM_EXCIDLE -> {
                R.string.system_excidle
            }
            else -> {
                R.string.unknown
            }
        }
    }

    fun updateItem(bootManagerModel: BootManagerModel, position: Int) {
        components[position] = bootManagerModel
        notifyItemChanged(position.plus(1))
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_boot_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_boot_app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_boot_app_package_id)
        val data: TypeFaceTextView = itemView.findViewById(R.id.adapter_boot_data)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_boot_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val sorting: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
        val category: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_category)
    }
}