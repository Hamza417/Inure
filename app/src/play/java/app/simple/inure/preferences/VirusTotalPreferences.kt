package app.simple.inure.preferences

object VirusTotalPreferences {
    fun setVirusTotalApiKey(value: String) {}
    fun getVirusTotalApiKey(): String = ""
    fun String.validateAPI(): Boolean = false
    fun hasValidAPI(): Boolean = false
    fun setLoaderType(value: Int) {}
    fun getLoaderType(): Int = 0
}