package app.simple.inure.themes.manager

object ThemeManager {

    const val light = 0
    const val dark = 1
    const val amoled = 2
    const val followSystem = 3
    const val dayNight = 4

    private val listeners = mutableSetOf<ThemeChangedListener>()

    var theme = Theme.LIGHT
        set(value) {
            field = value
            listeners.forEach { listener -> listener.onThemeChanged(value) }
        }

    fun addListener(listener: ThemeChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ThemeChangedListener) {
        listeners.remove(listener)
    }

}