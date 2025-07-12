package app.simple.inure.adapters.terminal

object Constants {
    private val keyCodeMap = mapOf(
            "Jog Ball" to 23, // Example keycode for D-Pad Center
            "@ (Address Sign)" to 77, // Example keycode for '@'
            "Left Alt" to 57, // Keycode for Left Alt
            "Right Alt" to 58, // Keycode for Right Alt
            "Volume Up" to 24, // Keycode for Volume Up
            "Volume Down" to 25, // Keycode for Volume Down
            "Camera" to 27, // Keycode for Camera
            "None" to -1 // No keycode
    )

    fun getKeyList(): List<String> {
        return keyCodeMap.keys.toList()
    }

    fun getKeyCode(key: String): Int {
        return keyCodeMap[key] ?: -1 // Return -1 if the key is not found
    }
}