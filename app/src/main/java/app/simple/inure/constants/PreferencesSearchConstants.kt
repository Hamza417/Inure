package app.simple.inure.constants

import androidx.core.content.edit
import app.simple.inure.R
import app.simple.inure.models.PreferenceModel
import app.simple.inure.preferences.SharedPreferences

@Suppress("MayBeConstant")
object PreferencesSearchConstants {

    const val PREFERENCES_SEARCH = "preferences_search"

    // Type
    private val toggleable = R.string.toggleable
    private val options = R.string.options
    private val multi_toggleable = R.string.multi_toggleable
    private val link = R.string.link
    private val popup = R.string.popup
    private val none = R.string.none
    private val web_page = R.string.web_page

    // Category
    private val color = R.string.color
    private val layouts = R.string.layouts
    private val icons = R.string.icons
    private val popups = R.string.popups
    private val animations = R.string.animations
    private val scrolling = R.string.scrolling
    private val loading = R.string.loading
    private val application = R.string.application
    private val root = R.string.root
    private val shizuku = R.string.shizuku
    private val information = R.string.information
    private val lists = R.string.lists
    private val text = R.string.text
    private val keyboard = R.string.keyboard
    private val editor = R.string.editor
    private val viewers = R.string.viewers
    private val legalNotes = R.string.legal_notes
    private val contribute = R.string.contribute
    private val dexClasses = R.string.dex_classes
    private val preferences = R.string.preferences
    private val installer = R.string.installer
    private val home = R.string.home

    // Panel
    private val appearance = R.string.appearance
    private val behaviour = R.string.behavior
    private val configuration = R.string.configuration
    private val formatting = R.string.formatting
    private val accessibility = R.string.accessibility
    private val terminal = R.string.terminal
    private val shell = R.string.shell
    private val development = R.string.development
    private val about = R.string.about

    private val appearanceData = arrayListOf(
            PreferenceModel(R.drawable.ic_dark_mode, R.string.application_theme, R.string.desc_application_theme, options, color, appearance),
            PreferenceModel(R.drawable.ic_colors, R.string.accent_colors, R.string.accent_colors, options, color, appearance),
            PreferenceModel(R.drawable.ic_navigation_color, R.string.accent_for_nav, R.string.desc_accent_on_nav, toggleable, color, appearance),
            PreferenceModel(R.drawable.ic_text_fields, R.string.app_typeface, R.string.desc_app_typeface, options, layouts, appearance),
            PreferenceModel(R.drawable.ic_rounded_corner, R.string.corner_radius, R.string.corner_radius_desc, popup, layouts, appearance),
            PreferenceModel(R.drawable.ic_light_shadow, R.string.icon_shadows, R.string.icon_shadow_desc, toggleable, icons, appearance),
            PreferenceModel(R.drawable.ic_ruler, R.string.icon_size, R.string.icon_size_desc, popup, icons, appearance),
            PreferenceModel(R.drawable.ic_colorize, R.string.tinted_shadows, R.string.colored_shadows_desc, toggleable, icons, appearance)
    )

    private val behaviourData = arrayListOf(
            PreferenceModel(R.drawable.ic_light, R.string.dim_windows, R.string.dim_windows_desc, toggleable, popups, behaviour),
            PreferenceModel(R.drawable.ic_blur_on, R.string.blur_windows, R.string.blur_windows_desc, toggleable, popups, behaviour),
            PreferenceModel(R.drawable.ic_lightbulb, R.string.colored_shadows, R.string.colored_shadows_desc, toggleable, popups, behaviour),
            PreferenceModel(R.drawable.ic_animation, R.string.transitions, R.string.transition_desc, toggleable, animations, behaviour),
            PreferenceModel(R.drawable.ic_animation, R.string.transition_type, R.string.transition_type_desc, popup, animations, behaviour),
            PreferenceModel(R.drawable.switch_thumb, R.string.damping_ratio, R.string.damping_ratio_desc, popup, scrolling, behaviour),
            PreferenceModel(R.drawable.ic_stiffness, R.string.stiffness, R.string.stiffness_desc, popup, scrolling, behaviour),
            PreferenceModel(R.drawable.ic_arc_animations, R.string.arc_animations, R.string.desc_arc_animations, toggleable, animations, behaviour),
            PreferenceModel(R.drawable.ic_arc_animations, R.string.arc_type, R.string.arc_type_desc, popup, animations, behaviour),
            PreferenceModel(R.drawable.ic_marquee, R.string.marquee_effect, R.string.desc_marquee, toggleable, animations, behaviour),
            PreferenceModel(R.drawable.ic_downloading, R.string.skip_loading_on_app_start, R.string.desc_skip_loading, toggleable, loading, behaviour),
    )

