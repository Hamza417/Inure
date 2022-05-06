package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterPreferenceSearch
import app.simple.inure.adapters.preferences.AdapterPreferences
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.PreferencesCallbacks
import app.simple.inure.preferences.PreferencesSearchData
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.PreferencesViewModel

class MainPreferencesScreen : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterPreferences: AdapterPreferences
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner

    private val adapterPreferenceSearch = AdapterPreferenceSearch()
    private val preferencesViewModel: PreferencesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preference, container, false)

        recyclerView = view.findViewById(R.id.preferences_recycler_view)
        search = view.findViewById(R.id.preferences_search_btn)
        searchBox = view.findViewById(R.id.preferences_search)
        title = view.findViewById(R.id.preferences_title)

        searchBoxState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesViewModel.getPreferences().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            adapterPreferences = AdapterPreferences(it)

            adapterPreferences.setOnPreferencesCallbackListener(object : PreferencesCallbacks {
                override fun onPrefsClicked(imageView: ImageView, category: Int, position: Int) {

                    /**
                     * Workaround for shared animation lag
                     */
                    var duration = 500L
                    duration *= position.div(4)
                    if (duration < 250) duration = 400L

                    when (category) {
                        R.string.appearance -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              AppearanceScreen.newInstance(),
                                                              imageView,
                                                              "appearance_prefs", duration)
                        }
                        R.string.behaviour -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              BehaviourScreen.newInstance(),
                                                              imageView,
                                                              "behaviour_prefs", duration)
                        }
                        R.string.configuration -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              ConfigurationScreen.newInstance(),
                                                              imageView,
                                                              "config_prefs", duration)
                        }
                        R.string.formatting -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              FormattingScreen.newInstance(),
                                                              imageView,
                                                              "formatting_prefs", duration)
                        }
                        R.string.terminal -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              TerminalScreen.newInstance(),
                                                              imageView,
                                                              "terminal_prefs", duration)
                        }
                        R.string.shell_preferences -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              ShellScreen.newInstance(),
                                                              imageView,
                                                              "shell_prefs", duration)
                        }
                        R.string.accessibility -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              AccessibilityScreen.newInstance(),
                                                              imageView,
                                                              "accessibility_prefs", duration)
                        }
                        R.string.development -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              DevelopmentScreen.newInstance(),
                                                              imageView,
                                                              "development_prefs", duration)
                        }
                        R.string.about -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              AboutScreen.newInstance(),
                                                              imageView,
                                                              "about_prefs", duration)
                        }
                    }
                }
            })

            recyclerView.adapter = adapterPreferences

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    preferencesViewModel.keyword = text.toString()
                }
            }
        }

        preferencesViewModel.getPreferencesSearchData().observe(viewLifecycleOwner) {
            if (searchBox.text.toString().isEmpty()) return@observe
            adapterPreferenceSearch.list = it
            adapterPreferenceSearch.keyword = searchBox.text.toString()

            adapterPreferenceSearch.setOnPreferencesCallbackListener(object : PreferencesCallbacks {
                override fun onPrefsClicked(imageView: ImageView, category: Int, position: Int) {
                    clearExitTransition()
                    when (category) {
                        R.string.appearance -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        AppearanceScreen.newInstance(),
                                                        "appearance_prefs")
                        }
                        R.string.behaviour -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        BehaviourScreen.newInstance(),
                                                        "behaviour_prefs")
                        }
                        R.string.configuration -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        ConfigurationScreen.newInstance(),
                                                        "config_prefs")
                        }
                        R.string.formatting -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        FormattingScreen.newInstance(),
                                                        "formatting_prefs")
                        }
                        R.string.terminal -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TerminalScreen.newInstance(),
                                                        "terminal_prefs")
                        }
                        R.string.shell_preferences -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        ShellScreen.newInstance(),
                                                        "shell_prefs")
                        }
                        R.string.accessibility -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        AccessibilityScreen.newInstance(),
                                                        "accessibility_prefs")
                        }
                        R.string.development -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        DevelopmentScreen.newInstance(),
                                                        "development_prefs")
                        }
                        R.string.about -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        AboutScreen.newInstance(),
                                                        "about_prefs")
                        }
                    }
                }
            })

            recyclerView.adapter = adapterPreferenceSearch
            recyclerView.scheduleLayoutAnimation()
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                PreferencesSearchData.setSearchVisibility(!PreferencesSearchData.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState() {
        if (PreferencesSearchData.isSearchVisible()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(true)
            searchBox.showInput()
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(true)
            searchBox.gone()
            searchBox.hideInput()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferencesSearchData.preferencesSearch -> {
                searchBoxState()
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