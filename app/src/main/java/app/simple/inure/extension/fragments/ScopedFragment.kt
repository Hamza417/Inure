package app.simple.inure.extension.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import app.simple.inure.decorations.transitions.DetailsTransition
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.CoroutineScope

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
     * [postponeEnterTransition] here and initialize all the
     * views in [onCreateView] with proper transition names
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        appUninstallObserver = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    onAppUninstalled(true, result.data)
                    onAppUninstalled(true)
                }
                Activity.RESULT_CANCELED -> {
                    onAppUninstalled(false, result.data)
                    onAppUninstalled(false)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
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
    open fun onAppUninstalled(@NonNull result: Boolean, data: Intent?) {}
    open fun onAppUninstalled(@NonNull result: Boolean) {}

    /**
     * clears the [setExitTransition] for the current fragment in support
     * for making the custom animations work for the fragments that needs
     * to originate from the current fragment
     */
    open fun clearExitTransition() {
        exitTransition = null
    }

    open fun clearEnterTransition() {
        enterTransition = null
    }

    /**
     * Sets fragment transitions prior to creating a new fragment.
     * Used with shared elements
     */
    open fun setTransitions() {
        /**
         * Animations are expensive, every time a view is added into the
         * animating view transaction time will increase a little
         * making the interaction a little bit slow.
         */
        if (BehaviourPreferences.isTransitionOn()) {
            exitTransition = Fade()
            enterTransition = Fade()
        } else {
            clearExitTransition()
            clearEnterTransition()
        }

        if (BehaviourPreferences.isAnimationOn()) {
            sharedElementEnterTransition = DetailsTransitionArc()
            sharedElementReturnTransition = DetailsTransitionArc()
        }
    }

    open fun setLinearTransitions() {
        /**
         * Animations are expensive, every time a view is added into the
         * animating view transaction time will increase a little
         * making the interaction a little bit slow.
         */
        if (BehaviourPreferences.isTransitionOn()) {
            exitTransition = Fade()
            enterTransition = Fade()
        } else {
            clearExitTransition()
            clearEnterTransition()
        }

        if (BehaviourPreferences.isAnimationOn()) {
            sharedElementEnterTransition = DetailsTransition()
            sharedElementReturnTransition = DetailsTransition()
        }
    }
}
