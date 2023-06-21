package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.LocaleHelper
import app.simple.inure.util.PackageListUtils.setAppInfo
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.Sort
import app.simple.inure.util.StatusBarHeight
import java.util.*

class AdapterApps : RecyclerView.Adapter<VerticalListViewHolder>(), PopupTextProvider {

    var apps = arrayListOf<PackageInfo>()
    private lateinit var adapterCallbacks: AdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                if (LocaleHelper.isAppRussianLocale() && StatusBarHeight.isLandscape(parent.context).invert()) {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_all_apps_ru, parent, false))
                } else {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_all_apps, parent, false))
                }
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_all_apps_small_details, parent, false))
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

            holder.icon.transitionName = apps[position].packageName
            holder.name.text = apps[position].applicationInfo.name
            holder.packageId.text = apps[position].packageName

            if (apps[position].applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                holder.icon.loadAppIcon(apps[position].packageName, false, apps[position].applicationInfo.sourceDir.toFile())
            } else {
                holder.icon.loadAppIcon(apps[position].packageName, apps[position].applicationInfo.enabled)
            }

            holder.name.setStrikeThru(apps[position].applicationInfo.enabled)
            holder.info.setAppInfo(apps[position])

            holder.container.setOnClickListener {
                adapterCallbacks.onAppClicked(apps[position], holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks.onAppLongPressed(apps[position], holder.icon)
                true
            }
        }

        if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), apps.size)

            holder.category.text = when (AppsPreferences.getAppsType()) {
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

            holder.sorting.text = when (AppsPreferences.getSortStyle()) {
                Sort.NAME -> {
                    holder.getString(R.string.name)
                }
                Sort.PACKAGE_NAME -> {
                    holder.getString(R.string.package_name)
                }
                Sort.INSTALL_DATE -> {
                    holder.getString(R.string.install_date)
                }
                Sort.SIZE -> {
                    holder.getString(R.string.app_size)
                }
                Sort.UPDATE_DATE -> {
                    holder.getString(R.string.update_date)
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
        return apps.size + 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val info: TypeFaceTextView = itemView.findViewById(R.id.details)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val sorting: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
        val category: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_category)
    }

    override fun getPopupText(position: Int): String {
        return apps[position].applicationInfo.name.substring(0, 1).uppercase(Locale.ROOT)
    }
}
