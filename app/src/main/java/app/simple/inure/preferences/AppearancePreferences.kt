package app.simple.inure.preferences

import androidx.annotation.IntRange
import androidx.annotation.NonNull
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object AppearancePreferences {

    private const val appCornerRadius = "corner_radius"
    private const val dimWindows = "is_dimming_windows_on"
    private const val shadows = "are_shadows_on"

    const val accentColor = "app_accent_color"
    const val appFont = "type_face"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentColor(int: Int) {
        getSharedPreferences().edit().putInt(accentColor, int).apply()
    }

    fun getAccentColor(): Int {
        return getSharedPreferences().getInt(accentColor, 0)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppFont(@NonNull font: String) {
        getSharedPreferences().edit().putString(appFont, font).apply()
    }

    fun getAppFont(): String {
        return getSharedPreferences().getString(appFont, TypeFaceTextView.JOST)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCornerRadius(@IntRange(from = 25, to = 400) radius: Int) {
        getSharedPreferences().edit().putInt(appCornerRadius, radius / 5).apply()
    }

    fun getCornerRadius(): Int {
        return getSharedPreferences().getInt(appCornerRadius, 60)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDimWindows(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(dimWindows, boolean).apply()
    }

    fun isDimmingOn(): Boolean {
        return getSharedPreferences().getBoolean(dimWindows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShadows(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(shadows, boolean).apply()
    }

    fun areShadowsOn(): Boolean {
        return getSharedPreferences().getBoolean(shadows, true)
    }
}