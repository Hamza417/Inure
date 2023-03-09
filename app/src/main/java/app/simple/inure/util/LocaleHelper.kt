package app.simple.inure.util

import android.content.res.Resources
import android.os.Build
import android.view.View
import app.simple.inure.models.Locales
import app.simple.inure.preferences.ConfigurationPreferences
import java.util.*

object LocaleHelper {

    private var appLocale = Locale.getDefault()

    /**
     * Code for russian locale
     */
    private const val russianLocale = "ru-RU"

    fun getSystemLanguageCode(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0].language
        } else {
            @Suppress("deprecation")
            Resources.getSystem().configuration.locale.language
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
            // Auto detect language (default)
            Locales("autoSystemLanguageString" /* Placeholder */, "default"),
            // English (United States)
            Locales("English (US)", "en-US"),
            // Traditional Chinese (Taiwan)
            Locales("漢語 (Traditional Chinese)", "zh-Hant-TW"),
            // Russian
            Locales("Русский (Russian)", "ru-RU"),
            // Italian
            Locales("Italiano (Italian)", "it-IT"),
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

    fun isAppRussianLocale(): Boolean {
        return ConfigurationPreferences.getAppLanguage() == russianLocale
    }
}
