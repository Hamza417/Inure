package app.simple.inure.adapters.viewers

import android.annotation.SuppressLint
import android.app.ApplicationExitInfo
import android.content.Context
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.DateUtils.toDate

class AdapterRecentExits(
        private val list: List<ApplicationExitInfo>,
        private val packageInfo: PackageInfo) : RecyclerView.Adapter<AdapterRecentExits.Holder>() {

    private var adapterRecentExitsListener: AdapterRecentExitsListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_recent_exits, parent, false))
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val exitReason = list[position]

        /**
         * Reasons:
         *         REASON_UNKNOWN,
         *         REASON_EXIT_SELF,
         *         REASON_SIGNALED,
         *         REASON_LOW_MEMORY,
         *         REASON_CRASH,
         *         REASON_CRASH_NATIVE,
         *         REASON_ANR,
         *         REASON_INITIALIZATION_FAILURE,
         *         REASON_PERMISSION_CHANGE,
         *         REASON_EXCESSIVE_RESOURCE_USAGE,
         *         REASON_USER_REQUESTED,
         *         REASON_USER_STOPPED,
         *         REASON_DEPENDENCY_DIED,
         *         REASON_OTHER,
         *         REASON_FREEZER,
         *         REASON_PACKAGE_STATE_CHANGE,
         *         REASON_PACKAGE_UPDATED,
         */
        holder.reason.text = when (exitReason.reason) {
            ApplicationExitInfo.REASON_USER_REQUESTED -> "User Requested/Force Stopped"
            ApplicationExitInfo.REASON_SIGNALED -> "Killed by Signal (SIGKILL)"
            ApplicationExitInfo.REASON_CRASH -> "App Crashed"
            ApplicationExitInfo.REASON_ANR -> "App Not Responding (ANR)"
            ApplicationExitInfo.REASON_LOW_MEMORY -> "Low Memory"
            ApplicationExitInfo.REASON_USER_STOPPED -> "User Stopped"
            ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "Excessive Resource Usage"
            ApplicationExitInfo.REASON_EXIT_SELF -> "Self Exit"
            ApplicationExitInfo.REASON_PACKAGE_UPDATED -> "Package Updated"
            ApplicationExitInfo.REASON_PACKAGE_STATE_CHANGE -> "Package State Change"
            ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "Permission Change"
            ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "Dependency Died"
            ApplicationExitInfo.REASON_CRASH_NATIVE -> "Native Crash"
            ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "Initialization Failure"
            ApplicationExitInfo.REASON_FREEZER -> "Freezer"
            ApplicationExitInfo.REASON_UNKNOWN -> "Unknown"
            else -> "Other (${exitReason.reason})"
        }

        holder.details.text = exitReason.getDescription(holder.itemView.context)
        holder.timestamp.text = exitReason.timestamp.toDate()

        holder.container.setOnClickListener {
            adapterRecentExitsListener?.onExitInfoClicked(exitReason.toString())
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
        val reason: TypeFaceTextView = itemView.findViewById(R.id.reason)
        val details: TypeFaceTextView = itemView.findViewById(R.id.details)
        val timestamp: TypeFaceTextView = itemView.findViewById(R.id.timestamp)
    }

    @SuppressLint("NewApi")
    private fun ApplicationExitInfo.getDescription(context: Context): String {
        var description = this.description

        if (description.isNullOrEmpty()) {
            return context.getString(R.string.desc_not_available)
        }

        description = description.replace(oldValue = packageInfo.packageName,
                                          newValue = packageInfo.applicationInfo?.name ?: packageInfo.packageName)

        return description
    }

    fun setAdapterRecentExitsListener(listener: AdapterRecentExitsListener) {
        this.adapterRecentExitsListener = listener
    }

    companion object {
        const val TAG = "AdapterRecentExits"

        interface AdapterRecentExitsListener {
            fun onExitInfoClicked(exitInfo: String)
        }
    }
}
