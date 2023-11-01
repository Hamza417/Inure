package app.simple.inure.adapters.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.BatchProfile
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.SortBatch

class AdapterBatchProfiles(private val names: ArrayList<BatchProfile>) : RecyclerView.Adapter<AdapterBatchProfiles.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_batch_profiles, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = names[position].profileName

        holder.sortStyle.text = buildString {
            with(names[position]) {
                when (sortStyle) {
                    SortBatch.NAME -> append(holder.getString(R.string.name))
                    SortBatch.SIZE -> append(holder.getString(R.string.size))
                    SortBatch.INSTALL_DATE -> append(holder.getString(R.string.install_date))
                    SortBatch.MIN_SDK -> append(holder.getString(R.string.minimum_sdk))
                    SortBatch.TARGET_SDK -> append(holder.getString(R.string.target_sdk))
                    SortBatch.UPDATE_DATE -> append(holder.getString(R.string.update_date))
                    SortBatch.PACKAGE_NAME -> append(holder.getString(R.string.package_name))
                }

                append(" | ")

                if (names[position].isReversed) {
                    append(holder.getString(R.string.descending))
                } else {
                    append(holder.getString(R.string.ascending))
                }
            }
        }

        holder.filters.text = buildString {
            with(names[position]) {
                when (appType) {
                    SortConstant.BOTH -> append(holder.getString(R.string.all))
                    SortConstant.SYSTEM -> append(holder.getString(R.string.system))
                    SortConstant.USER -> append(holder.getString(R.string.user))
                }

                if (FlagUtils.isFlagSet(filterStyle, SortConstant.BATCH_SELECTED)) {
                    append(" | ")
                    append(holder.getString(R.string.selected))
                }

                if (FlagUtils.isFlagSet(filterStyle, SortConstant.BATCH_NOT_SELECTED)) {
                    append(" | ")
                    append(holder.getString(R.string.not_selected))
                }

                if (FlagUtils.isFlagSet(filterStyle, SortConstant.BATCH_DISABLED)) {
                    append(" | ")
                    append(holder.getString(R.string.disabled))
                }

                if (FlagUtils.isFlagSet(filterStyle, SortConstant.BATCH_ENABLED)) {
                    append(" | ")
                    append(holder.getString(R.string.enabled))
                }
            }
        }

        holder.container.setOnClickListener {
            BatchPreferences.setAppsFilter(names[position].filterStyle)
            BatchPreferences.setSortStyle(names[position].sortStyle)
            BatchPreferences.setReverseSorting(names[position].isReversed)
            BatchPreferences.setLastSelectedProfile(names[position].id)
        }
    }

    override fun getItemCount(): Int {
        return names.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val sortStyle: TypeFaceTextView = itemView.findViewById(R.id.sort_style)
        val filters: TypeFaceTextView = itemView.findViewById(R.id.filters)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
    }
}