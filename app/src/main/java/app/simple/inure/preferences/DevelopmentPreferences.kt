package app.simple.inure.preferences

import androidx.core.content.edit
import app.simple.inure.models.DevelopmentPreferencesModel

object DevelopmentPreferences {

    const val IS_WEBVIEW_XML_VIEWER = "is_xml_viewer_web_view"
    const val TEXTVIEW_DRAWABLE_INDICATOR = "is_preferences_indicator_hidden"
    const val CRASH_HANDLER = "is_using_native_crash_handler"
    const val MUSIC = "is_music_enabled"
    const val IMAGE_CACHING = "is_image_caching_enabled"
    const val DEBUG_MODE = "is_debug_messages_enabled"
    const val HOVER_ANIMATION = "is_hover_animation_enabled"
    const val CENTER_BOTTOM_MENU = "is_center_bottom_menu_enabled"
    const val ALTERNATIVE_BATTERY_OPTIMIZATION_SWITCH = "is_alternative_battery_optimization_switch_enabled"
    const val LOAD_ALBUM_ART_FROM_FILE = "is_album_art_loaded_from_file"
    const val USE_OLD_STYLE_USAGE_STATS_PANEL = "is_old_style_usage_stats_panel_enabled"
    const val ADD_BITMAP_TO_METADATA = "is_bitmap_added_to_metadata"
    const val DISABLE_TRANSPARENT_STATUS = "is_transparent_status_disabled_removed"
    const val LOAD_ALL_INSTALLER_PAGES = "is_all_installer_pages_loaded"
    const val IS_NOTCH_AREA_ENABLED = "is_notch_area_enabled"
    const val IS_TEXT_SELECTABLE = "is_text_selectable"
    const val SHOW_GREETING_IN_TERMINAL = "is_greeting_shown_in_terminal"
    const val OLD_STYLE_SCROLLING_BEHAVIOR_DIALOG = "is_old_style_scrolling_behavior_dialog_enabled"
    const val USE_ALTERNATE_AUDIO_PLAYER_INTERFACE = "is_alternate_audio_player_interface_enabled"
    const val SHOW_COMPLETE_APP_SIZE = "is_complete_app_size_shown"
    const val PADDING_LESS_POPUP_MENUS = "is_padding_less_popup_menus_enabled"
    const val DIVIDER_ON_NAVIGATION_BAR = "is_divider_on_navigation_bar_enabled"
    const val PAUSE_IMAGE_LOADER = "is_image_loader_paused"
    const val EXPAND_HOME_HEADER = "is_home_header_expanded"
    const val IS_SWITCH_FANCY_DRAGGABLE = "is_switch_fancy_draggable"
    const val USE_COLORFUL_HIGHLIGHT = "is_colorful_highlight_enabled"
    const val USE_PERISTYLE_INTERFACE = "is_felicity_flow_interface_enabled"
    const val USE_CORRESPONDING_COLOR_ON_HIGHLIGHT = "is_corresponding_color_on_highlight_enabled"
    const val USE_BLUR_BETWEEN_PANELS = "is_blur_between_panels_enabled"
    const val CLEAR_SEARCH_STATE = "clear_search_state"
    const val TEST_PREDICTIVE_BACK_GESTURE = "test_predictive_back_gesture"
    const val REFRESH_APPS_LIST_USING_LAUNCHER_SERVICE = "refresh_apps_list_using_launcher_service"
    const val HIDE_CHIPS_CHECKED_ICON = "hide_chips_checked_icon"

