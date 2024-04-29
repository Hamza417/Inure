package app.simple.inure.ui.preferences.subscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterLanguage
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser

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

        adapterLanguage.setLanguageCallback(object : AdapterLanguage.Companion.LanguageCallback {
            override fun onCreditsClicked() {
                openWebPage(getString(R.string.translate))
            }

            override fun onParticipateClicked() {
                CROWDIN_LINK.asUri().openInBrowser(requireContext())
            }
        })
    }

    companion object {
        fun newInstance(): Language {
            val args = Bundle()
            val fragment = Language()
            fragment.arguments = args
            return fragment
        }

        private const val CROWDIN_LINK = "https://crowdin.com/project/inure"

        const val TAG = "Language"
    }
}
