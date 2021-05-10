package app.simple.inure.extension.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.transition.Fade
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * [ScopedFragment] is lifecycle aware [CoroutineScope] fragment
 * used to bind independent coroutines with the lifecycle of
 * the given fragment. All [Fragment] extension classes must extend
 * this class instead. *
 * It is recommended to read this code before implementing to know
 * its purpose and importance
 */
abstract class ScopedFragment :
    Fragment(),
    CoroutineScope,
    SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Use this to launch app uninstall intent and listen to the results
     * via [onAppUninstalled] function.
     */
    lateinit var appUninstallObserver: ActivityResultLauncher<Intent>

    /**
     * [ScopedFragment]'s own [Handler] instance
     */
    val handler = Handler(Looper.getMainLooper())

    /**
     * [ScopedFragment]'s own [ApplicationInfo] instance, needs
     * to be initialized before use
     *
     * @throws UninitializedPropertyAccessException
     */
    lateinit var applicationInfo: ApplicationInfo

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

        appUninstallObserver = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    onAppUninstalled(true)
                }
                Activity.RESULT_CANCELED -> {
                    onAppUninstalled(false)
                }
            }
        }
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

    /**
     * Called when any preferences is changed using [getSharedPreferences]
     *
     * Override this to get any preferences change events inside
     * the fragment
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    /**
     * Callback occurs when an app is uninstalled
     * @param result true when app is successfully uninstalled
     *               else false for any other result be it cancelled
     *               or failed
     */
    open fun onAppUninstalled(@NonNull result: Boolean) {}

    /**
     * clears the [setExitTransition] for the current fragment in support
     * for making the custom animations work for the fragments that needs
     * to originate from the current fragment
     */
    open fun clearExitTransition() {
        exitTransition = null
    }

    open fun setExitTransition() {
        exitTransition = Fade()
    }

    open fun setTransitions() {
        exitTransition = Fade()
        enterTransition = Fade()
        sharedElementEnterTransition = DetailsTransitionArc()
        sharedElementReturnTransition = DetailsTransitionArc()
    }
}
