package app.simple.inure.extensions.fragments

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.transition.ArcMotion
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.dialogs.miscellaneous.Loader
import app.simple.inure.dialogs.miscellaneous.Warning
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupTransitionType
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.ui.app.AppInfo
import app.simple.inure.ui.panels.Search
import app.simple.inure.ui.panels.WebPage
import app.simple.inure.util.NullSafety.isNotNull
import com.google.android.material.transition.*
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

    private val maximumAngle = 90
    private val minimumHorizontalAngle = 80
    private val minimumVerticalAngle = 15

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
    private var loader: Loader? = null

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
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BehaviourPreferences.transitionType -> {
                setTransitions()
            }
        }
    }

    /**
     * clears the [setExitTransition] for the current fragment in support
     * for making the custom animations work for the fragments that needs
     * to originate from the current fragment
     */
    internal fun clearExitTransition() {
        exitTransition = null
    }

    internal fun clearEnterTransition() {
        enterTransition = null
    }

    /**
     * Sets fragment transitions prior to creating a new fragment.
     * Used with shared elements
     */
    open fun setTransitions() {
        allowEnterTransitionOverlap = true
        allowReturnTransitionOverlap = true

        /**
         * Animations are expensive, every time a view is added into the
         * animating view transaction time will increase a little
         * making the interaction a little bit slow.
         */
        if (BehaviourPreferences.isTransitionOn()) {
            when (BehaviourPreferences.getTransitionType()) {
                PopupTransitionType.FADE -> {
                    exitTransition = Fade()
                    enterTransition = Fade()
                    reenterTransition = Fade()
                }
                PopupTransitionType.ELEVATION -> {
                    exitTransition = MaterialElevationScale(false)
                    enterTransition = MaterialElevationScale(true)
                    reenterTransition = MaterialElevationScale(false)
                }
                PopupTransitionType.SHARED_AXIS -> {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                }
                PopupTransitionType.THROUGH -> {
                    exitTransition = MaterialFadeThrough()
                    enterTransition = MaterialFadeThrough()
                    reenterTransition = MaterialFadeThrough()
                }
            }
        } else {
            clearExitTransition()
            clearEnterTransition()
        }
    }

    private fun clearTransitions() {
        clearEnterTransition()
        clearExitTransition()
    }

    open fun setArcTransitions(duration: Long) {
        setTransitions()

        if (BehaviourPreferences.isArcAnimationOn()) {
            sharedElementEnterTransition = MaterialContainerTransform().apply {
                setDuration(duration)
                setAllContainerColors(Color.TRANSPARENT)
                scrimColor = Color.TRANSPARENT
                when (BehaviourPreferences.getArcType()) {
                    PopupArcType.INURE -> {
                        setPathMotion(ArcMotion().apply {
                            maximumAngle = this.maximumAngle
                            minimumHorizontalAngle = this.minimumHorizontalAngle
                            minimumVerticalAngle = this.minimumVerticalAngle
                        })
                    }
                    PopupArcType.MATERIAL -> {
                        setPathMotion(MaterialArcMotion())
                    }
                }
            }
            sharedElementReturnTransition = MaterialContainerTransform().apply {
                setDuration(duration)
                setAllContainerColors(Color.TRANSPARENT)
                scrimColor = Color.TRANSPARENT
                when (BehaviourPreferences.getArcType()) {
                    PopupArcType.INURE -> {
                        setPathMotion(ArcMotion().apply {
                            maximumAngle = this.maximumAngle
                            minimumHorizontalAngle = this.minimumHorizontalAngle
                            minimumVerticalAngle = this.minimumVerticalAngle
                        })
                    }
                    PopupArcType.MATERIAL -> {
                        setPathMotion(MaterialArcMotion())
                    }
                }
            }
        }
    }

    open fun setLinearTransitions(duration: Long) {
        setTransitions()

        if (BehaviourPreferences.isArcAnimationOn()) {
            sharedElementEnterTransition = MaterialContainerTransform().apply {
                setDuration(duration)
                setAllContainerColors(Color.TRANSPARENT)
                scrimColor = Color.TRANSPARENT
            }
            sharedElementReturnTransition = MaterialContainerTransform().apply {
                setDuration(duration)
                setAllContainerColors(Color.TRANSPARENT)
                scrimColor = Color.TRANSPARENT
            }
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

    open fun showWarning(@StringRes warning: Int) {
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

    open fun openWebPage(source: String) {
        clearExitTransition()
        openFragmentSlide(WebPage.newInstance(string = source), "web_page")
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
     * If the fragment does not need to be pushed into backstack
     * leave the [tag] unattended
     *
     * @param fragment [Fragment]
     * @param tag back stack tag for fragment
     */
    protected fun openFragmentSlide(fragment: ScopedFragment, tag: String? = null) {
        clearExitTransition()

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        transaction.replace(R.id.app_container, fragment, tag)
        if (tag.isNotNull()) {
            transaction.addToBackStack(tag)
        }
        transaction.commit()
    }

    /**
     * Open fragment using linear animation for shared element
     *
     * If the fragment does not need to be pushed into backstack
     * leave the [tag] unattended
     *
     * @param fragment [Fragment]
     * @param view [View] that needs to be animated
     * @param tag back stack tag for fragment
     */
    fun openFragmentLinear(fragment: ScopedFragment, view: View, tag: String? = null, duration: Long? = null) {
        fragment.setLinearTransitions(duration ?: resources.getInteger(R.integer.animation_duration).toLong())

        requireActivity().supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addSharedElement(view, view.transitionName)
            .replace(R.id.app_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    /**
     * Open fragment using arc animation for shared element
     *
     * If the fragment does not need to be pushed into backstack
     * leave the [tag] unattended
     *
     * @param fragment [ScopedFragment]
     * @param icon [View] that needs to be animated
     * @param tag back stack tag for fragment
     */
    protected fun openFragmentArc(fragment: ScopedFragment, icon: View, tag: String? = null, duration: Long? = null) {
        fragment.setArcTransitions(duration ?: resources.getInteger(R.integer.animation_duration).toLong())

        val transaction = requireActivity().supportFragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            addSharedElement(icon, icon.transitionName)
            replace(R.id.app_container, fragment, tag)
            if (tag.isNotNull()) {
                addToBackStack(tag)
            }
        }
        transaction.commit()
    }

    protected fun openAppInfo(packageInfo: PackageInfo, icon: ImageView) {
        openFragmentArc(AppInfo.newInstance(packageInfo, icon.transitionName), icon, "app_info_${packageInfo.packageName}")
    }

    protected fun openAppSearch() {
        openFragmentSlide(Search.newInstance(true), "search")
    }
}