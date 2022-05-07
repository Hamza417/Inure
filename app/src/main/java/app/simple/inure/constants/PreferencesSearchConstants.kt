package app.simple.inure.constants

import app.simple.inure.R
import app.simple.inure.models.PreferenceSearchModel
import app.simple.inure.preferences.SharedPreferences

object PreferencesSearchConstants {

    const val preferencesSearch = "preferences_search"

    // Type
    private const val toggleable = R.string.toggleable
    private const val options = R.string.options
    private const val multi_toggleable = R.string.multi_toggleable
    private const val link = R.string.link
    private const val popup = R.string.popup
    private const val none = R.string.none
    private const val web_page = R.string.web_page

    // Category
    private const val color = R.string.color
    private const val layouts = R.string.layouts
    private const val icons = R.string.icons
    private const val popups = R.string.popups
    private const val animations = R.string.animations
    private const val loading = R.string.loading
    private const val application = R.string.application
    private const val root = R.string.root
    private const val information = R.string.information
    private const val lists = R.string.lists
    private const val text = R.string.text
    private const val keyboard = R.string.keyboard
    private const val editor = R.string.editor
    private const val viewers = R.string.viewers
    private const val legalNotes = R.string.legal_notes
    private const val contribute = R.string.contribute
    private const val preferences = R.string.preferences

    // Panel
    private const val appearance = R.string.appearance
    private const val behaviour = R.string.behaviour
    private const val configuration = R.string.configuration
    private const val formatting = R.string.formatting
    private const val accessibility = R.string.accessibility
    private const val terminal = R.string.terminal
    private const val shell = R.string.shell
    private const val batch = R.string.batch
    private const val development = R.string.development
    private const val about = R.string.about

