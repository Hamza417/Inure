package app.simple.inure.preferences

object AnalyticsPreferences {

    const val sdkValue = "analytics_sdk_value"
    const val pieHoleRadius = "pie_hole_radius"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSDKValue(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(sdkValue, value).apply()
    }

    fun getSDKValue(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(sdkValue, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setPieHoleRadiusValue(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(pieHoleRadius, value).apply()
    }

    fun getPieHoleRadiusValue(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(pieHoleRadius, 50F)
    }

}