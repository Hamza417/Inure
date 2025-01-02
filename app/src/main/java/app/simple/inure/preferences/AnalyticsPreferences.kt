package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant

object AnalyticsPreferences {

    const val SDK_VALUE = "analytics_sdk_value"
    const val PIE_HOLE_RADIUS = "pie_hole_radius"
    const val APPLICATION_TYPE = "application_type_analytics"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSDKValue(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(SDK_VALUE, value).apply()
    }

    fun getSDKValue(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(SDK_VALUE, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setPieHoleRadiusValue(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(PIE_HOLE_RADIUS, value).apply()
    }

    fun getPieHoleRadiusValue(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(PIE_HOLE_RADIUS, 50F)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setApplicationType(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(APPLICATION_TYPE, value).apply()
    }

    fun getApplicationType(): String {
        return SharedPreferences.getSharedPreferences().getString(APPLICATION_TYPE, SortConstant.BOTH)!!
    }
}
