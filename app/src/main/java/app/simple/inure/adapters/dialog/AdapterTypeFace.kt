package app.simple.inure.adapters.dialog

import android.content.res.Resources
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.interfaces.adapters.AdapterTypeFaceCallbacks
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils.makeGoAway
import app.simple.inure.util.ViewUtils.makeVisible

class AdapterTypeFace : RecyclerView.Adapter<AdapterTypeFace.Holder>() {

    private lateinit var adapterTypeFaceCallbacks: AdapterTypeFaceCallbacks
    private var list = TypeFace.list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        list.sortBy {
            it.typefaceName
        }
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_type_face, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        try {
            holder.textView.typeface = ResourcesCompat.getFont(holder.itemView.context, list[position].typeFaceResId)
        } catch (e: Resources.NotFoundException) {
            holder.textView.typeface = Typeface.DEFAULT_BOLD
        }

        holder.textView.text = list[position].typefaceName

        if (AppearancePreferences.getAppFont() == list[position].name) {
            holder.icon.makeVisible()
        } else {
            holder.icon.makeGoAway()
        }

        holder.container.setOnClickListener {
            adapterTypeFaceCallbacks.onTypeFaceClicked(list[position].name)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnTypeFaceClickListener(adapterTypeFaceCallbacks: AdapterTypeFaceCallbacks) {
        this.adapterTypeFaceCallbacks = adapterTypeFaceCallbacks
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.adapter_typeface_textview)
        val icon: ImageView = itemView.findViewById(R.id.adapter_typeface_check_icon)
        val container: LinearLayout = itemView.findViewById(R.id.adapter_typeface_container)
    }
}
