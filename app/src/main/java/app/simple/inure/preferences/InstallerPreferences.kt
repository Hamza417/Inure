package app.simple.inure.preferences

object InstallerPreferences {

    const val isChangesVisible = "isChangesVisible"
    const val isPermissionsVisible = "isPermissionsVisible"
    const val isInfoVisible = "isInfoVisible"
    const val isCertificateVisible = "isCertificateVisible"
    const val isManifestVisible = "isManifestVisible"
    const val isTrackersVisible = "isTrackersVisible"

    // ---------------------------------------------------------------------------------------------------------- //

    fun getPanelVisibility(key: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(key, true)
    }

    fun setPanelVisibility(key: String, value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(key, value).apply()
    }
}