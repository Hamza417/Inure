package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.enums.Removal
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.models.Bloat
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.StringUtils.appendFlag

class AdapterDebloat(private val bloats: ArrayList<Bloat>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterDebloatCallback: AdapterDebloatCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_debloat, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_debloat, parent, false))
            }
            else -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_debloat, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        val pos = holder.bindingAdapterPosition.minus(1)
        when (holder) {
            is Holder -> {
                holder.name.text = bloats[pos].packageInfo.applicationInfo.name
                holder.packageName.text = bloats[pos].packageInfo.packageName
                holder.flags.setBloatFlags(bloats[pos])
                holder.desc.text = bloats[pos].description
                holder.checkBox.isChecked = bloats[pos].isSelected
                holder.icon.loadAppIcon(bloats[pos].packageInfo.packageName, bloats[pos].packageInfo.applicationInfo.enabled)

                holder.checkBox.setOnCheckedChangeListener {
                    bloats[pos].isSelected = it
                    adapterDebloatCallback?.onBloatSelected(bloats[pos])
                }

                holder.container.setOnClickListener {
                    holder.checkBox.toggle()
                }
            }
            is Header -> {
                holder.total.text = holder.total.context.getString(R.string.total_apps, bloats.size.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return bloats.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageName: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val flags: TypeFaceTextView = itemView.findViewById(R.id.flags)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.desc)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
    }

    fun setAdapterDebloatCallback(adapterDebloatCallback: AdapterDebloatCallback) {
        this.adapterDebloatCallback = adapterDebloatCallback
    }

    fun isAnyItemSelected(): Boolean {
        bloats.forEach {
            if (it.isSelected) {
                return true
            }
        }
        return false
    }

    private fun TypeFaceTextView.setBloatFlags(bloat: Bloat) {
        text = buildString {
            if (bloat.packageInfo.applicationInfo.enabled) {
                appendFlag(this@setBloatFlags.context.getString(R.string.enabled))
            } else {
                appendFlag(this@setBloatFlags.context.getString(R.string.disabled))
            }

            when (bloat.removal.method) {
                Removal.ADVANCED.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.advanced))
                }
                Removal.EXPERT.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.expert))
                }
                Removal.RECOMMENDED.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.recommended))
                }
                Removal.UNSAFE.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.unsafe))
                }
            }
        }
    }

    companion object {
        interface AdapterDebloatCallback {
            fun onBloatSelected(bloat: Bloat)
        }
    }
}
