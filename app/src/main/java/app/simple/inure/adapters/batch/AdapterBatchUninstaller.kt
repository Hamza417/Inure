package app.simple.inure.adapters.batch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.batch.BatchUninstaller
import app.simple.inure.glide.util.ImageLoader.loadAppIcon

class AdapterBatchUninstaller(private val results: ArrayList<BatchUninstaller.Companion.BatchUninstallerResult>) : RecyclerView.Adapter<AdapterBatchUninstaller.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_batch_uninstaller, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val result = results[position]

        holder.icon.loadAppIcon(results[position].packageInfo.packageName, false)

        runCatching {
            holder.name.text = result.packageInfo.safeApplicationInfo.name
        }.onFailure {
            holder.name.text = result.packageInfo.packageName
        }

        holder.result.text = when (result.isSuccessful) {
            null -> holder.itemView.context.getString(R.string.pending)
            true -> holder.itemView.context.getString(R.string.uninstalled)
            false -> holder.itemView.context.getString(R.string.failed)
        }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    fun updateResults(newResults: ArrayList<BatchUninstaller.Companion.BatchUninstallerResult>) {
        // Find which items changed and notify only those
        for (i in results.indices) {
            if (i < newResults.size && results[i].isSuccessful != newResults[i].isSuccessful) {
                results[i] = newResults[i]
                notifyItemChanged(i)
            }
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.app_name)
        val result: TypeFaceTextView = itemView.findViewById(R.id.result)
    }
}