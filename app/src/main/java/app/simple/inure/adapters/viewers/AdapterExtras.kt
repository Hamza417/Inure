package app.simple.inure.adapters.viewers

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.Extra
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.StringUtils.highlightExtensions
import app.simple.inure.util.StringUtils.optimizeToColoredString

class AdapterExtras(var list: MutableList<Extra>, var keyword: String) : RecyclerView.Adapter<AdapterExtras.Holder>(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var extrasCallbacks: ExtrasCallbacks
    private var isHighlighted = ExtrasPreferences.isExtensionsHighlighted()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_extras, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = list[position].name.highlightExtensions(isHighlighted) // Bad optimization??
        holder.path.text = list[position].path
        holder.size.text = list[position].size.toSize()

        list[position].path.optimizeToColoredString("...") // fade ellipsis maybe?

        holder.container.setOnClickListener {
            extrasCallbacks.onExtrasClicked(list[position])
        }

        holder.container.setOnLongClickListener {
            extrasCallbacks.onExtrasLongClicked(list[position])
            true
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
            AdapterUtils.searchHighlighter(holder.path, keyword)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ExtrasPreferences.HIGHLIGHT -> {
                isHighlighted = ExtrasPreferences.isExtensionsHighlighted().also {
                    notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: MutableList<Extra>, keyword: String) {
        this.keyword = keyword
        this.list = list
        notifyDataSetChanged()
    }

    fun unregister() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    fun setOnResourceClickListener(resourceCallbacks: ExtrasCallbacks) {
        this.extrasCallbacks = resourceCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val path: TypeFaceTextView = itemView.findViewById(R.id.path)
        val size: TypeFaceTextView = itemView.findViewById(R.id.size)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)

        init {
            name.enableSelection()
        }
    }

    interface ExtrasCallbacks {
        fun onExtrasClicked(extra: Extra)
        fun onExtrasLongClicked(extra: Extra)
    }
}
