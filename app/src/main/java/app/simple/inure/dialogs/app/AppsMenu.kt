package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen

class AppsMenu : ScopedBottomSheetFragment() {

    private lateinit var openAppsSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_main_settings, container, false)

        openAppsSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        openAppsSettings.setOnClickListener {
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("main_preferences_screen")
                ?: MainPreferencesScreen.newInstance()

            requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.app_container, fragment, "main_preferences_screen")
                    .addToBackStack(tag)
                    .commit()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(): AppsMenu {
            val args = Bundle()
            val fragment = AppsMenu()
            fragment.arguments = args
            return fragment
        }
    }
}
