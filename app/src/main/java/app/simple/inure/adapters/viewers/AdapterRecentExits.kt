package app.simple.inure.adapters.viewers

import android.app.ApplicationExitInfo
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.ExitReason
import app.simple.inure.util.DateUtils.toDate

class AdapterRecentExits(private val list: List<ExitReason>, private val packageInfo: PackageInfo) : RecyclerView.Adapter<AdapterRecentExits.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_recent_exits, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val exitReason = list[position]

        val reason = when (exitReason.reason) {
            ApplicationExitInfo.REASON_USER_REQUESTED -> {
                "Force Stopped"
            }
            ApplicationExitInfo.REASON_SIGNALED -> {
                "Killed by Signal (SIGKILL)"
            }
            ApplicationExitInfo.REASON_CRASH -> {
                "Crash"
            }
            ApplicationExitInfo.REASON_ANR -> {
                "App Not Responding (ANR)"
            }
            ApplicationExitInfo.REASON_LOW_MEMORY -> {
                "Low Memory"
            }
            ApplicationExitInfo.REASON_USER_STOPPED -> {
                "User Stopped"
            }
            ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> {
                "Excessive Resource Usage"
            }
            else -> {
                "Other (${exitReason.reason})"
            }
        }

        holder.title.text = reason
        holder.data.text = exitReason.details.ifEmpty {
            holder.getString(R.string.desc_not_available)
        }.replace(
                oldValue = packageInfo.packageName,
                newValue = packageInfo.applicationInfo?.name ?: packageInfo.packageName
        )

        holder.timestamp.text = exitReason.timestamp.toDate()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
        val title: TypeFaceTextView = itemView.findViewById(R.id.title)
        val data: TypeFaceTextView = itemView.findViewById(R.id.details)
        val timestamp: TypeFaceTextView = itemView.findViewById(R.id.timestamp)
    }
}
