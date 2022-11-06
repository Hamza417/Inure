package app.simple.inure.constants

@Suppress("unused")
object Warnings {

    /**
     * InureWarning01: Cannot establish a root connection with the main shell.
     */
    fun getInureWarning01(): String = "InureWarning01: Cannot establish a root connection with the main shell."

    /**
     * InureWarning02: Failed to load the bitmap
     */
    fun getInureWarning02(path: String): String = "InureWarning02: Failed to load bitmap from $path"
}