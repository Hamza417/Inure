package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.BootManagerModel
import app.simple.inure.preferences.BootManagerPreferences
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.SortBootManager
import app.simple.inure.util.StatusBarHeight
import com.bumptech.glide.Glide

class AdapterBootManager(private val components: ArrayList<BootManagerModel>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterCallbacks: AdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                if (LocaleUtils.isAppRussianLocale() && StatusBarHeight.isLandscape(parent.context).invert()) {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_boot_manager_ru, parent, false))
                } else {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_boot_manager, parent, false))
                }
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
            holder.icon.transitionName = components[position].packageInfo.packageName
            holder.icon.loadAppIcon(components[position].packageInfo.packageName, components[position].isEnabled)
            holder.name.text = components[position].packageInfo.safeApplicationInfo.name
            holder.packageId.text = components[position].packageInfo.packageName
            holder.name.setAppVisualStates(components[position].packageInfo)

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

            holder.category.text = when (BootManagerPreferences.getAppsCategory()) {
                SortConstant.USER -> {
                    holder.getString(R.string.user)
                }
                SortConstant.SYSTEM -> {
                    holder.getString(R.string.system)
                }
                SortConstant.BOTH -> {
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

            holder.sorting.text = when (BootManagerPreferences.getSortStyle()) {
                SortBootManager.NAME -> {
                    holder.getString(R.string.name)
                }
                SortBootManager.PACKAGE_NAME -> {
                    holder.getString(R.string.package_name)
                }
                SortBootManager.SIZE -> {
                    holder.getString(R.string.app_size)
                }
                SortBootManager.INSTALL_DATE -> {
                    holder.getString(R.string.install_date)
                }
                SortBootManager.UPDATE_DATE -> {
                    holder.getString(R.string.update_date)
                }
                SortBootManager.TARGET_SDK -> {
                    holder.getString(R.string.target_sdk)
                }
                SortBootManager.MIN_SDK -> {
                    holder.getString(R.string.minimum_sdk)
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
            Glide.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return components.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun updateItem(bootManagerModel: BootManagerModel, position: Int) {
        components[position] = bootManagerModel
        notifyItemChanged(position.plus(1))
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.adapter_boot_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_boot_app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_boot_app_package_id)
        val data: TypeFaceTextView = itemView.findViewById(R.id.adapter_boot_data)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_boot_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val sorting: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
        val category: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_category)
    }
}