    private val configurationData = arrayListOf(
            PreferenceModel(R.drawable.ic_phone, R.string.keep_screen_on, R.string.keep_screen_on_desc, toggleable, application, configuration),
            PreferenceModel(R.drawable.ic_extension, R.string.components, R.string.component_manager_desc, options, application, configuration),
            PreferenceModel(R.drawable.ic_translate, R.string.language, R.string.language_desc, options, application, configuration),
            PreferenceModel(R.drawable.ic_route, R.string.path, R.string.desc_app_path, popup, application, configuration),
            PreferenceModel(R.drawable.ic_shortcut, R.string.shortcuts, R.string.shortcuts_desc, multi_toggleable, application, configuration),
            PreferenceModel(R.drawable.ic_su, R.string.use_root_methods, R.string.root_desc, toggleable, root, configuration),
            PreferenceModel(R.drawable.ic_shizuku, R.string.use_shizuku, R.string.shizuku_desc, toggleable, shizuku, configuration),
            PreferenceModel(R.drawable.ic_people, R.string.show_user_list, R.string.show_user_list_desc, toggleable, application, configuration),
    )

    private val formattingData = arrayListOf(
            PreferenceModel(R.drawable.ic_binary, R.string.use_binary_format, R.string.desc_binary_format, toggleable, information, formatting),
            PreferenceModel(R.drawable.ic_date_format, R.string.date_format, R.string.desc_date_format, popup, information, formatting),
    )

    private val accessibilityData = arrayListOf(
            PreferenceModel(R.drawable.ic_highlight, R.string.static_backgrounds, R.string.desc_static_backgrounds, toggleable, icons, accessibility),
            PreferenceModel(R.drawable.ic_border_outer, R.string.stroke, R.string.desc_highlight_stroke, toggleable, icons, accessibility),
            PreferenceModel(R.drawable.ic_divider_lines, R.string.dividers, R.string.desc_dividers, toggleable, lists, accessibility),
            PreferenceModel(R.drawable.ic_animation, R.string.reduce_animations, R.string.desc_reduce_animations, toggleable, animations, accessibility),
            PreferenceModel(R.drawable.ic_animation, R.string.disable_animations, R.string.desc_disable_animations, toggleable, animations, accessibility),
            PreferenceModel(R.drawable.ic_format_paint, R.string.colorful_icons, R.string.colorful_icons_desc, toggleable, icons, accessibility),
    )

    private val terminalData = arrayListOf(
            PreferenceModel(R.mipmap.ic_terminal, R.string.standalone_terminal, R.string.standalone_terminal_desc, toggleable, terminal, terminal),
            //PreferenceModel(-1, R.string.termux_title, R.string.termux_desc, toggleable, terminal, terminal),
            PreferenceModel(R.drawable.ic_text_fields, R.string.title_fontsize_preference, R.string.summary_fontsize_preference, options, text, terminal),
            PreferenceModel(R.drawable.ic_colors, R.string.title_color_preference, R.string.summary_color_preference, options, text, terminal),
            PreferenceModel(R.drawable.ic_navigation_color, R.string.title_cursorblink_preference, R.string.summary_cursorblink_preference, toggleable, text, terminal),
            PreferenceModel(R.drawable.ic_utf_8, R.string.title_utf8_by_default_preference, R.string.summary_utf8_by_default_preference, toggleable, text, terminal),
            PreferenceModel(R.drawable.ic_arrow_back, R.string.title_backaction_preference, R.string.summary_backaction_preference, options, keyboard, terminal),
            PreferenceModel(R.drawable.ic_ctrl_key, R.string.title_controlkey_preference, R.string.summary_controlkey_preference, options, keyboard, terminal),
            PreferenceModel(R.drawable.ic_fn_key, R.string.title_fnkey_preference, R.string.summary_fnkey_preference, options, keyboard, terminal),
            PreferenceModel(R.drawable.ic_input, R.string.title_ime_preference, R.string.summary_ime_preference, options, keyboard, terminal),
            PreferenceModel(R.drawable.ic_alt_key, R.string.alt_sends_esc, R.string.desc_not_available, options, keyboard, terminal),
            PreferenceModel(R.drawable.ic_keyboard, R.string.title_use_keyboard_shortcuts, R.string.use_keyboard_shortcuts_summary_on, options, keyboard, terminal),
    )

