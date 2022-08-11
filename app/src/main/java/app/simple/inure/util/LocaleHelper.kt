package app.simple.inure.util

import android.content.res.Resources
import android.os.Build
import android.view.View
import app.simple.inure.models.Locales
import java.util.*

object LocaleHelper {

    private var appLocale = Locale.getDefault()

    fun getSystemLanguageCode(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0].language
        } else {
            "default"
        }
    }

    /**
     * List of languages currently supported by
     * the app.
     *
     * Do not include incomplete translations.
     *
     * Use Java 8's [Locale] codes and not Android's:
     * https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html
     */
    val localeList = arrayListOf(
            Locales("autoSystemLanguageString" /* Placeholder */, "default"),
            Locales("English (US)", "en"),
            // Locales("български", "bg"),
            // Locales("Français", "fr"),
            // Locales("Čeština", "cs"),
            // Locales("हिन्दी", "hi"),
            // Locales("Română", "ro"),
            // Locales("Русский", "ru"),
            // Locales("اردو", "ur"))
            Locales("漢語 (Traditional Chinese)", "zh-TW")
    )

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
