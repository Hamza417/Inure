package app.simple.inure.preferences

import android.content.Context
import android.content.SharedPreferences
import app.simple.inure.util.NullSafety.isNull
import dev.spght.encryptedprefs.EncryptedSharedPreferences
import dev.spght.encryptedprefs.MasterKey
import java.io.File

object SharedPreferences {

    private const val PREFERENCES = "Preferences"
    private const val PREFERENCES_ENCRYPTED = "PreferencesSecured"
    private var sharedPreferences: SharedPreferences? = null
    private var encryptedSharedPreferences: SharedPreferences? = null

    private var isInitialized = false

    fun init(context: Context) {
        if (sharedPreferences.isNull()) {
            sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        }
    }

    fun initEncrypted(context: Context) {
        kotlin.runCatching {
            if (encryptedSharedPreferences.isNull()) {
                val masterKeyAlias = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                encryptedSharedPreferences = EncryptedSharedPreferences.create(
                        context,
                        PREFERENCES_ENCRYPTED,
                        masterKeyAlias,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
            }
        }.onFailure {
            /**
             * Retry with a fail safe for recursion
             */
            if (isInitialized.not()) {
                // Delete the encrypted shared preferences if it fails to initialize
                File(getEncryptedSharedPreferencesPath(context)).delete()
                initEncrypted(context)
                isInitialized = true
            }
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

    fun getSharedPreference(context: Context): SharedPreferences {
        init(context)
        return sharedPreferences ?: throw NullPointerException()
    }

    fun getEncryptedSharedPreferences(): SharedPreferences {
        return encryptedSharedPreferences ?: throw NullPointerException()
    }

    fun registerSharedPreferencesListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Use this function to register shared preference change listener if
     * the current context has [SharedPreferences.OnSharedPreferenceChangeListener]
     * implemented.
     */
    fun SharedPreferences.OnSharedPreferenceChangeListener.registerSharedPreferenceChangeListener() {
        registerSharedPreferencesListener(this)
    }

    /**
     * Use this function to unregister shared preference change listener if
     * the current context has [SharedPreferences.OnSharedPreferenceChangeListener]
     * implemented.
     */
    fun SharedPreferences.OnSharedPreferenceChangeListener.unregisterSharedPreferenceChangeListener() {
        unregisterListener(this)
    }

    fun registerEncryptedSharedPreferencesListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getEncryptedSharedPreferences().registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterEncryptedSharedPreferencesListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getEncryptedSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener)
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
        return context.applicationInfo.dataDir + "/shared_prefs/" + PREFERENCES + ".xml"
    }

    fun getEncryptedSharedPreferencesPath(context: Context): String {
        return context.applicationInfo.dataDir + "/shared_prefs/" + PREFERENCES_ENCRYPTED + ".xml"
    }
}
