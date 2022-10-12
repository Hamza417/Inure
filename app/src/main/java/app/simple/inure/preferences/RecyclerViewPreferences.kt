package app.simple.inure.preferences

object RecyclerViewPreferences {

    private const val viewTag = "view_tag"
    private const val position = "view_positions"

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

}