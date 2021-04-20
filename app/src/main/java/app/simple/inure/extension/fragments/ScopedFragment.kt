package app.simple.inure.extension.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * [ScopedFragment] is lifecycle aware [CoroutineScope] fragment
 * used to bind independent coroutines with the lifecycle of
 * the given fragment. All [Fragment] classes must extend
 * this class instead.
 *
 * It is recommended to read this code before implementing to know
 * its purpose and importance
 */
abstract class ScopedFragment : Fragment(), CoroutineScope, SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Get the job instance here, must be a final value
     */
    private val job = Job()

    /**
     * Use the job instance and attach it the [Dispatchers.Main]
     * which will be the main thread of the given app
     * instance
     */
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    /**
     * [postponeEnterTransition] here and initialize all the
     * views in [onCreateView] with proper transition names
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Cancel the job instance here, since
     * we have attached the [Job] with [Dispatchers.Main]
     * it will cancel all the coroutines of the given instance
     * and this way coroutines won't last more than the
     * lifecycle itself
     */
    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        job.cancel()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        onPreferencesChanged(sharedPreferences, key)
    }


    /**
     * Called when any preferences is changed using [getSharedPreferences]
     */
    abstract fun onPreferencesChanged(sharedPreferences: SharedPreferences?, key: String?)
}
