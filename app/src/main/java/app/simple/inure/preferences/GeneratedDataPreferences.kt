package app.simple.inure.preferences

object GeneratedDataPreferences {

    const val generatedDataType = "generated_data_type"
    private const val generatorFlags = "generator_flags_e"

    const val NAME = 1L shl 1
    const val PACKAGE_NAME = 1L shl 2
    const val VERSION = 1L shl 3
    const val INSTALL_DATE = 1L shl 4
    const val UPDATE_DATE = 1L shl 5
    const val MINIMUM_SDK = 1L shl 6
    const val TARGET_SDK = 1L shl 7
    const val SIZE = 1L shl 8
    const val PLAY_STORE = 1L shl 9
    const val FDROID = 1L shl 10
    const val AMAZON_STORE = 1L shl 11
    const val GALAXY_STORE = 1L shl 12

    private const val DEFAULT_FLAGS = NAME or
            PACKAGE_NAME or
            VERSION or
            PLAY_STORE or
            FDROID

    const val TXT = "txt"
    const val XML = "xml"
    const val MD = "md"
    const val HTML = "html"
    const val CSV = "csv"
    const val JSON = "json"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedDataType(type: String) {
        SharedPreferences.getSharedPreferences().edit().putString(generatedDataType, type).apply()
    }

    fun getGeneratedDataType(): String {
        return SharedPreferences.getSharedPreferences().getString(generatedDataType, TXT)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratorFlags(flags: Long) {
        SharedPreferences.getSharedPreferences().edit().putLong(generatorFlags, flags).apply()
    }

    fun getGeneratorFlags(): Long {
        return SharedPreferences.getSharedPreferences().getLong(generatorFlags, DEFAULT_FLAGS)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun reset() {
        setGeneratedDataType(TXT)
    }
}