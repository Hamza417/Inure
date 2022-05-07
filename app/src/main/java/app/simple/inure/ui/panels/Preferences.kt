package app.simple.inure.ui.panels

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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
import app.simple.inure.constants.PreferencesSearchConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.appearance.IconSize
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.dialogs.configuration.DateFormat
import app.simple.inure.dialogs.terminal.DialogCommandLine
import app.simple.inure.dialogs.terminal.DialogHomePath
import app.simple.inure.dialogs.terminal.DialogInitialCommand
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.PreferencesCallbacks
import app.simple.inure.models.PreferenceSearchModel
import app.simple.inure.ui.preferences.mainscreens.*
import app.simple.inure.ui.preferences.subscreens.*
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.PreferencesViewModel

class Preferences : ScopedFragment() {

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

        searchBoxState(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

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
                        R.string.shell -> {
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
                    preferencesViewModel.keyword = text.toString().trim()
                }
            }
        }

        preferencesViewModel.getPreferencesSearchData().observe(viewLifecycleOwner) {
            if (searchBox.text.toString().isEmpty()) return@observe
            adapterPreferenceSearch.list = it
            adapterPreferenceSearch.keyword = searchBox.text.toString()

            adapterPreferenceSearch.setOnPreferencesCallbackListener(object : PreferencesCallbacks {
                override fun onPrefsSearchItemClicked(preferenceSearchModel: PreferenceSearchModel) {
                    clearExitTransition()
                    when (preferenceSearchModel.panel) {
                        R.string.appearance -> {
                            when (preferenceSearchModel.title) {
                                R.string.application_theme -> {
                                    FragmentHelper.openFragment(parentFragmentManager, AppearanceAppTheme.newInstance(), "theme")
                                }
                                R.string.accent_colors -> {
                                    FragmentHelper.openFragment(parentFragmentManager, AccentColor.newInstance(), "accent_color")
                                }
                                R.string.app_typeface -> {
                                    FragmentHelper.openFragment(parentFragmentManager, AppearanceTypeFace.newInstance(), "typeface")
                                }
                                R.string.corner_radius -> {
                                    RoundedCorner.newInstance().show(childFragmentManager, "rounded_corner")
                                }
                                R.string.icon_size -> {
                                    IconSize.newInstance().show(childFragmentManager, "icon_size")
                                }
                                else -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                AppearanceScreen.newInstance(),
                                                                "appearance_prefs")
                                }
                            }
                        }
                        R.string.behaviour -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        BehaviourScreen.newInstance(),
                                                        "behaviour_prefs")
                        }
                        R.string.configuration -> {
                            when (preferenceSearchModel.title) {
                                R.string.shortcuts -> {
                                    FragmentHelper.openFragment(parentFragmentManager, Shortcuts.newInstance(), "shortcuts")
                                }
                                else -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                ConfigurationScreen.newInstance(),
                                                                "config_prefs")
                                }
                            }
                        }
                        R.string.formatting -> {
                            when (preferenceSearchModel.title) {
                                R.string.date_format -> {
                                    DateFormat.newInstance().show(childFragmentManager, "date_format")
                                }
                                else -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                FormattingScreen.newInstance(),
                                                                "formatting_prefs")
                                }
                            }
                        }
                        R.string.terminal -> {
                            when (preferenceSearchModel.title) {
                                R.string.title_fontsize_preference -> {
                                    FragmentHelper.openFragment(parentFragmentManager, TerminalFontSize.newInstance(), "font_size")
                                }
                                R.string.title_color_preference -> {
                                    FragmentHelper.openFragment(parentFragmentManager, TerminalColor.newInstance(), "color")
                                }
                                R.string.title_backaction_preference -> {
                                    FragmentHelper.openFragment(parentFragmentManager, TerminalBackButtonAction.newInstance(), "back_button")
                                }
                                R.string.title_controlkey_preference -> {
                                    FragmentHelper.openFragment(parentFragmentManager, TerminalControlKey.newInstance(), "control_key")
                                }
                                R.string.title_fnkey_preference -> {
                                    FragmentHelper.openFragment(parentFragmentManager, TerminalFnKey.newInstance(), "fn_key")
                                }
                                else -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                TerminalScreen.newInstance(),
                                                                "terminal_prefs")
                                }
                            }
                        }
                        R.string.shell -> {
                            when (preferenceSearchModel.title) {
                                R.string.title_shell_preference -> {
                                    DialogCommandLine.newInstance()
                                        .show(childFragmentManager, "command_line")
                                }
                                R.string.title_initialcommand_preference -> {
                                    DialogInitialCommand.newInstance()
                                        .show(childFragmentManager, "initial_command")
                                }
                                R.string.title_termtype_preference -> {
                                    FragmentHelper.openFragment(parentFragmentManager, ShellTerminalType.newInstance(), "terminal_type")
                                }
                                R.string.title_home_path_preference -> {
                                    DialogHomePath.newInstance()
                                        .show(childFragmentManager, "home_path")
                                }
                                else -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                ShellScreen.newInstance(),
                                                                "shell_prefs")
                                }
                            }
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
                            when (preferenceSearchModel.title) {
                                R.string.change_logs -> {
                                    FragmentHelper.openFragment(parentFragmentManager,
                                                                WebPage.newInstance(getString(R.string.change_logs)),
                                                                "web_page")
                                }
                                R.string.user_agreements -> {
                                    FragmentHelper.openFragment(parentFragmentManager,
                                                                WebPage.newInstance(getString(R.string.user_agreements)),
                                                                "web_page")
                                }
                                R.string.credits -> {
                                    FragmentHelper.openFragment(parentFragmentManager,
                                                                WebPage.newInstance(getString(R.string.credits)),
                                                                "web_page")
                                }
                                R.string.open_source_licenses -> {
                                    FragmentHelper.openFragment(parentFragmentManager,
                                                                WebPage.newInstance(getString(R.string.open_source_licenses)),
                                                                "web_page")
                                }
                                R.string.share -> {
                                    FragmentHelper.openFragment(parentFragmentManager,
                                                                Share.newInstance(),
                                                                "share")
                                }
                                R.string.github -> {
                                    val uri: Uri = Uri.parse("https://github.com/Hamza417/Inure")
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                                R.string.translate -> {
                                    val uri: Uri = Uri.parse("https://crowdin.com/project/inure")
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                                R.string.join_telegram -> {
                                    val uri: Uri = Uri.parse("https://t.me/inure_app_manager")
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                                else -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                AboutScreen.newInstance(),
                                                                "about_prefs")
                                }
                            }
                        }
                    }
                }
            })

            recyclerView.adapter = adapterPreferenceSearch
            recyclerView.scheduleLayoutAnimation()
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                PreferencesSearchConstants.setSearchVisibility(!PreferencesSearchConstants.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState(animate: Boolean) {
        if (PreferencesSearchConstants.isSearchVisible()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(animate)
            searchBox.showInput()
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(animate)
            searchBox.gone()
            searchBox.hideInput()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferencesSearchConstants.preferencesSearch -> {
                searchBoxState(true)
            }
        }
    }

    companion object {
        fun newInstance(): Preferences {
            val args = Bundle()
            val fragment = Preferences()
            fragment.arguments = args
            return fragment
        }
    }
}