    val appearanceData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_dark_mode, R.string.application_theme, R.string.desc_application_theme, options, color, appearance),
            PreferenceSearchModel(R.drawable.ic_colors, R.string.accent_colors, R.string.accent_colors, options, color, appearance),
            PreferenceSearchModel(R.drawable.ic_navigation_color, R.string.accent_for_nav, R.string.desc_accent_on_nav, toggleable, color, appearance),
            PreferenceSearchModel(R.drawable.ic_text_fields, R.string.app_typeface, R.string.desc_app_typeface, options, layouts, appearance),
            PreferenceSearchModel(R.drawable.ic_rounded_corner, R.string.corner_radius, R.string.corner_radius_desc, popup, layouts, appearance),
            PreferenceSearchModel(R.drawable.ic_fullscreen, R.string.disable_transparent_status_bar, R.string.desc_transparent_status, toggleable, layouts, appearance),
            PreferenceSearchModel(R.drawable.ic_light_shadow, R.string.icon_shadows, R.string.icon_shadow_desc, toggleable, icons, appearance),
            PreferenceSearchModel(R.drawable.ic_ruler, R.string.icon_size, R.string.icon_size_desc, popup, icons, appearance),
            PreferenceSearchModel(R.drawable.ic_colorize, R.string.colored_shadows, R.string.colored_shadows_desc, toggleable, icons, appearance)
    )

    val behaviourData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_light, R.string.dim_windows, R.string.dim_windows_desc, toggleable, popups, behaviour),
            PreferenceSearchModel(R.drawable.ic_lightbulb, R.string.colored_shadows, R.string.colored_shadows_desc, toggleable, popups, behaviour),
            PreferenceSearchModel(R.drawable.ic_animation, R.string.transitions, R.string.transition_desc, toggleable, animations, behaviour),
            PreferenceSearchModel(R.drawable.ic_arc_animations, R.string.arc_animations, R.string.desc_arc_animations, toggleable, animations, behaviour),
            PreferenceSearchModel(R.drawable.ic_marquee, R.string.marquee_effect, R.string.desc_marquee, toggleable, animations, behaviour),
            PreferenceSearchModel(R.drawable.ic_downloading, R.string.skip_loading_on_app_start, R.string.desc_skip_loading, toggleable, loading, behaviour),
    )

    val configurationData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_phone, R.string.keep_screen_on, R.string.keep_screen_on_desc, toggleable, application, configuration),
            PreferenceSearchModel(R.drawable.ic_shortcut, R.string.shortcuts, R.string.shortcuts_desc, multi_toggleable, application, configuration),
            PreferenceSearchModel(R.drawable.ic_su, R.string.use_root_methods, R.string.root_desc, toggleable, root, configuration),
    )

    val formattingData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_binary, R.string.use_binary_format, R.string.desc_binary_format, toggleable, information, formatting),
            PreferenceSearchModel(R.drawable.ic_quote, R.string.load_large_strings, R.string.desc_large_strings, toggleable, information, formatting),
            PreferenceSearchModel(R.drawable.ic_date_format, R.string.date_format, R.string.desc_date_format, popup, information, formatting),
    )

    val accessibilityData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_highlight, R.string.static_backgrounds, R.string.desc_static_backgrounds, toggleable, icons, accessibility),
            PreferenceSearchModel(R.drawable.ic_border_outer, R.string.stroke, R.string.desc_highlight_stroke, toggleable, icons, accessibility),
            PreferenceSearchModel(R.drawable.ic_divider_lines, R.string.dividers, R.string.desc_dividers, toggleable, lists, accessibility),
            PreferenceSearchModel(R.drawable.ic_animation, R.string.reduce_animations, R.string.desc_reduce_animations, toggleable, animations, accessibility),
    )

    val terminalData = arrayListOf(
            PreferenceSearchModel(R.mipmap.ic_terminal, R.string.standalone_terminal, R.string.standalone_terminal_desc, toggleable, terminal, terminal),
            PreferenceSearchModel(-1, R.string.termux_title, R.string.termux_desc, toggleable, terminal, terminal),
            PreferenceSearchModel(R.drawable.ic_text_fields, R.string.title_fontsize_preference, R.string.summary_fontsize_preference, options, text, terminal),
            PreferenceSearchModel(R.drawable.ic_colors, R.string.title_color_preference, R.string.summary_color_preference, options, text, terminal),
            PreferenceSearchModel(R.drawable.ic_navigation_color, R.string.title_cursorblink_preference, R.string.summary_cursorblink_preference, toggleable, text, terminal),
            PreferenceSearchModel(R.drawable.ic_utf_8, R.string.title_utf8_by_default_preference, R.string.summary_utf8_by_default_preference, toggleable, text, terminal),
            PreferenceSearchModel(R.drawable.ic_arrow_back, R.string.title_backaction_preference, R.string.summary_backaction_preference, options, keyboard, terminal),
            PreferenceSearchModel(R.drawable.ic_ctrl_key, R.string.title_controlkey_preference, R.string.summary_controlkey_preference, options, keyboard, terminal),
            PreferenceSearchModel(R.drawable.ic_fn_key, R.string.title_fnkey_preference, R.string.summary_fnkey_preference, options, keyboard, terminal),
            PreferenceSearchModel(R.drawable.ic_input, R.string.title_ime_preference, R.string.summary_ime_preference, options, keyboard, terminal),
            PreferenceSearchModel(R.drawable.ic_alt_key, R.string.alt_sends_esc, R.string.desc_not_available, options, keyboard, terminal),
            PreferenceSearchModel(R.drawable.ic_keyboard, R.string.title_use_keyboard_shortcuts, R.string.use_keyboard_shortcuts_summary_on, options, keyboard, terminal),
    )

    val shellData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_terminal_black, R.string.title_shell_preference, R.string.summary_shell_preference, popup, editor, shell),
            PreferenceSearchModel(R.drawable.ic_terminal_black, R.string.title_initialcommand_preference, R.string.summary_initialcommand_preference, popup, editor, shell),
            PreferenceSearchModel(R.drawable.ic_terminal_black, R.string.title_termtype_preference, R.string.summary_termtype_preference, options, editor, shell),
            PreferenceSearchModel(R.drawable.ic_mouse, R.string.title_mouse_tracking_preference, R.string.summary_mouse_tracking_preference, toggleable, editor, shell),
            PreferenceSearchModel(R.drawable.ic_close_windows, R.string.title_close_window_on_process_exit_preference, R.string.summary_close_window_on_process_exit_preference, toggleable, editor, shell),
            PreferenceSearchModel(R.drawable.ic_verify_path, R.string.title_verify_path_preference, R.string.summary_verify_path_preference, toggleable, editor, shell),
            PreferenceSearchModel(R.drawable.ic_verify_path, R.string.title_do_path_extensions_preference, R.string.summary_do_path_extensions_preference, toggleable, editor, shell),
            PreferenceSearchModel(R.drawable.ic_verify_path, R.string.title_allow_prepend_path_preference, R.string.summary_allow_prepend_path_preference, toggleable, editor, shell),
            PreferenceSearchModel(R.drawable.ic_home, R.string.title_home_path_preference, R.string.summary_home_path_preference, popup, editor, shell),
    )

    val developmentData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_code, R.string.use_webview_xml_viewer, R.string.use_webview_xml_viewer_desc, toggleable, viewers, development),
            PreferenceSearchModel(R.drawable.ic_music_note, R.string.full_screen_audio_player, R.string.desc_full_screen_audio_player, toggleable, viewers, development),
            PreferenceSearchModel(R.drawable.ic_preference_indicators, R.string.hide_preferences_indicators, R.string.desc_hide_preferences_indicators, toggleable, preferences, development),
    )

    val aboutData = arrayListOf(
            PreferenceSearchModel(R.drawable.ic_label, R.string.version, R.string.versionName, none, application, about),
            PreferenceSearchModel(R.drawable.ic_change_history, R.string.change_logs, R.string.desc_changelogs, web_page, application, about),
            PreferenceSearchModel(R.drawable.ic_law, R.string.user_agreements, R.string.desc_user_agreements, web_page, legalNotes, about),
            PreferenceSearchModel(R.drawable.ic_credits, R.string.credits, R.string.desc_credits, web_page, legalNotes, about),
            PreferenceSearchModel(R.drawable.ic_licenses, R.string.open_source_licenses, R.string.desc_licenses, web_page, application, about),
            PreferenceSearchModel(R.drawable.ic_github, R.string.github, R.string.desc_github, link, contribute, about),
            PreferenceSearchModel(R.drawable.ic_translate, R.string.translate, R.string.transition_desc, link, contribute, about),
            PreferenceSearchModel(R.drawable.ic_telegram, R.string.join_telegram, R.string.desc_telegram, link, contribute, about),
            PreferenceSearchModel(R.drawable.ic_share, R.string.share, R.string.desc_share, popup, contribute, about),
    )

    // ---------------------------------------------------------------------------------------------------------- //

    val preferencesStructureData =
        appearanceData +
                behaviourData +
                configurationData +
                formattingData +
                accessibilityData +
                terminalData +
                shellData +
                developmentData +
                aboutData

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(preferencesSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(preferencesSearch, false)
    }
}