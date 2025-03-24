package app.simple.inure.adapters.installer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.BulletTextView
import app.simple.inure.models.Triple
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ConditionUtils.invert

class AdapterInstallerChanges(private val list: ArrayList<Triple<String, String, String>>) : RecyclerView.Adapter<AdapterInstallerChanges.Holder>() {

    var isDiffStyle = InstallerPreferences.isDiffStyleChanges()
        set(value) {
            field = value
            list.forEachIndexed { index, _ ->
                notifyItemChanged(index)
            }
        }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.title)
        val added: BulletTextView = itemView.findViewById(R.id.added)
        val removed: BulletTextView = itemView.findViewById(R.id.removed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_installer_changes, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.title.text = list[position].first()

        if (isDiffStyle) {
            holder.added.text = list[position].second().lines().joinToString("\n") { "+ $it" }
            holder.removed.text = list[position].third().lines().joinToString("\n") { "- $it" }
        } else {
            holder.added.text = list[position].second()
            holder.removed.text = list[position].third()
        }

        if (isDiffStyle.invert()) {
            ContextCompat.getDrawable(holder.context, R.drawable.ic_check_12dp)?.let { holder.added.setBulletDrawable(it) }
            ContextCompat.getDrawable(holder.context, R.drawable.ic_close_12dp)?.let { holder.removed.setBulletDrawable(it) }
            holder.added.setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
            holder.removed.setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
        } else {
            holder.added.setTextColor(0xFF16A085.toInt())
            holder.removed.setTextColor(0xFFCB4335.toInt())
            holder.added.removeBulletDrawable()
            holder.removed.removeBulletDrawable()
        }
    }
}