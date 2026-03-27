package app.simple.inure.adapters.batch

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.util.ImageLoader.loadAPKIcon
import app.simple.inure.models.BatchInstallerInfo
import app.simple.inure.models.BatchInstallerInfo.InstallState
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager

/**
 * RecyclerView adapter for the Batch Installer screen.
 *
 * Inflates [R.layout.adapter_all_apps_small_details] and binds each
 * [BatchInstallerInfo] entry, displaying the APK icon, app name,
 * package name, and current [InstallState] with color coding.
 *
 * @param results The initial list of [BatchInstallerInfo] entries.
 *
 * @author Hamza417
 */
class AdapterBatchInstaller(
        private val results: ArrayList<BatchInstallerInfo>
) : RecyclerView.Adapter<AdapterBatchInstaller.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_all_apps_small_details, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val info = results[position]

        // For split-APK bundles the icon must be loaded from the base APK extracted from the
        // archive, not the bundle itself (which has no AndroidManifest.xml at its root).
        val iconFile = info.apkFiles.firstOrNull() ?: info.file
        holder.icon.loadAPKIcon(iconFile)
        holder.name.text = info.appName
        holder.packageId.text = info.packageName

        val (textRes, color) = when (info.installState) {
            InstallState.PENDING -> Pair(
                    R.string.pending,
                    ThemeManager.theme.textViewTheme.secondaryTextColor
            )
            InstallState.INSTALLING -> Pair(
                    R.string.installing,
                    AppearancePreferences.getAccentColor()
            )
            InstallState.INSTALLED -> Pair(
                    R.string.installed,
                    AppearancePreferences.getAccentColor()
            )
            InstallState.FAILED -> Pair(
                    R.string.failed,
                    Color.RED
            )
        }

        holder.details.text = holder.itemView.context.getString(textRes)
        holder.details.setTextColor(color)
    }

    override fun getItemCount(): Int = results.size

    /**
     * Diffs [newResults] against the adapter's own internal list and notifies only the
     * changed positions. Because the ViewModel emits a fresh immutable snapshot on every
     * state change and the adapter holds its own independent copy (initialized via
     * `ArrayList(snapshot)`), the comparison always reflects true deltas.
     *
     * @param newResults The latest immutable snapshot from [BatchInstallerViewModel.installList].
     */
    fun updateResults(newResults: List<BatchInstallerInfo>) {
        for (i in results.indices) {
            if (i < newResults.size && results[i].installState != newResults[i].installState) {
                results[i] = newResults[i]
                notifyItemChanged(i)
            }
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val details: TypeFaceTextView = itemView.findViewById(R.id.details)
    }
}

