package app.simple.inure.ui.preferences

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.transitions.TransitionManager
import app.simple.inure.util.FragmentHelper

class MainPreferencesScreen : Fragment() {

    private lateinit var appearance: DynamicRippleLinearLayout
    private lateinit var configuration: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preference, container, false)

        appearance = view.findViewById(R.id.frag_pref_appearances)
        configuration = view.findViewById(R.id.frag_pref_config)

        // (view.findViewById<ImageView>(R.id.preferences_header_icon).drawable as AnimatedVectorDrawable).start()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appearance.setOnClickListener {
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("appearance_preferences_screen")
                ?: AppearanceScreen.newInstance()

            exitTransition = TransitionManager.getEnterTransitions(TransitionManager.FADE)
            fragment.sharedElementEnterTransition = DetailsTransitionArc()
            fragment.enterTransition = TransitionManager.getExitTransition(TransitionManager.FADE)
            fragment.sharedElementReturnTransition = DetailsTransitionArc()

            requireActivity().supportFragmentManager.beginTransaction()
                    .addSharedElement(view.findViewById(R.id.appearance_icon), "appearance_icon")
                    .replace(R.id.app_container, fragment, "appearance_preferences_screen")
                    .addToBackStack(fragment.tag)
                    .commit()
        }

        configuration.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        ConfigurationScreen.newInstance(),
                                        view.findViewById(R.id.config_icon),
                                        tag!!)
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
