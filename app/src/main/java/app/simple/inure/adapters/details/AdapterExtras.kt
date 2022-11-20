package app.simple.inure.adapters.details

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.StringUtils.highlightExtensions
import app.simple.inure.util.StringUtils.optimizeToColoredString


class AdapterExtras(var list: MutableList<String>, var keyword: String) : RecyclerView.Adapter<AdapterExtras.Holder>(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var extrasCallbacks: ExtrasCallbacks
    private var isHighlighted = ExtrasPreferences.isExtensionsHighlighted()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_resources, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.extra.text = if (isHighlighted) {
            list[position].optimizeToColoredString("/").highlightExtensions()
        } else {
            list[position].optimizeToColoredString("/")
        }

        list[position].optimizeToColoredString("...") // fade ellipsis maybe?

        holder.extra.setOnClickListener {
            extrasCallbacks.onExtrasClicked(list[position])
        }

        holder.extra.setOnLongClickListener {
            extrasCallbacks.onExtrasLongClicked(list[position])
            true
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.extra, keyword)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ExtrasPreferences.highlight -> {
                isHighlighted = ExtrasPreferences.isExtensionsHighlighted().also {
                    notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: MutableList<String>, keyword: String) {
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
        val extra: DynamicRippleTextView = itemView.findViewById(R.id.adapter_resources_name)
    }

    interface ExtrasCallbacks {
        fun onExtrasClicked(path: String)
        fun onExtrasLongClicked(path: String)
    }
}