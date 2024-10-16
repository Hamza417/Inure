package app.simple.inure.adapters.viewers

import android.content.pm.FeatureInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.FeaturesUtils
import app.simple.inure.apk.utils.FeaturesUtils.getProperName
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.ui.viewers.Features
import app.simple.inure.util.StringUtils.appendFlag

class AdapterFeatures(private val features: MutableList<FeatureInfo>) : RecyclerView.Adapter<AdapterFeatures.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_features, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (features[position].name.isNullOrEmpty()) {
            holder.name.text = MetaUtils.getOpenGL(features[position].reqGlEsVersion)
        } else {
            holder.name.text = features[position].name
        }

        holder.required.text = buildString {
            if (features[position].name.isNullOrEmpty()) {
                appendFlag(holder.itemView.context.getString(R.string.graphics))
            } else {
                appendFlag(features[position].getProperName()
                               ?: holder.itemView.context.getString(R.string.unknown))
            }

            if (features[position].flags and FeatureInfo.FLAG_REQUIRED != 0) {
                appendFlag(holder.itemView.context.getString(R.string.required))
            } else {
                appendFlag(holder.itemView.context.getString(R.string.not_required))
            }

            try {
                if (PackageUtils.isFeatureSupported(holder.context, features[position].name)) {
                    appendFlag(holder.itemView.context.getString(R.string.supported))
                } else {
                    appendFlag(holder.itemView.context.getString(R.string.not_supported))
                }
            } catch (e: NullPointerException) {
                if (FeaturesUtils.isOpenGLVersionSupported(holder.context, features[position].reqGlEsVersion)) {
                    appendFlag(holder.itemView.context.getString(R.string.supported))
                } else {
                    appendFlag(holder.itemView.context.getString(R.string.not_supported))
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (features[position].version != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
                    appendFlag(features[position].version.toString())
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return features.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_features_name)
        val required: TypeFaceTextView = itemView.findViewById(R.id.adapter_features_required)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_features_container)
    }

}
