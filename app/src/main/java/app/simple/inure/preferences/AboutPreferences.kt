package app.simple.inure.preferences

object AboutPreferences {

    private const val SHARE_MESSAGE = "last_share_message"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShareMessage(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(SHARE_MESSAGE, value).apply()
    }

    fun getShareMessage(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(SHARE_MESSAGE,
                       "Let me recommend you this really good looking applications manager app.")!!
    }

}