    val developmentPreferences: List<DevelopmentPreferencesModel> by lazy {
        listOf(
                DevelopmentPreferencesModel("Use WebView for XML Preview",
                                            "Use WebView for XML Preview instead of TextView.",
                                            IS_WEBVIEW_XML_VIEWER,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Hide Text View Indicators",
                                            "Hides the indicators in the settings, dialogs and various other places in the app.",
                                            TEXTVIEW_DRAWABLE_INDICATOR,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Music",
                                            "Enable music player in the app.",
                                            MUSIC,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Disable Native Crash Handler",
                                            "Disable native crash handler of the app and let system handle crash reports.",
                                            CRASH_HANDLER,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Disable Image Caching",
                                            "Disable image caching to save memory but at the cost of higher CPU usage due to regeneration " +
                                                    "of all image data everytime they\'re loaded.",
                                            IMAGE_CACHING,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Debug Mode",
                                            "Enable debug messages in the app to help with debugging and finding bugs.",
                                            DEBUG_MODE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Hover Animation",
                                            "Enable scale animation on hover on all views in the app.",
                                            HOVER_ANIMATION,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Center Bottom Menu",
                                            "Center gravity for the bottom menus in the app.",
                                            CENTER_BOTTOM_MENU,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Alternative Battery Optimization Switch",
                                            "Enable alternative battery optimization switcher popup in the app.",
                                            ALTERNATIVE_BATTERY_OPTIMIZATION_SWITCH,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Load Album Art From File",
                                            "Load album art from file instead of using MediaStore.\n\nThis will increase the memory and time " +
                                                    "taken to load album art but " +
                                                    "will significantly improve the album art quality and animation performance.",
                                            LOAD_ALBUM_ART_FROM_FILE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Old Style Usage Stats Panel",
                                            "Use old raw data style usage stats panel instead of the current one.",
                                            USE_OLD_STYLE_USAGE_STATS_PANEL,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Add Bitmap To Metadata",
                                            "Add bitmap to the metadata of MusicPlayerService. " +
                                                    "Useful for ROMs that shows Album Art on Lock Screen or " +
                                                    "if widgets are not showing the album art.",
                                            ADD_BITMAP_TO_METADATA,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Disable Transparent Status",
                                            "Disable transparent status bar in the app. This will make the status bar opaque.",
                                            DISABLE_TRANSPARENT_STATUS,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Load All Installer Pages",
                                            "Load all installer pages at once, reduces initial performance but improves scrolling performance.",
                                            LOAD_ALL_INSTALLER_PAGES,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Notch Area",
                                            "Enable notch area in the app. This will make the app render in the notch" +
                                                    " cutout area (Android Version >= P 9.0)." +
                                                    "\n\n" +
                                                    "May cause navigation bar overlapping issue.",
                                            IS_NOTCH_AREA_ENABLED,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Text Selection",
                                            "Enable selection on texts in Details panels\n\nCaution: causes various issues with the app.",
                                            IS_TEXT_SELECTABLE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Show Greeting In Terminal",
                                            "Show greeting in terminal STDOUT when terminal is opened.",
                                            SHOW_GREETING_IN_TERMINAL,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Old Style Scrolling Behavior Dialogs",
                                            "Use old style scrolling behavior dialog instead of the current one.",
                                            OLD_STYLE_SCROLLING_BEHAVIOR_DIALOG,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Alternate Audio Player Interface",
                                            "Use alternate audio player interface with full screen album art and lyrics view\n\nOnly portrait mode is supported.",
                                            USE_ALTERNATE_AUDIO_PLAYER_INTERFACE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Show Complete App Size",
                                            "Show complete app size including cache, data, obb, etc. in the various panels of the app.\n\nCan cause some performance issues.",
                                            SHOW_COMPLETE_APP_SIZE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Padding Less Popup Menus",
                                            "Remove padding from popup menus.",
                                            PADDING_LESS_POPUP_MENUS,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Hide Divider On Navigation Bar",
                                            "Hide divider on navigation bar in the app (Android Version >= P 9.0).",
                                            DIVIDER_ON_NAVIGATION_BAR,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Pause Image Loader",
                                            "Pause image loader in the app when fast scroller is being dragged to improve list performance.",
                                            PAUSE_IMAGE_LOADER,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Expand Home Header",
                                            "Expand header in the home screen of the app.",
                                            EXPAND_HOME_HEADER,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Colorful Highlight",
                                            "Use colorful static backgrounds in the enu containers as well if colorful icons are enabled with static backgrounds.",
                                            USE_COLORFUL_HIGHLIGHT,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Peristyle Interface",
                                            "Use basic Peristyle Interface in the music panel of the app.",
                                            USE_PERISTYLE_INTERFACE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Corresponding Color while Highlighting",
                                            "Use corresponding color on highlight buttons instead of the default accent color in Debloat panel.",
                                            USE_CORRESPONDING_COLOR_ON_HIGHLIGHT,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Blur Between Panels",
                                            "Use blur effects while switching between panels in the app. It's available in" +
                                                    " Android 12+ only. Highly unstable, please use it with caution and on your own discretion.",
                                            USE_BLUR_BETWEEN_PANELS,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Clear Search State",
                                            "Clear various search states everytime app is launched.",
                                            CLEAR_SEARCH_STATE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Test Predictive Back Gesture",
                                            "Enable test mode for predictive back gestures in the app. Restart the app after enabling.",
                                            TEST_PREDICTIVE_BACK_GESTURE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Refresh Apps List Using Launcher Service",
                                            "Automatically refresh apps' list using launcher service whenever any state of any app changes in the device.",
                                            REFRESH_APPS_LIST_USING_LAUNCHER_SERVICE,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Hide Chips Checked Icon",
                                            "Hide the checked icon in the chips in the app. This should make the chips look cleaner.",
                                            HIDE_CHIPS_CHECKED_ICON,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN)

                //                DevelopmentPreferencesModel("Enable Fancy Drag in Switch",
                //                                            "Drag switch to any position and not just left and right.",
                //                                            isSwitchFancyDraggable,
                //                                            DevelopmentPreferencesModel.TYPE_BOOLEAN)
        ).sortedBy {
            it.title
        }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun get(key: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(key, false)
    }

    fun set(key: String, value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(key, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //
}
