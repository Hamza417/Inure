package app.simple.inure.preferences

import android.content.Context
import androidx.core.content.edit

object InstallerPreferences {

    const val IS_CHANGES_VISIBLE = "isChangesVisible"
    const val IS_PERMISSIONS_VISIBLE = "isPermissionsVisible"
    const val IS_INFO_VISIBLE = "isInfoVisible"
    const val IS_CERTIFICATE_VISIBLE = "isCertificateVisible"
    const val IS_MANIFEST_VISIBLE = "isManifestVisible"
    const val IS_TRACKERS_VISIBLE = "isTrackersVisible"
    const val IS_DIFF_STYLE_CHANGES = "isDiffStyleChanges"

    private const val INSTALLER_PACKAGE_NAME = "installer_package_name"
    private const val INSTALLER_GRANT_RUNTIME_PERMISSIONS = "installer_grant_runtime_permissions"
    private const val INSTALLER_VERSION_CODE_DOWNGRADE = "installer_version_code_downgrade"
    private const val INSTALLER_TEST_PACKAGES = "installer_test_packages"
    private const val INSTALLER_BYPASS_LOW_TARGET_SDK = "installer_bypass_low_target_sdk"
    private const val INSTALLER_REPLACE_EXISTING = "installer_replace_existing"
    private const val INSTALLER_DONT_KILL = "installer_dont_kill"

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

    // ---------------------------------------------------------------------------------------------------------- //

    fun getInstallerPackageName(context: Context): String {
        return SharedPreferences.getSharedPreferences()
            .getString(INSTALLER_PACKAGE_NAME, context.packageName) ?: context.packageName
    }

    fun setInstallerPackageName(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(INSTALLER_PACKAGE_NAME, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isGrantRuntimePermissions(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(INSTALLER_GRANT_RUNTIME_PERMISSIONS, false)
    }

    fun setGrantRuntimePermissions(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(INSTALLER_GRANT_RUNTIME_PERMISSIONS, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isVersionCodeDowngrade(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(INSTALLER_VERSION_CODE_DOWNGRADE, false)
    }

    fun setVersionCodeDowngrade(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(INSTALLER_VERSION_CODE_DOWNGRADE, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isTestPackages(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(INSTALLER_TEST_PACKAGES, false)
    }

    fun setTestPackages(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(INSTALLER_TEST_PACKAGES, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isBypassLowTargetSdk(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(INSTALLER_BYPASS_LOW_TARGET_SDK, true)
    }

    fun setBypassLowTargetSdk(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(INSTALLER_BYPASS_LOW_TARGET_SDK, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isReplaceExisting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(INSTALLER_REPLACE_EXISTING, true)
    }

    fun setReplaceExisting(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(INSTALLER_REPLACE_EXISTING, value) }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isDontKill(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(INSTALLER_DONT_KILL, false)
    }

    fun setDontKill(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit { putBoolean(INSTALLER_DONT_KILL, value) }
    }
}
