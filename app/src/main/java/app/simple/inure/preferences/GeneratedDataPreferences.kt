package app.simple.inure.preferences

object GeneratedDataPreferences {

    const val generatedDataType = "generated_data_type"
    private const val generatorFlags = "generator_flags_a"

    const val NAME = 1
    const val PACKAGE_NAME = 2
    const val VERSION = 4
    const val INSTALL_DATE = 8
    const val UPDATE_DATE = 16
    const val MINIMUM_SDK = 32
    const val TARGET_SDK = 64
    const val SIZE = 128
    const val PLAY_STORE = 256
    const val FDROID = 512

    const val DEFAULT_FLAGS = NAME or
            PACKAGE_NAME or
            VERSION

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

    fun setGeneratorFlags(flags: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(generatorFlags, flags).apply()
    }

    fun getGeneratorFlags(): Int {
        return SharedPreferences.getSharedPreferences().getInt(generatorFlags, DEFAULT_FLAGS)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun reset() {
        setGeneratedDataType(TXT)
    }
}