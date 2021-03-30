package app.simple.inure.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.interfaces.adapters.AdapterTypeFaceCallbacks
import app.simple.inure.preferences.MainPreferences

class AdapterTypeFace : RecyclerView.Adapter<AdapterTypeFace.Holder>() {

    private lateinit var adapterTypeFaceCallbacks: AdapterTypeFaceCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        list.sortBy {
            it.typefaceName
        }
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_type_face, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.textView.typeface = ResourcesCompat.getFont(holder.itemView.context, list[position].typeFaceResId)
        holder.textView.text = list[position].typefaceName

        if (MainPreferences.getAppFont() == list[position].name) {
            holder.icon.visibility = View.VISIBLE
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

    companion object {
        val list = arrayListOf(
            TypeFaceList("Lato", R.font.lato_bold, TypeFaceTextView.LATO),
            TypeFaceList("Plus Jakarta Sans", R.font.plus_jakarta_bold, TypeFaceTextView.PLUS_JAKARTA),
            TypeFaceList("Mulish", R.font.mulish_bold, TypeFaceTextView.MULISH),
            TypeFaceList("Jost", R.font.jost_bold, TypeFaceTextView.JOST),
            TypeFaceList("Epilogue", R.font.epilogue_bold, TypeFaceTextView.EPILOGUE),
            TypeFaceList("Ubuntu", R.font.ubuntu_bold, TypeFaceTextView.UBUNTU)
        )

        class TypeFaceList(val typefaceName: String, val typeFaceResId: Int, val name: String)
    }
}
