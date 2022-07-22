package app.simple.inure.themes.manager

import app.simple.inure.themes.interfaces.ThemeChangedListener

object ThemeManager {

    private val listeners = mutableSetOf<ThemeChangedListener>()

    var theme = Theme.LIGHT
        set(value) {
            val bool = field != value
            field = value
            listeners.forEach { listener -> listener.onThemeChanged(value, bool) }
        }

    var accent = Accent.INURE
        set(value) {
            field = value
            listeners.forEach { it.onAccentChanged(accent) }
        }

    fun addListener(listener: ThemeChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ThemeChangedListener) {
        listeners.remove(listener)
    }
}