package app.simple.inure.dialogs.appearance

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.AdapterTypeFace
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.MainPreferences

class AppearanceTypeFace : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterTypeFace: AdapterTypeFace

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_typeface, container, false)

        recyclerView = view.findViewById(R.id.typeface_recycler_view)
        adapterTypeFace = AdapterTypeFace()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterTypeFace.setOnTypeFaceClickListener {
            AppearancePreferences.setAppFont(it)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapterTypeFace
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == AppearancePreferences.appFont) {
            requireActivity().recreate()
        }
    }

    companion object {
        fun newInstance(): AppearanceTypeFace {
            val args = Bundle()
            val fragment = AppearanceTypeFace()
            fragment.arguments = args
            return fragment
        }
    }
}
