package app.simple.inure.adapters.details

import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.TextViewUtils.makeLinks

class AdapterInformation(private val list: ArrayList<Pair<Int, Spannable>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterInformationCallbacks: AdapterInformationCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_divider_preferences, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_information, parent, false))
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        if (holder is Holder) {
            holder.heading.setText(list[position].first)
            holder.data.text = list[position].second

            holder.container.setOnClickListener {
                adapterInformationCallbacks?.onInformationClicked(it, list[position].second.toString())
            }

            if (list[position].first == R.string.apk_base_package || list[position].first == R.string.data || list[position].first == R.string.native_libraries_dir) {
                holder.data.makeLinks(Pair(list[position].second.toString().split("|").first(), View.OnClickListener {
                    val selectedUri: Uri = Uri.parse(list[position].second.split("|").first().toString())
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(selectedUri, "resource/folder")

                    if (intent.resolveActivityInfo(holder.data.context.packageManager, 0) != null) {
                        holder.data.context.startActivity(intent)
                    } else {
                        Log.d("Information", "No file explorer app installed on your device")
                        adapterInformationCallbacks?.onWarning(Warnings.getNoFileExplorerWarning())
                    }
                }))
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].first.isZero()) {
            RecyclerViewUtils.TYPE_DIVIDER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_information_container)
        val heading: TypeFaceTextView = itemView.findViewById(R.id.information_heading)
        val data: TypeFaceTextView = itemView.findViewById(R.id.information_data)

        init {
            heading.enableSelection()
            data.enableSelection()
        }
    }

    inner class Divider(itemView: View) : VerticalListViewHolder(itemView)

    fun setOnAdapterInformationCallbacks(adapterInformationCallbacks: AdapterInformationCallbacks) {
        this.adapterInformationCallbacks = adapterInformationCallbacks
    }

    companion object {
        interface AdapterInformationCallbacks {
            fun onInformationClicked(view: View, string: String)
            fun onWarning(string: String)
        }
    }
}
