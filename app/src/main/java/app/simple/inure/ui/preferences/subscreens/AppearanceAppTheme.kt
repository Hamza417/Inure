package app.simple.inure.ui.preferences.subscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterTheme
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.interfaces.ThemeRevealCoordinatesListener
import app.simple.inure.util.ThemeUtils

class AppearanceAppTheme : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.terminal_font_size, container, false)

        recyclerView = view.findViewById(R.id.font_size_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        val adapterTheme = AdapterTheme()

        adapterTheme.onTouch = { x: Float, y: Float ->
            (requireActivity() as ThemeRevealCoordinatesListener).onTouchCoordinates(x, y)
        }

        recyclerView.adapter = adapterTheme
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.theme -> {
                handler.postDelayed({ ThemeUtils.setAppTheme(resources) }, 200)
            }
        }
    }

    companion object {
        fun newInstance(): AppearanceAppTheme {
            val args = Bundle()
            val fragment = AppearanceAppTheme()
            fragment.arguments = args
            return fragment
        }
    }
}