package app.simple.inure.extensions.fragments

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.IntegerRes
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.transitions.DetailsTransition
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.dialogs.miscellaneous.Loader
import app.simple.inure.dialogs.miscellaneous.Warning
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.CoroutineScope

/**
 * [ScopedFragment] is lifecycle aware [CoroutineScope] fragment
 * used to bind independent coroutines with the lifecycle of
 * the given fragment. All [Fragment] extension classes must extend
 * this class instead.
 *
 * It is recommended to read this code before implementing to know
 * its purpose and importance
 */
abstract class ScopedFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

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
    lateinit var packageInfo: PackageInfo

    /**
     * Fragments own loader instance
     */
    protected var loader: Loader? = null

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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
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

    /**
     * Sets fragment transitions prior to creating a new fragment.
     * Used with shared elements
     *
     * @param duration duration of the transition
     */
    open fun setTransitions(duration: Long) {
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
            sharedElementEnterTransition = DetailsTransitionArc(duration)
            sharedElementReturnTransition = DetailsTransitionArc(duration)
        }
    }

    open fun clearTransitions() {
        clearEnterTransition()
        clearExitTransition()
    }

    open fun setLinearTransitions(duration: Long) {

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
            sharedElementEnterTransition = DetailsTransition(duration)
            sharedElementReturnTransition = DetailsTransition(duration)
        }
    }

    open fun showLoader() {
        if (requireArguments().getBoolean(BundleConstants.loading)) {
            loader = Loader.newInstance()
            loader?.show(childFragmentManager, "loader")
        }
    }

    @Throws(IllegalStateException::class)
    open fun hideLoader() {
        loader?.dismiss()
    }

    open fun showWarning(warning: String) {
        val p0 = Warning.newInstance(warning)
        p0.setOnWarningCallbackListener(object : Warning.Companion.WarningCallbacks {
            override fun onDismiss() {
                requireActivity().onBackPressed()
            }
        })
        p0.show(childFragmentManager, "warning")
    }

    open fun showWarning(@IntegerRes warning: Int) {
        val p0 = Warning.newInstance(warning)
        p0.setOnWarningCallbackListener(object : Warning.Companion.WarningCallbacks {
            override fun onDismiss() {
                requireActivity().onBackPressed()
            }
        })
        p0.show(childFragmentManager, "warning")
    }

    open fun showError(error: String) {
        val e = Error.newInstance(error)
        e.show(childFragmentManager, "error_dialog")
        e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
            override fun onDismiss() {
                requireActivity().onBackPressed()
            }
        })
    }

    /**
     * Return the {@link Application} this fragment is currently associated with.
     */
    protected fun requireApplication(): Application {
        return requireActivity().application
    }

    protected fun requirePackageManager(): PackageManager {
        return requireActivity().packageManager
    }

    protected fun getInteger(resId: Int): Int {
        return resources.getInteger(resId)
    }

    /**
     * Open fragment using slide animation
     *
     * @param fragment [Fragment]
     * @param tag back stack tag for fragment
     */
    protected fun openFragment(fragment: ScopedFragment, @Nullable tag: String?) {
        clearExitTransition()

        requireActivity().supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(R.id.app_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }
}