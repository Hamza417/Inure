package app.simple.inure.util

import android.content.res.Resources
import android.view.View
import java.util.*

object LocaleHelper {

    private var appLocale = Locale.getDefault()

    fun getSystemLanguageCode(): String {
        return Resources.getSystem().configuration.locales[0].language
    }

    fun getAppLocale(): Locale {
        return synchronized(this) {
            appLocale
        }
    }

    fun setAppLocale(value: Locale) {
        synchronized(this) {
            appLocale = value
        }
    }

    fun Resources.isRTL(): Boolean {
        return configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }
}
