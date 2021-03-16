package app.simple.inure.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.AppIconExtensions.loadAppIcon
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import java.util.*

class AppsAdapter(private val appsAdapterCallbacks: AppsAdapterCallbacks) : RecyclerView.Adapter<AppsAdapter.Holder>() {

    var apps = arrayListOf<ApplicationInfo>()
    var searchKeyword: String = ""
    private var xOff = 0f
    private var yOff = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_all_apps, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = "app_$position"
        holder.icon.loadAppIcon(holder.itemView.context, apps[position].packageName)
        holder.name.text = apps[position].name
        holder.packageId.text = apps[position].packageName

        if(searchKeyword.isNotEmpty()) {
            searchHighlighter(holder.name, holder.itemView.context)
            searchHighlighter(holder.packageId, holder.itemView.context)
        }

        holder.container.setOnClickListener {
            appsAdapterCallbacks.onAppClicked(apps[position], holder.icon)
        }

        holder.container.setOnTouchListener { _, event ->
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    xOff = event.x
                    yOff = event.y
                }
            }
            false
        }

        holder.container.setOnLongClickListener {
            appsAdapterCallbacks.onMenuClicked(apps[position], holder.container, xOff, yOff, holder.icon)
            true
        }
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        GlideApp.with(holder.icon).clear(holder.icon)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    private fun searchHighlighter(textView: TextView, context: Context) {
        val string = textView.text.toString()
        val sb = SpannableStringBuilder(string)
        val startPos = string.toLowerCase(Locale.getDefault()).indexOf(searchKeyword.toLowerCase(Locale.getDefault()))
        val endPos = startPos + searchKeyword.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(ContextCompat.getColor(context, R.color.appAccent)))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            sb.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        textView.text = sb
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_all_app_icon)
        val name: TextView = itemView.findViewById(R.id.adapter_all_app_name)
        val packageId: TextView = itemView.findViewById(R.id.adapter_all_app_package_id)
        val container: ConstraintLayout = itemView.findViewById(R.id.adapter_all_app_container)
    }
}
