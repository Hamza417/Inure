package app.simple.inure.preferences

object InstallerPreferences {

    const val IS_CHANGES_VISIBLE = "isChangesVisible"
    const val IS_PERMISSIONS_VISIBLE = "isPermissionsVisible"
    const val IS_INFO_VISIBLE = "isInfoVisible"
    const val IS_CERTIFICATE_VISIBLE = "isCertificateVisible"
    const val IS_MANIFEST_VISIBLE = "isManifestVisible"
    const val IS_TRACKERS_VISIBLE = "isTrackersVisible"

    // ---------------------------------------------------------------------------------------------------------- //

    fun getPanelVisibility(key: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(key, true)
    }

    fun setPanelVisibility(key: String, value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(key, value).apply()
    }
}
