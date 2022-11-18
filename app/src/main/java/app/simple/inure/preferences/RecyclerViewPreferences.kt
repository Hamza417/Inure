package app.simple.inure.preferences

object RecyclerViewPreferences {

    private const val viewTag = "view_tag"
    private const val position = "view_positions"
    private const val scaleFactor = "view_scale_factor"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setViewTag(value: String?) {
        SharedPreferences.getSharedPreferences().edit().putString(viewTag, value).apply()
    }

    fun getViewTag(): String? {
        return SharedPreferences.getSharedPreferences().getString(viewTag, null)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setViewPosition(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(position, value).apply()
    }

    fun getViewPosition(): Int {
        return SharedPreferences.getSharedPreferences().getInt(position, -1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setViewScaleFactor(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(scaleFactor, value).apply()
    }

    fun getViewScaleFactor(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(scaleFactor, 1.0f)
    }
}