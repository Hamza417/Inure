package app.simple.inure.preferences

import android.content.Context
import android.content.SharedPreferences
import app.simple.inure.util.NullSafety.isNull

object SharedPreferences {

    private const val preferences = "Preferences"
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences.isNull()) {
            sharedPreferences = context.getSharedPreferences(preferences, Context.MODE_PRIVATE)
        }
    }

    /**
     * Singleton to hold reference of SharedPreference.
     * Call [init] first before making a instance request
     *
     * @see init
     * @throws NullPointerException
     */
    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences ?: throw NullPointerException()
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Singleton to hold reference of SharedPreference.
     *
     * @see init
     */
    fun getSharedPreferences(context: Context): SharedPreferences {
        kotlin.runCatching {
            return sharedPreferences ?: throw NullPointerException()
        }.getOrElse {
            init(context)
            return sharedPreferences!!
        }
    }

    fun getSharedPreferencesPath(context: Context): String {
        return context.applicationInfo.dataDir + "/shared_prefs/" + preferences + ".xml"
    }
}