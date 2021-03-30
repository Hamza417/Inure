package app.simple.inure.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.decorations.animatedbackground.AnimatedBackgroundLinearLayout
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.transitions.TransitionManager

class MainPreferencesScreen : Fragment() {

    private lateinit var appearance: AnimatedBackgroundLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preference, container, false)

        appearance = view.findViewById(R.id.frag_pref_appearances)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appearance.setOnClickListener {
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("appearance_preferences_screen")
                ?: AppearanceScreen.newInstance()

            exitTransition = TransitionManager.getEnterTransitions(TransitionManager.FADE)
            fragment.sharedElementEnterTransition = DetailsTransitionArc(1.5F)
            fragment.enterTransition = TransitionManager.getExitTransition(TransitionManager.FADE)
            fragment.sharedElementReturnTransition = DetailsTransitionArc(1.2F)

            requireActivity().supportFragmentManager.beginTransaction()
                    .addSharedElement(view.findViewById(R.id.appearance_icon), "appearance_icon")
                    .replace(R.id.app_container, fragment, "appearance_preferences_screen")
                    .addToBackStack(tag)
                    .commit()
        }
    }

    companion object {
        fun newInstance(): MainPreferencesScreen {
            val args = Bundle()
            val fragment = MainPreferencesScreen()
            fragment.arguments = args
            return fragment
        }
    }
}
