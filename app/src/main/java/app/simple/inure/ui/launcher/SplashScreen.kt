package app.simple.inure.ui.launcher

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.transitions.TransitionManager
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.app.Apps
import app.simple.inure.viewmodels.AppData

class SplashScreen : ScopedFragment() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appData: AppData by viewModels()

        appData.getAppData().observe(requireActivity(), {
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("all_apps")
                ?: Apps.newInstance()

            exitTransition = TransitionManager.getEnterTransitions(TransitionManager.FADE)
            fragment.sharedElementEnterTransition = DetailsTransitionArc(1.5F)
            fragment.enterTransition = TransitionManager.getExitTransition(TransitionManager.FADE)
            fragment.sharedElementReturnTransition = DetailsTransitionArc(1.5F)

            requireActivity().supportFragmentManager.beginTransaction()
                    .addSharedElement(view.findViewById(R.id.imageView), "main_app_icon")
                    .replace(R.id.app_container, fragment, "all_apps")
                    .commit()
        })
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        /* no-op */
    }

    private fun openAllAppsFragment() {

    }

    companion object {
        fun newInstance(): SplashScreen {
            val args = Bundle()
            val fragment = SplashScreen()
            fragment.arguments = args
            return fragment
        }
    }
}