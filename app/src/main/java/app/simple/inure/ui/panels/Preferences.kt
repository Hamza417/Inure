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
import app.simple.inure.dialogs.behavior.DampingRatio.Companion.showDampingRatioDialog
import app.simple.inure.dialogs.behavior.Stiffness.Companion.showStiffnessDialog
import app.simple.inure.dialogs.configuration.AppPath.Companion.showAppPathDialog
import app.simple.inure.dialogs.configuration.DateFormat
import app.simple.inure.dialogs.terminal.TerminalCommandLine
import app.simple.inure.dialogs.terminal.TerminalHomePath
import app.simple.inure.dialogs.terminal.TerminalInitialCommand
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.interfaces.adapters.PreferencesCallbacks
import app.simple.inure.models.PreferenceModel
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupDampingRatio
import app.simple.inure.popups.behavior.PopupStiffness
import app.simple.inure.popups.behavior.PopupTransitionType
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.ui.preferences.mainscreens.AboutScreen
import app.simple.inure.ui.preferences.mainscreens.AccessibilityScreen
import app.simple.inure.ui.preferences.mainscreens.AppearanceScreen
import app.simple.inure.ui.preferences.mainscreens.BehaviourScreen
import app.simple.inure.ui.preferences.mainscreens.ConfigurationScreen
import app.simple.inure.ui.preferences.mainscreens.DevelopmentScreen
import app.simple.inure.ui.preferences.mainscreens.FormattingScreen
import app.simple.inure.ui.preferences.mainscreens.LayoutsScreen
import app.simple.inure.ui.preferences.mainscreens.ShellScreen
import app.simple.inure.ui.preferences.mainscreens.TerminalScreen
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceAppTheme
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.ui.preferences.subscreens.ComponentManager
import app.simple.inure.ui.preferences.subscreens.Language
import app.simple.inure.ui.preferences.subscreens.Share
import app.simple.inure.ui.preferences.subscreens.ShellTerminalType
import app.simple.inure.ui.preferences.subscreens.Shortcuts
import app.simple.inure.ui.preferences.subscreens.TerminalBackButtonAction
import app.simple.inure.ui.preferences.subscreens.TerminalColor
import app.simple.inure.ui.preferences.subscreens.TerminalControlKey
import app.simple.inure.ui.preferences.subscreens.TerminalFnKey
import app.simple.inure.ui.preferences.subscreens.TerminalFontSize
import app.simple.inure.viewmodels.panels.PreferencesViewModel
import kotlin.math.pow

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
        postponeEnterTransition()
        // setCount(getString(R.string.app_name_full) + " | " + BuildConfig.VERSION_NAME)

        preferencesViewModel.getPreferences().observe(viewLifecycleOwner) {
            adapterPreferences = AdapterPreferences(it)

            adapterPreferences.setOnPreferencesCallbackListener(object : PreferencesCallbacks {
                override fun onPrefsClicked(imageView: ImageView, category: Int, position: Int) {
                    val duration = (position + 1).toDouble().pow(2.0).toLong().coerceIn(500L, 800L)

                    when (category) {
                        R.string.appearance -> {
                            openFragmentLinear(AppearanceScreen.newInstance(), imageView, AppearanceScreen.TAG, duration)
                        }

                        R.string.behavior -> {
                            openFragmentLinear(BehaviourScreen.newInstance(), imageView, BehaviourScreen.TAG, duration)
                        }

                        R.string.configuration -> {
                            openFragmentLinear(ConfigurationScreen.newInstance(), imageView, ConfigurationScreen.TAG, duration)
                        }

                        R.string.formatting -> {
                            openFragmentLinear(FormattingScreen.newInstance(), imageView, FormattingScreen.TAG, duration)
                        }

                        R.string.terminal -> {
                            openFragmentLinear(TerminalScreen.newInstance(), imageView, TerminalScreen.TAG, duration)
                        }

                        R.string.shell -> {
                            openFragmentLinear(ShellScreen.newInstance(), imageView, ShellScreen.TAG, duration)
                        }

                        R.string.layouts -> {
                            openFragmentLinear(LayoutsScreen.newInstance(), imageView, LayoutsScreen.TAG, duration)
                        }

                        R.string.accessibility -> {
                            openFragmentLinear(AccessibilityScreen.newInstance(), imageView, AccessibilityScreen.TAG, duration)
                        }

                        R.string.development -> {
                            openFragmentLinear(DevelopmentScreen.newInstance(), imageView, DevelopmentScreen.TAG, duration)
                        }

                        R.string.about -> {
                            openFragmentLinear(AboutScreen.newInstance(), imageView, AboutScreen.TAG, duration)
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
                override fun onPrefsSearchItemClicked(preferenceModel: PreferenceModel, view: View) {
                    when (preferenceModel.panel) {
                        R.string.appearance -> {
                            when (preferenceModel.title) {
                                R.string.application_theme -> {
                                    openFragmentSlide(AppearanceAppTheme.newInstance(), AppearanceAppTheme.TAG)
                                }

                                R.string.accent_colors -> {
                                    openFragmentSlide(AccentColor.newInstance(), AccentColor.TAG)
                                }

                                R.string.app_typeface -> {
                                    openFragmentSlide(AppearanceTypeFace.newInstance(), AppearanceTypeFace.TAG)
                                }

                                R.string.corner_radius -> {
                                    RoundedCorner.newInstance().show(childFragmentManager, RoundedCorner.TAG)
                                }

                                R.string.icon_size -> {
                                    IconSize.newInstance().show(childFragmentManager, IconSize.TAG)
                                }

                                else -> {
                                    openFragmentSlide(AppearanceScreen.newInstance(), AppearanceScreen.TAG)
                                }
                            }
                        }

                        R.string.behavior -> {
                            when (preferenceModel.title) {
                                R.string.transition_type -> {
                                    PopupTransitionType(view)
                                }

                                R.string.arc_type -> {
                                    PopupArcType(view)
                                }

                                R.string.damping_ratio -> {
                                    if (DevelopmentPreferences.get(DevelopmentPreferences.oldStyleScrollingBehaviorDialog)) {
                                        PopupDampingRatio(view)
                                    } else {
                                        childFragmentManager.showDampingRatioDialog()
                                    }
                                }

                                R.string.stiffness -> {
                                    if (DevelopmentPreferences.get(DevelopmentPreferences.oldStyleScrollingBehaviorDialog)) {
                                        PopupStiffness(view)
                                    } else {
                                        childFragmentManager.showStiffnessDialog()
                                    }
                                }

                                else -> {
                                    openFragmentSlide(BehaviourScreen.newInstance(), BehaviourScreen.TAG)
                                }
                            }
                        }

                        R.string.configuration -> {
                            when (preferenceModel.title) {
                                R.string.shortcuts -> {
                                    openFragmentSlide(Shortcuts.newInstance(), Shortcuts.TAG)
                                }

                                R.string.components -> {
                                    openFragmentSlide(ComponentManager.newInstance(), ComponentManager.TAG)
                                }

                                R.string.language -> {
                                    openFragmentSlide(Language.newInstance(), Language.TAG)
                                }

                                R.string.path -> {
                                    childFragmentManager.showAppPathDialog()
                                }

                                else -> {
                                    openFragmentSlide(ConfigurationScreen.newInstance(), ConfigurationScreen.TAG)
                                }
                            }
                        }

                        R.string.formatting -> {
                            when (preferenceModel.title) {
                                R.string.date_format -> {
                                    DateFormat.newInstance().show(childFragmentManager, DateFormat.TAG)
                                }

                                else -> {
                                    openFragmentSlide(FormattingScreen.newInstance(), FormattingScreen.TAG)
                                }
                            }
                        }

                        R.string.terminal -> {
                            when (preferenceModel.title) {
                                R.string.title_fontsize_preference -> {
                                    openFragmentSlide(TerminalFontSize.newInstance(), TerminalFontSize.TAG)
                                }

                                R.string.title_color_preference -> {
                                    openFragmentSlide(TerminalColor.newInstance(), TerminalColor.TAG)
                                }

                                R.string.title_backaction_preference -> {
                                    openFragmentSlide(TerminalBackButtonAction.newInstance(), TerminalBackButtonAction.TAG)
                                }

                                R.string.title_controlkey_preference -> {
                                    openFragmentSlide(TerminalControlKey.newInstance(), TerminalControlKey.TAG)
                                }

                                R.string.title_fnkey_preference -> {
                                    openFragmentSlide(TerminalFnKey.newInstance(), TerminalFnKey.TAG)
                                }

                                else -> {
                                    openFragmentSlide(TerminalScreen.newInstance(), TerminalScreen.TAG)
                                }
                            }
                        }

                        R.string.shell -> {
                            when (preferenceModel.title) {
                                R.string.title_shell_preference -> {
                                    TerminalCommandLine.newInstance()
                                        .show(childFragmentManager, TerminalCommandLine.TAG)
                                }

                                R.string.title_initialcommand_preference -> {
                                    TerminalInitialCommand.newInstance()
                                        .show(childFragmentManager, TerminalInitialCommand.TAG)
                                }

                                R.string.title_termtype_preference -> {
                                    openFragmentSlide(ShellTerminalType.newInstance(), ShellTerminalType.TAG)
                                }

                                R.string.title_home_path_preference -> {
                                    TerminalHomePath.newInstance()
                                        .show(childFragmentManager, TerminalHomePath.TAG)
                                }

                                else -> {
                                    openFragmentSlide(ShellScreen.newInstance(), ShellScreen.TAG)
                                }
                            }
                        }

                        R.string.accessibility -> {
                            openFragmentSlide(AccessibilityScreen.newInstance(), AccessibilityScreen.TAG)
                        }

                        R.string.development -> {
                            openFragmentSlide(DevelopmentScreen.newInstance(), DevelopmentScreen.TAG)
                        }

                        R.string.about -> {
                            when (preferenceModel.title) {
                                R.string.change_logs -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.change_logs)), WebPage.TAG)
                                }

                                R.string.user_agreements -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.user_agreements)), WebPage.TAG)
                                }

                                R.string.credits -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.credits)), WebPage.TAG)
                                }

                                R.string.open_source_licenses -> {
                                    openFragmentSlide(WebPage.newInstance(getString(R.string.open_source_licenses)), WebPage.TAG)
                                }

                                R.string.share -> {
                                    openFragmentSlide(Share.newInstance(), Share.TAG)
                                }

                                R.string.github -> {
                                    val uri: Uri = Uri.parse("https://github.com/Hamza417/Inure")
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }

                                R.string.translate -> {
                                    val uri: Uri = Uri.parse("https://crowdin.com/project/inure")
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }

                                R.string.telegram -> {
                                    val uri: Uri = Uri.parse("https://t.me/inure_app_manager")
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }

                                else -> {
                                    openFragmentSlide(AboutScreen.newInstance(), AboutScreen.TAG)
                                }
                            }
                        }
                    }
                }
            })

            if (recyclerView.adapter != adapterPreferenceSearch) {
                recyclerView.adapter = adapterPreferenceSearch
            }
        }

        memory.setOnClickListener {
            startActivity(Intent(requireActivity(), ManageSpace::class.java))
        }

        memory.setOnLongClickListener {
            AppMemory.newInstance()
                .show(childFragmentManager, AppMemory.TAG)
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

        const val TAG = "Preferences"
    }
}
