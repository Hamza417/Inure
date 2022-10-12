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
import app.simple.inure.activities.app.ManageSpace
import app.simple.inure.adapters.preferences.AdapterPreferenceSearch
import app.simple.inure.adapters.preferences.AdapterPreferences
import app.simple.inure.constants.PreferencesSearchConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.app.AppMemory
import app.simple.inure.dialogs.appearance.IconSize
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.dialogs.configuration.DateFormat
import app.simple.inure.dialogs.terminal.DialogCommandLine
import app.simple.inure.dialogs.terminal.DialogHomePath
import app.simple.inure.dialogs.terminal.DialogInitialCommand
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.interfaces.adapters.PreferencesCallbacks
import app.simple.inure.models.PreferenceModel
import app.simple.inure.preferences.RecyclerViewPreferences
import app.simple.inure.ui.preferences.mainscreens.*
import app.simple.inure.ui.preferences.subscreens.*
import app.simple.inure.viewmodels.panels.PreferencesViewModel

class Preferences : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterPreferences: AdapterPreferences
    private lateinit var memory: DynamicRippleImageButton

    private val adapterPreferenceSearch = AdapterPreferenceSearch()
    private val preferencesViewModel: PreferencesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preferences, container, false)

        recyclerView = view.findViewById(R.id.preferences_recycler_view)
        search = view.findViewById(R.id.preferences_search_btn)
        memory = view.findViewById(R.id.preferences_memory_btn)
        searchBox = view.findViewById(R.id.preferences_search)
        title = view.findViewById(R.id.preferences_title)

        searchBoxState(false, PreferencesSearchConstants.isSearchVisible())

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
                    RecyclerViewPreferences.setViewTag(tag)
                    RecyclerViewPreferences.setViewPosition(position)

                    /**
                     * Workaround for shared animation lag
                     */
                    var duration = 100L
                    duration += position * 60
                    duration = duration.coerceIn(500L, 800L)

                    when (category) {
                        R.string.appearance -> {
                            openFragmentLinear(AppearanceScreen.newInstance(), imageView, "appearance_prefs", duration)
                        }
                        R.string.behaviour -> {
                            openFragmentLinear(BehaviourScreen.newInstance(), imageView, "behaviour_prefs", duration)
                        }
                        R.string.configuration -> {
                            openFragmentLinear(ConfigurationScreen.newInstance(), imageView, "config_prefs", duration)
                        }
                        R.string.formatting -> {
                            openFragmentLinear(FormattingScreen.newInstance(), imageView, "formatting_prefs", duration)
                        }
                        R.string.terminal -> {
                            openFragmentLinear(TerminalScreen.newInstance(), imageView, "terminal_prefs", duration)
                        }
                        R.string.shell -> {
                            openFragmentLinear(ShellScreen.newInstance(), imageView, "shell_prefs", duration)
                        }
                        R.string.accessibility -> {
                            openFragmentLinear(AccessibilityScreen.newInstance(), imageView, "accessibility_prefs", duration)
                        }
                        R.string.development -> {
                            openFragmentLinear(DevelopmentScreen.newInstance(), imageView, "development_prefs", duration)
                        }
                        R.string.about -> {
                            openFragmentLinear(AboutScreen.newInstance(), imageView, "about_prefs", duration)
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
                override fun onPrefsSearchItemClicked(preferenceModel: PreferenceModel) {
                    when (preferenceModel.panel) {
                        R.string.appearance -> {
                            when (preferenceModel.title) {
                                R.string.application_theme -> {
                                    openFragmentSlide(AppearanceAppTheme.newInstance(), "theme")
                                }
                                R.string.accent_colors -> {
                                    openFragmentSlide(AccentColor.newInstance(), "accent_color")
                                }
                                R.string.app_typeface -> {
                                    openFragmentSlide(AppearanceTypeFace.newInstance(), "typeface")
                                }
                                R.string.corner_radius -> {
                                    RoundedCorner.newInstance().show(childFragmentManager, "rounded_corner")
                                }
                                R.string.icon_size -> {
                                    IconSize.newInstance().show(childFragmentManager, "icon_size")
                                }
                                else -> {
                                    openFragmentSlide(AppearanceScreen.newInstance(), "appearance_prefs")
                                }
                            }
                        }
                        R.string.behaviour -> {
                            openFragmentSlide(BehaviourScreen.newInstance(), "behaviour_prefs")
                        }
                        R.string.configuration -> {
                            when (preferenceModel.title) {
                                R.string.shortcuts -> {
                                    openFragmentSlide(Shortcuts.newInstance(), "shortcuts")
                                }
                                else -> {
                                    openFragmentSlide(ConfigurationScreen.newInstance(), "config_prefs")
                                }
                            }
                        }
                        R.string.formatting -> {
                            when (preferenceModel.title) {
                                R.string.date_format -> {
                                    DateFormat.newInstance().show(childFragmentManager, "date_format")
                                }
                                else -> {
                                    openFragmentSlide(FormattingScreen.newInstance(), "formatting_prefs")
                                }
                            }
                        }
                        R.string.terminal -> {
                            when (preferenceModel.title) {
                                R.string.title_fontsize_preference -> {
                                    openFragmentSlide(TerminalFontSize.newInstance(), "font_size")
                                }
                                R.string.title_color_preference -> {
                                    openFragmentSlide(TerminalColor.newInstance(), "color")
                                }
                                R.string.title_backaction_preference -> {
                                    openFragmentSlide(TerminalBackButtonAction.newInstance(), "back_button")
                                }
                                R.string.title_controlkey_preference -> {
                                    openFragmentSlide(TerminalControlKey.newInstance(), "control_key")
                                }
                                R.string.title_fnkey_preference -> {
                                    openFragmentSlide(TerminalFnKey.newInstance(), "fn_key")
                                }
                                else -> {
                                    openFragmentSlide(TerminalScreen.newInstance(), "terminal_prefs")
                                }
                            }
                        }
                        R.string.shell -> {
                            when (preferenceModel.title) {
                                R.string.title_shell_preference -> {
                                    DialogCommandLine.newInstance()
                                        .show(childFragmentManager, "command_line")
                                }
                                R.string.title_initialcommand_preference -> {
                                    DialogInitialCommand.newInstance()
                                        .show(childFragmentManager, "initial_command")
                                }
                                R.string.title_termtype_preference -> {
                                    openFragmentSlide(ShellTerminalType.newInstance(), "terminal_type")
                                }
                                R.string.title_home_path_preference -> {
                                    DialogHomePath.newInstance()
                                        .show(childFragmentManager, "home_path")
                                }
                                else -> {
                                    openFragmentSlide(ShellScreen.newInstance(), "shell_prefs")
                                }
                            }
                        }
                        R.string.accessibility -> {
                            openFragmentSlide(AccessibilityScreen.newInstance(), "accessibility_prefs")
                        }
                        R.string.development -> {
                            openFragmentSlide(DevelopmentScreen.newInstance(), "development_prefs")
                        }
                        R.string.about -> {
                            when (preferenceModel.title) {
                                R.string.change_logs -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.change_logs)), "web_page")
                                }
                                R.string.user_agreements -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.user_agreements)), "web_page")
                                }
                                R.string.credits -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.credits)), "web_page")
                                }
                                R.string.open_source_licenses -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.open_source_licenses)), "web_page")
                                }
                                R.string.share -> {
                                    openFragmentSlide(Share.newInstance(), "share")
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
                                    openFragmentSlide(AboutScreen.newInstance(), "about_prefs")
                                }
                            }
                        }
                    }
                }
            })

            recyclerView.adapter = adapterPreferenceSearch
            recyclerView.scheduleLayoutAnimation()
        }

        memory.setOnClickListener {
            startActivity(Intent(requireActivity(), ManageSpace::class.java))
        }

        memory.setOnLongClickListener {
            AppMemory.newInstance()
                .show(childFragmentManager, "app_memory")
            return@setOnLongClickListener true
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                PreferencesSearchConstants.setSearchVisibility(!PreferencesSearchConstants.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferencesSearchConstants.preferencesSearch -> {
                searchBoxState(animate = true, PreferencesSearchConstants.isSearchVisible())
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