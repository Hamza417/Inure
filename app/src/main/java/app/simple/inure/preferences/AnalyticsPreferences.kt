package app.simple.inure.preferences

object AnalyticsPreferences {

    const val sdkValue = "analytics_sdk_value"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSDKValue(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(sdkValue, value).apply()
    }

    fun getSDKValue(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(sdkValue, true)
    }

}