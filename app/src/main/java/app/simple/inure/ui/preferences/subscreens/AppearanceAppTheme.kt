package app.simple.inure.ui.preferences.subscreens

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterTheme
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ThemeUtils

class AppearanceAppTheme : ScopedFragment(), ThemeChangedListener {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_theme, container, false)

        recyclerView = view.findViewById(R.id.app_theme_recycler_view)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        recyclerView.adapter = AdapterTheme()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.theme -> {
                handler.postDelayed({ ThemeUtils.setAppTheme(resources) }, 25)
            }
        }
    }

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        // adapterTheme.notifyItemChanged(0)
    }

    override fun onResume() {
        super.onResume()
        ThemeManager.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeListener(this)
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