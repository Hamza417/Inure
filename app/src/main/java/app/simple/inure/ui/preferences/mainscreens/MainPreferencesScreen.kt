package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.preferences.PreferencesAdapter
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.viewers.PreferencesViewModel

class MainPreferencesScreen : Fragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var preferencesAdapter: PreferencesAdapter

    private val preferencesViewModel: PreferencesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preference, container, false)

        recyclerView = view.findViewById(R.id.preferences_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesViewModel.getPreferences().observe(viewLifecycleOwner, {

            postponeEnterTransition()

            preferencesAdapter = PreferencesAdapter(it)

            preferencesAdapter.setOnPreferencesCallbackListener(object : PreferencesAdapter.Companion.PreferencesCallbacks {
                override fun onPrefsClicked(imageView: ImageView, category: String) {
                    when (category) {
                        getString(R.string.appearance) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        AppearanceScreen.newInstance(),
                                                        imageView,
                                                        "appearance_prefs")
                        }
                        getString(R.string.behaviour) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        BehaviourScreen.newInstance(),
                                                        imageView,
                                                        "behaviour_prefs")
                        }
                        getString(R.string.configuration) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        ConfigurationScreen.newInstance(),
                                                        imageView,
                                                        "config_prefs")
                        }
                    }
                }
            })

            recyclerView.adapter = preferencesAdapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })
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
