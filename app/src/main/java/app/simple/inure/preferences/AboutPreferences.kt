package app.simple.inure.preferences

object AboutPreferences {

    private const val shareMessage = "last_share_message"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShareMessage(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(shareMessage, value).apply()
    }

    fun getShareMessage(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(shareMessage,
                       "Let me recommend you this really good looking applications manager app.")!!
    }

}