    private val shellData = arrayListOf(
            PreferenceModel(R.drawable.ic_terminal_black, R.string.title_shell_preference, R.string.summary_shell_preference, popup, editor, shell),
            PreferenceModel(R.drawable.ic_terminal_black, R.string.title_initialcommand_preference, R.string.summary_initialcommand_preference, popup, editor, shell),
            PreferenceModel(R.drawable.ic_terminal_black, R.string.title_termtype_preference, R.string.summary_termtype_preference, options, editor, shell),
            PreferenceModel(R.drawable.ic_mouse, R.string.title_mouse_tracking_preference, R.string.summary_mouse_tracking_preference, toggleable, editor, shell),
            PreferenceModel(R.drawable.ic_close_windows, R.string.title_close_window_on_process_exit_preference, R.string.summary_close_window_on_process_exit_preference, toggleable, editor, shell),
            PreferenceModel(R.drawable.ic_verify_path, R.string.title_verify_path_preference, R.string.summary_verify_path_preference, toggleable, editor, shell),
            PreferenceModel(R.drawable.ic_verify_path, R.string.title_do_path_extensions_preference, R.string.summary_do_path_extensions_preference, toggleable, editor, shell),
            PreferenceModel(R.drawable.ic_verify_path, R.string.title_allow_prepend_path_preference, R.string.summary_allow_prepend_path_preference, toggleable, editor, shell),
            PreferenceModel(R.drawable.ic_home, R.string.title_home_path_preference, R.string.summary_home_path_preference, popup, editor, shell),
    )

    private val layoutData = arrayListOf(
            PreferenceModel(R.drawable.ic_linear_scale, R.string.visibility_customization, R.string.installer_visibility_customization_desc, options, installer, layouts),
            PreferenceModel(R.drawable.ic_linear_scale, R.string.visibility_customization, R.string.home_visibility_customization_desc, options, home, layouts),
            PreferenceModel(R.drawable.ic_grid_4, R.string.menu_layout, R.string.menu_layout_desc, options, home, layouts),
            PreferenceModel(R.drawable.ic_linear_scale, R.string.app_information, R.string.info_visibility_customization_desc, options, home, layouts),
    )

    private val aboutData = arrayListOf(
            PreferenceModel(R.drawable.ic_label, R.string.version, R.string.versionName, none, application, about),
            PreferenceModel(R.drawable.ic_change_history, R.string.change_logs, R.string.desc_changelogs, web_page, application, about),
            PreferenceModel(R.drawable.ic_law, R.string.user_agreements, R.string.desc_user_agreements, web_page, legalNotes, about),
            PreferenceModel(R.drawable.ic_credits, R.string.credits, R.string.desc_credits, web_page, legalNotes, about),
            PreferenceModel(R.drawable.ic_licenses, R.string.open_source_licenses, R.string.desc_licenses, web_page, application, about),
            PreferenceModel(R.drawable.ic_github, R.string.github, R.string.desc_github, link, contribute, about),
            PreferenceModel(R.drawable.ic_translate, R.string.translate, R.string.transition_desc, link, contribute, about),
            PreferenceModel(R.drawable.ic_telegram, R.string.telegram, R.string.empty, link, contribute, about),
            PreferenceModel(R.drawable.ic_share, R.string.share, R.string.desc_share, popup, contribute, about),
            PreferenceModel(R.drawable.ic_face, R.string.developer_profile, R.string.empty, popup, contribute, about),
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
                // layoutData +
                // developmentData +
                aboutData

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(PREFERENCES_SEARCH, boolean) }
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(PREFERENCES_SEARCH, false)
    }
}
