package app.simple.inure.preferences

import app.simple.inure.models.DevelopmentPreferencesModel

object DevelopmentPreferences {

    const val isWebViewXmlViewer = "is_xml_viewer_web_view"
    const val preferencesIndicator = "is_preferences_indicator_hidden"
    const val crashHandler = "is_using_native_crash_handler"
    const val music = "is_music_enabled"
    const val imageCaching = "is_image_caching_enabled"
    const val debugMode = "is_debug_messages_enabled"
    const val hoverAnimation = "is_hover_animation_enabled"
    const val centerBottomMenu = "is_center_bottom_menu_enabled"
    const val alternativeBatteryOptimizationSwitch = "is_alternative_battery_optimization_switch_enabled"
    const val loadAlbumArtFromFile = "is_album_art_loaded_from_file"
    const val useOldStyleUsageStatsPanel = "is_old_style_usage_stats_panel_enabled"
    const val enableDeviceInfo = "is_device_info_enabled"
    const val addBitmapToMetadata = "is_bitmap_added_to_metadata"
    const val enableHiddenApps = "is_hidden_apps_enabled"
    const val transparentStatus = "is_transparent_status_disabled_removed"
    const val loadAllInstallerPages = "is_all_installer_pages_loaded"

    val developmentPreferences: List<DevelopmentPreferencesModel> by lazy {
        listOf(
                DevelopmentPreferencesModel("Use WebView for XML Preview",
                                            "Use WebView for XML Preview instead of TextView.",
                                            isWebViewXmlViewer,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Hide Preferences Indicator",
                                            "Hides the indicators in the settings and dialogs.",
                                            preferencesIndicator,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Music",
                                            "Enable music player in the app.",
                                            music,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Disable Native Crash Handler",
                                            "Disable native crash handler of the app and let system handle crash reports.",
                                            crashHandler,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Disable Image Caching",
                                            "Disable image caching to save memory but at the cost of higher CPU usage due to regeneration " +
                                                    "of all image data everytime they\'re loaded.",
                                            imageCaching,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Debug Mode",
                                            "Enable debug messages in the app to help with debugging and finding bugs.",
                                            debugMode,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Hover Animation",
                                            "Enable scale animation on hover on all views in the app.",
                                            hoverAnimation,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Center Bottom Menu",
                                            "Center gravity for the bottom menus in the app.",
                                            centerBottomMenu,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Alternative Battery Optimization Switch",
                                            "Enable alternative battery optimization switcher popup in the app.",
                                            alternativeBatteryOptimizationSwitch,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Load Album Art From File",
                                            "Load album art from file instead of using MediaStore.\n\nThis will increase the memory and time " +
                                                    "taken to load album art but " +
                                                    "will significantly improve the album art quality and animation performance.",
                                            loadAlbumArtFromFile,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Use Old Style Usage Stats Panel",
                                            "Use old raw data style usage stats panel instead of the current one.",
                                            useOldStyleUsageStatsPanel,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Device Info",
                                            "Enable Device Info panel in the app. It was planned but never implemented.",
                                            enableDeviceInfo,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Add Bitmap To Metadata",
                                            "Add bitmap to the metadata of MusicPlayerService. Useful for ROMs that shows Album Art on Lock Screen.",
                                            addBitmapToMetadata,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Enable Hidden Apps",
                                            "Enable Hidden Apps in the app. The feature was added but removed due to API changes by" +
                                                    " Google and is not working properly anymore.",
                                            enableHiddenApps,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Disable Transparent Status",
                                            "Disable transparent status bar in the app. This will make the status bar opaque.",
                                            transparentStatus,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN),

                DevelopmentPreferencesModel("Load All Installer Pages",
                                            "Load all installer pages at once, reduces initial performance but improves scrolling performance.",
                                            loadAllInstallerPages,
                                            DevelopmentPreferencesModel.TYPE_BOOLEAN)
        ).sortedBy {
            it.title
        }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun get(key: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(key, false)
    }

    fun set(key: String, value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(key, value).apply()
    }

    // ---------------------------------------------------------------------------------------------------------- //
}
