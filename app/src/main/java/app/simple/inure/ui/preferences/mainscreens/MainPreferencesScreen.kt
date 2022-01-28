package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterPreferences
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.PreferencesViewModel

class MainPreferencesScreen : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterPreferences: AdapterPreferences

    private val preferencesViewModel: PreferencesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preference, container, false)

        recyclerView = view.findViewById(R.id.preferences_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesViewModel.getPreferences().observe(viewLifecycleOwner) {

            postponeEnterTransition()

            adapterPreferences = AdapterPreferences(it)

            adapterPreferences.setOnPreferencesCallbackListener(object : AdapterPreferences.Companion.PreferencesCallbacks {
                override fun onPrefsClicked(imageView: ImageView, category: String) {
                    when (category) {
                        getString(R.string.appearance) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              AppearanceScreen.newInstance(),
                                                              imageView,
                                                              "appearance_prefs")
                        }
                        getString(R.string.behaviour) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              BehaviourScreen.newInstance(),
                                                              imageView,
                                                              "behaviour_prefs")
                        }
                        getString(R.string.configuration) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              ConfigurationScreen.newInstance(),
                                                              imageView,
                                                              "config_prefs")
                        }
                        getString(R.string.terminal) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              TerminalScreen.newInstance(),
                                                              imageView,
                                                              "terminal_prefs")
                        }
                        getString(R.string.shell_preferences) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              ShellScreen.newInstance(),
                                                              imageView,
                                                              "shell_prefs")
                        }
                        getString(R.string.accessibility) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              AccessibilityScreen.newInstance(),
                                                              imageView,
                                                              "accessibility_prefs")
                        }
                        getString(R.string.about) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              AboutScreen.newInstance(),
                                                              imageView,
                                                              "about_prefs")
                        }
                    }
                }
            })

            recyclerView.adapter = adapterPreferences

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
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
