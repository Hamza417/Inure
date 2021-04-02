package app.simple.inure.adapters

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
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.interfaces.adapters.AdapterTypeFaceCallbacks
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ViewUtils.makeGoAway
import app.simple.inure.util.ViewUtils.makeVisible

class AdapterTypeFace : RecyclerView.Adapter<AdapterTypeFace.Holder>() {

    private lateinit var adapterTypeFaceCallbacks: AdapterTypeFaceCallbacks

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

    companion object {
        val list = arrayListOf(
            TypeFaceList("Auto (System Default)", 0, TypeFaceTextView.AUTO),
            TypeFaceList("Lato", R.font.lato_bold, TypeFaceTextView.LATO),
            TypeFaceList("Plus Jakarta Sans", R.font.plus_jakarta_bold, TypeFaceTextView.PLUS_JAKARTA),
            TypeFaceList("Mulish", R.font.mulish_bold, TypeFaceTextView.MULISH),
            TypeFaceList("Jost", R.font.jost_bold, TypeFaceTextView.JOST),
            TypeFaceList("Epilogue", R.font.epilogue_bold, TypeFaceTextView.EPILOGUE),
            TypeFaceList("Ubuntu", R.font.ubuntu_bold, TypeFaceTextView.UBUNTU),
            TypeFaceList("Poppins", R.font.poppins_bold, TypeFaceTextView.POPPINS),
            TypeFaceList("Manrope", R.font.manrope_bold, TypeFaceTextView.MANROPE),
            TypeFaceList("Inter", R.font.inter_bold, TypeFaceTextView.INTER),
            TypeFaceList("Overpass", R.font.overpass_bold, TypeFaceTextView.OVERPASS),
            TypeFaceList("Urbanist", R.font.urbanist_bold, TypeFaceTextView.URBANIST),
            TypeFaceList("Nunito", R.font.nunito_bold, TypeFaceTextView.NUNITO),
            TypeFaceList("Oswald", R.font.oswald_bold, TypeFaceTextView.OSWALD),
            TypeFaceList("Roboto", R.font.roboto_bold, TypeFaceTextView.ROBOTO),
            TypeFaceList("Reforma", R.font.reforma_negra, TypeFaceTextView.REFORMA),
            TypeFaceList("Subjectivity", R.font.subjectivity_bold, TypeFaceTextView.SUBJECTIVITY),
            TypeFaceList("Mohave", R.font.mohave_bold, TypeFaceTextView.MOHAVE),
            TypeFaceList("Yessica", R.font.yessica_bold, TypeFaceTextView.YESSICA),
            TypeFaceList("Audrey", R.font.audrey_bold, TypeFaceTextView.AUDREY),
            TypeFaceList("Josefin Sans", R.font.josefin_sans_bold, TypeFaceTextView.JOSEFIN),
            TypeFaceList("Comfortaa", R.font.comfortaa_bold, TypeFaceTextView.COMFORTAA),
        )

        class TypeFaceList(val typefaceName: String, val typeFaceResId: Int, val name: String)
    }
}
