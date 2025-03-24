package app.simple.inure.preferences

import androidx.core.content.edit

object InstallerPreferences {

    const val IS_CHANGES_VISIBLE = "isChangesVisible"
    const val IS_PERMISSIONS_VISIBLE = "isPermissionsVisible"
    const val IS_INFO_VISIBLE = "isInfoVisible"
    const val IS_CERTIFICATE_VISIBLE = "isCertificateVisible"
    const val IS_MANIFEST_VISIBLE = "isManifestVisible"
    const val IS_TRACKERS_VISIBLE = "isTrackersVisible"
    const val IS_DIFF_STYLE_CHANGES = "isDiffStyleChanges"

    // ---------------------------------------------------------------------------------------------------------- //

    fun getPanelVisibility(key: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(key, true)
    }

    fun setPanelVisibility(key: String, value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(key, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isDiffStyleChanges(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_DIFF_STYLE_CHANGES, false)
    }

    fun setDiffStyleChanges(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(IS_DIFF_STYLE_CHANGES, value) }
    }
}
