package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterDevelopmentPreferences
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment

class DevelopmentScreen : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_development, container, false)

        recyclerView = view.findViewById(R.id.development_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        recyclerView.adapter = AdapterDevelopmentPreferences()
    }

    companion object {
        fun newInstance(): DevelopmentScreen {
            val args = Bundle()
            val fragment = DevelopmentScreen()
            fragment.arguments = args
            return fragment
        }
    }
}