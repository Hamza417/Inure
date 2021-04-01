package app.simple.inure.adapters

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.animatedbackground.DynamicRippleConstraintLayout
import app.simple.inure.decorations.animatedbackground.DynamicRippleImageButton
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_HEADER
import app.simple.inure.decorations.viewholders.VerticalListViewHolder.Companion.TYPE_ITEM
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.AppIconExtensions.loadAppIcon
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.util.FileSizeHelper.getFileSize

class AppsAdapterSmall : RecyclerView.Adapter<VerticalListViewHolder>() {

    var apps = arrayListOf<ApplicationInfo>()
    private lateinit var appsAdapterCallbacks: AppsAdapterCallbacks
    private var xOff = 0f
    private var yOff = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_all_apps_header, parent, false))
            }
            TYPE_ITEM -> {
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
            holder.icon.transitionName = "app_$position"
            holder.icon.loadAppIcon(holder.itemView.context, apps[position].packageName)
            holder.name.text = apps[position].name
            holder.packageId.text = apps[position].packageName

            holder.packageType.text = if ((apps[position].flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                holder.packageType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person, 0, 0, 0)
                holder.itemView.context.getString(R.string.user)
            } else {
                holder.packageType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_android, 0, 0, 0)
                holder.itemView.context.getString(R.string.system)
            }

            holder.packageSize.text = apps[position].sourceDir.getFileSize()

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
                appsAdapterCallbacks.onAppLongPress(apps[position], holder.container, xOff, yOff, holder.icon)
                true
            }
        }

        if (holder is Header) {
            holder.search.setOnClickListener {
                appsAdapterCallbacks.onSearchPressed(it)
            }

            holder.settings.setOnClickListener {
                appsAdapterCallbacks.onSettingsPressed()
            }

            holder.prefs.setOnClickListener {
                appsAdapterCallbacks.onPrefsIconPressed(it)
            }
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            GlideApp.with(holder.icon).clear(holder.icon)
        }
        if (holder is Header) {
            Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
        }
    }

    override fun getItemCount(): Int {
        return apps.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else TYPE_ITEM
    }

    fun setOnItemClickListener(appsAdapterCallbacks: AppsAdapterCallbacks) {
        this.appsAdapterCallbacks = appsAdapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_all_app_icon)
        val name: TextView = itemView.findViewById(R.id.adapter_all_app_name)
        val packageId: TextView = itemView.findViewById(R.id.adapter_all_app_package_id)
        val packageSize: TextView = itemView.findViewById(R.id.adapter_all_app_package_size)
        val packageType: TextView = itemView.findViewById(R.id.adapter_all_app_type)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_all_app_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.imageView3)
        private val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val search: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_search_button)
        val settings: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_configuration_button)
        val prefs: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_pref_button)

        init {
            Handler(Looper.getMainLooper()).postDelayed({ (appIcon.drawable as AnimatedVectorDrawable).start() }, 1000L)
            total.text = String.format(itemView.context.getString(R.string.apps), apps.size)
        }
    }
}
