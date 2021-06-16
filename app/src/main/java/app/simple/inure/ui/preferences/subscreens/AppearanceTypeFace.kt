package app.simple.inure.ui.preferences.subscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterTypeFace
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences

class AppearanceTypeFace : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterTypeFace: AdapterTypeFace

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_typeface, container, false)

        recyclerView = view.findViewById(R.id.typeface_recycler_view)
        adapterTypeFace = AdapterTypeFace()

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterTypeFace.setOnTypeFaceClickListener {
            AppearancePreferences.setAppFont(it)
        }

        recyclerView.adapter = adapterTypeFace
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
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
