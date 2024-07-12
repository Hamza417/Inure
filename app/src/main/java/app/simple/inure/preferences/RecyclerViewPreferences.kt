package app.simple.inure.preferences

object RecyclerViewPreferences {

    private const val VIEW_TAG = "view_tag"
    private const val POSITION = "view_positions"
    private const val SCALE_FACTOR = "view_scale_factor"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setViewTag(value: String?) {
        SharedPreferences.getSharedPreferences().edit().putString(VIEW_TAG, value).apply()
    }

    fun getViewTag(): String? {
        return SharedPreferences.getSharedPreferences().getString(VIEW_TAG, null)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setViewPosition(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(POSITION, value).apply()
    }

    fun getViewPosition(): Int {
        return SharedPreferences.getSharedPreferences().getInt(POSITION, -1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setViewScaleFactor(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(SCALE_FACTOR, value).apply()
    }

    fun getViewScaleFactor(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(SCALE_FACTOR, 1.0f)
    }
}
