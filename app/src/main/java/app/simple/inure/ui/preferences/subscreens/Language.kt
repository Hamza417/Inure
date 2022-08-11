package app.simple.inure.ui.preferences.subscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterLanguage
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences

class Language : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private val adapterLanguage = AdapterLanguage()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sub_preference_language, container, false)

        recyclerView = view.findViewById(R.id.language_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        recyclerView.adapter = adapterLanguage
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ConfigurationPreferences.language -> {
                requireActivity().recreate() // update the language in context wrapper
            }
        }
    }

    companion object {
        fun newInstance(): Language {
            val args = Bundle()
            val fragment = Language()
            fragment.arguments = args
            return fragment
        }
    }
}