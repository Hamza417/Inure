package app.simple.inure.extensions.fragments

import android.animation.ValueAnimator
import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.annotation.IntegerRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.ArcMotion
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.views.BottomMenuRecyclerView
import app.simple.inure.dialogs.app.FullVersion.Companion.showFullVersion
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.dialogs.miscellaneous.Loader
import app.simple.inure.dialogs.miscellaneous.Warning.Companion.showWarning
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupTransitionType
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.inure.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.ui.panels.AppInfo
import app.simple.inure.ui.panels.Search
import app.simple.inure.ui.panels.WebPage
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import com.google.android.material.transition.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    protected var maximumAngle = 90
    protected var minimumHorizontalAngle = 80
    protected var minimumVerticalAngle = 15

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
    private var blurAnimator: ValueAnimator? = null

    protected var bottomRightCornerMenu: BottomMenuRecyclerView? = null

    /**
     * [postponeEnterTransition] here and initialize all the
     * views in [onCreateView] with proper transition names
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlin.runCatching {
            packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        }

        postponeEnterTransition()

        /**
         * Why I am using [requireActivity.onBackPressedDispatcher] here when the activity
         * already manages fragment backstack?
         *
         * The reason is, when the fragment is popped from the backstack it transitions
         * back to the previous fragment with the same animation that was used to open
         * the fragment. However, this leads to an issue when the fragments are popped
         * quickly or before the transition is completed. This leads to screen getting
         * stuck in the middle of the transition because when the transition ends it puts
         * the closed fragment view on top of the previous fragment view. This is a workaround
         * to pop the fragment immediately when the back button is pressed and clear the
         * transition animation.
         */
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Log.d(TAG, "onCreate: onBackPressed")
            parentFragmentManager.popBackStackImmediate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requireView().clearViewTranslationCallback()
            }
            requireView().clearAnimation()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kotlin.runCatching {
            bottomRightCornerMenu = requireActivity().findViewById(R.id.bottom_menu)
        }

        animateBlur()
    }

    private fun animateBlur() {
        if (DevelopmentPreferences.get(DevelopmentPreferences.USE_BLUR_BETWEEN_PANELS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurAnimator?.cancel()
                blurAnimator = ValueAnimator.ofFloat(30F, 0F)
                blurAnimator?.addUpdateListener {
                    val value = it.animatedValue as Float
                    try {
                        if (value > 1F) {
                            requireView().setRenderEffect(
                                    RenderEffect.createBlurEffect(value, value, Shader.TileMode.CLAMP))
                        } else {
                            requireView().setRenderEffect(null)
                        }
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "animateBlur: ", e)
                    }
                }
                blurAnimator?.interpolator = LinearOutSlowInInterpolator()
                blurAnimator?.duration = 1000
                blurAnimator?.start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomRightCornerMenu?.let {
            if (it.translationY.isZero()) {
                it.visible(animate = true)
            } else {
                it.visible(animate = false)
            }
        }

        registerSharedPreferenceChangeListener()
    }

    override fun onStop() {
        bottomRightCornerMenu?.clearAnimation()
        bottomRightCornerMenu?.gone()
        blurAnimator?.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        unregisterSharedPreferenceChangeListener()
        bottomRightCornerMenu?.clear()
    }

    /**
     * Called when any preferences is changed using [getSharedPreferences]
     *
     * Override this to get any preferences change events inside
     * the fragment
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BehaviourPreferences.TRANSITION_TYPE -> {
                setTransitions()
            }
            BehaviourPreferences.ARC_TYPE -> {
                setArcTransitions(resources.getInteger(R.integer.animation_duration).toLong())
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

    private fun clearEnterTransition() {
        enterTransition = null
    }

    internal fun clearReEnterTransition() {
        reenterTransition = null
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
                    returnTransition = Fade()
                    reenterTransition = Fade()
                }
                PopupTransitionType.ELEVATION -> {
                    enterTransition = MaterialElevationScale(true)
                    exitTransition = MaterialElevationScale(false)
                    returnTransition = MaterialElevationScale(false)
                    reenterTransition = MaterialElevationScale(true)
                }
                PopupTransitionType.SHARED_AXIS_X -> {
                    enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                    returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                }
                PopupTransitionType.SHARED_AXIS_Y -> {
                    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
                    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
                }
                PopupTransitionType.SHARED_AXIS_Z -> {
                    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                }
                PopupTransitionType.THROUGH -> {
                    exitTransition = MaterialFadeThrough()
                    enterTransition = MaterialFadeThrough()
                    returnTransition = MaterialFadeThrough()
                    reenterTransition = MaterialFadeThrough()
                }
            }
        } else {
            clearTransitions()
        }
    }

    fun clearTransitions() {
        clearEnterTransition()
        clearExitTransition()
        clearReEnterTransition()
    }

    open fun setArcTransitions(duration: Long) {
        setTransitions()

        if (BehaviourPreferences.isArcAnimationOn()) {
            when (BehaviourPreferences.getArcType()) {
                PopupArcType.INURE -> {
                    sharedElementEnterTransition = MaterialContainerTransform().apply {
                        setDuration(duration)
                        setAllContainerColors(Color.TRANSPARENT)
                        scrimColor = Color.TRANSPARENT
                        setPathMotion(ArcMotion().apply {
                            maximumAngle = this.maximumAngle
                            minimumHorizontalAngle = this.minimumHorizontalAngle
                            minimumVerticalAngle = this.minimumVerticalAngle
                        })
                        isElevationShadowEnabled = false
                    }
                    sharedElementReturnTransition = MaterialContainerTransform().apply {
                        setDuration(duration)
                        setAllContainerColors(Color.TRANSPARENT)
                        scrimColor = Color.TRANSPARENT
                        setPathMotion(ArcMotion().apply {
                            maximumAngle = this.maximumAngle
                            minimumHorizontalAngle = this.minimumHorizontalAngle
                            minimumVerticalAngle = this.minimumVerticalAngle
                        })
                        isElevationShadowEnabled = false
                    }
                }
                PopupArcType.MATERIAL -> {
                    sharedElementEnterTransition = MaterialContainerTransform().apply {
                        setDuration(duration)
                        setAllContainerColors(Color.TRANSPARENT)
                        scrimColor = Color.TRANSPARENT
                        setPathMotion(MaterialArcMotion())
                        isElevationShadowEnabled = false
                    }
                    sharedElementReturnTransition = MaterialContainerTransform().apply {
                        setDuration(duration)
                        setAllContainerColors(Color.TRANSPARENT)
                        scrimColor = Color.TRANSPARENT
                        setPathMotion(MaterialArcMotion())
                        isElevationShadowEnabled = false
                    }
                }
                PopupArcType.LEGACY -> {
                    sharedElementEnterTransition = DetailsTransitionArc()
                    sharedElementReturnTransition = DetailsTransitionArc()
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

    /**
     * @param manualOverride if true, loader can be shown from anywhere
     */
    open fun showLoader(manualOverride: Boolean = false) {
        kotlin.runCatching {
            if (requireArguments().getBoolean(BundleConstants.loading)) {
                loader = Loader.newInstance()
                loader?.show(childFragmentManager, "loader")
            } else {
                if (manualOverride) {
                    loader = Loader.newInstance()
                    loader?.show(childFragmentManager, "loader")
                } else {
                    throw IllegalStateException("Loader can't be shown from here")
                }
            }
        }.getOrElse {
            if (manualOverride) {
                loader = Loader.newInstance()
                loader?.show(childFragmentManager, "loader")
            }
        }
    }

    open fun fullVersionCheck(goBack: Boolean = true): Boolean {
        return if (TrialPreferences.isAppFullVersionEnabled()) {
            true
        } else {
            childFragmentManager.showFullVersion().setFullVersionCallbacks {
                if (goBack) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            false
        }
    }

    @Throws(IllegalStateException::class)
    open fun hideLoader() {
        loader?.dismiss()
    }

    open fun showWarning(warning: String, goBack: Boolean = true) {
        if (isStateSaved) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    parentFragmentManager.showWarning(warning).setOnWarningCallbackListener {
                        if (goBack) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        } else {
            try {
                parentFragmentManager.showWarning(warning).setOnWarningCallbackListener {
                    if (goBack) {
                        try {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        } catch (e: IllegalStateException) {
                            // do nothing
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "showWarning: ", e)
            }
        }
    }

    open fun showWarning(@StringRes warning: Int, goBack: Boolean = true) {
        if (isStateSaved) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    parentFragmentManager.showWarning(warning).setOnWarningCallbackListener {
                        if (goBack) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        } else {
            parentFragmentManager.showWarning(warning).setOnWarningCallbackListener {
                if (goBack) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    open fun showError(error: String, goBack: Boolean = true) {
        if (isStateSaved) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    childFragmentManager.showError(error).setOnErrorCallbackListener {
                        if (goBack) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        } else {
            childFragmentManager.showError(error).setOnErrorCallbackListener {
                if (goBack) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    open fun showError(error: Throwable, goBack: Boolean = true) {
        if (isStateSaved) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    childFragmentManager.showError(error.stackTraceToString()).setOnErrorCallbackListener {
                        if (goBack) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        } else {
            childFragmentManager.showError(error.stackTraceToString()).setOnErrorCallbackListener {
                if (goBack) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    open fun openWebPage(source: String) {
        clearTransitions()
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

    protected fun getInteger(@IntegerRes resId: Int): Int {
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
        clearTransitions()

        try {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setReorderingAllowed(true)
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.replace(R.id.app_container, fragment, tag)
            if (tag.isNotNull()) {
                transaction.addToBackStack(tag)
            }
            transaction.commit()
        } catch (e: IllegalStateException) {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setReorderingAllowed(true)
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.replace(R.id.app_container, fragment, tag)
            if (tag.isNotNull()) {
                transaction.addToBackStack(tag)
            }
            transaction.commitAllowingStateLoss()
        }
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

        try {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setReorderingAllowed(true)
            transaction.addSharedElement(view, view.transitionName)
            transaction.replace(R.id.app_container, fragment, tag)
            if (tag.isNotNull()) {
                transaction.addToBackStack(tag)
            }
            transaction.commit()
        } catch (e: IllegalStateException) {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setReorderingAllowed(true)
            transaction.addSharedElement(view, view.transitionName)
            transaction.replace(R.id.app_container, fragment, tag)
            if (tag.isNotNull()) {
                transaction.addToBackStack(tag)
            }
            transaction.commitAllowingStateLoss()
        }
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

        //        try {
        //            (fragment.exitTransition as TransitionSet?)?.excludeTarget(icon, true)
        //        } catch (e: java.lang.ClassCastException) {
        //            (fragment.exitTransition as MaterialContainerTransform?)?.excludeTarget(icon, true)
        //        }

        try {
            val transaction = requireActivity().supportFragmentManager.beginTransaction().apply {
                setReorderingAllowed(true)
                addSharedElement(icon, icon.transitionName)
                replace(R.id.app_container, fragment, tag)
                if (tag.isNotNull()) {
                    addToBackStack(tag)
                }
            }

            transaction.commit()
        } catch (e: IllegalStateException) {
            val transaction = requireActivity().supportFragmentManager.beginTransaction().apply {
                setReorderingAllowed(true)
                addSharedElement(icon, icon.transitionName)
                replace(R.id.app_container, fragment, tag)
                if (tag.isNotNull()) {
                    addToBackStack(tag)
                }
            }

            transaction.commitAllowingStateLoss()
        }
    }

    protected fun openAppInfo(packageInfo: PackageInfo, icon: ImageView) {
        openFragmentArc(AppInfo.newInstance(packageInfo), icon, AppInfo.TAG)
    }

    protected fun openAppSearch() {
        openFragmentSlide(Search.newInstance(true), Search.TAG)
    }

    private fun getPackageInfoFromBundle(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(BundleConstants.packageInfo, PackageInfo::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable(BundleConstants.packageInfo)!!
        }
    }

    protected fun getPackageInfo(packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageUtils.flags))
        } else {
            requireContext().packageManager.getPackageInfo(packageName, PackageUtils.flags.toInt())
        }
    }

    @Suppress("unused", "UNUSED_VARIABLE")
    @RequiresApi(Build.VERSION_CODES.R)
    protected fun View.setKeyboardChangeListener() {
        val cb = object : WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
            var startBottom = 0
            var endBottom = 0

            override fun onPrepare(animation: WindowInsetsAnimation) {
                /**
                 * #1: First up, onPrepare is called which allows apps to record any
                 * view state from the current layout
                 */
                // endBottom = view.calculateBottomInWindow()
            }

            /**
             * #2: After onPrepare, the normal WindowInsets will be dispatched to
             * the view hierarchy, containing the end state. This means that your
             * view's OnApplyWindowInsetsListener will be called, which will cause
             * a layout pass to reflect the end state.
             */
            override fun onStart(animation: WindowInsetsAnimation, bounds: WindowInsetsAnimation.Bounds): WindowInsetsAnimation.Bounds {
                /**
                 * #3: Next up is onStart, which is called at the start of the animation.
                 * This allows apps to record the view state of the target or end state.
                 */
                return bounds
            }

            override fun onProgress(insets: WindowInsets, runningAnimations: List<WindowInsetsAnimation>): WindowInsets {
                /** #4: Next up is the important call: onProgress. This is called every time
                 * the insets change in the animation. In the case of the keyboard, which
                 * would be as it slides on screen.
                 */
                return insets
            }

            override fun onEnd(animation: WindowInsetsAnimation) {
                /**
                 * #5: And finally onEnd is called when the animation has finished. Use this
                 * to clear up any old state.
                 */
            }
        }
    }

    protected fun onSure(onSure: () -> Unit) {
        childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
            override fun onSure() {
                onSure()
            }
        })
    }

    protected fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    protected fun popBackStack() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    protected fun postDelayed(delay: Long, action: () -> Unit) {
        handler.postDelayed({ action() }, delay)
    }

    protected fun postDelayed(action: () -> Unit) {
        postDelayed(500, action)
    }

    protected fun removeHandlerCallbacks() {
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val TAG = "ScopedFragment"
    }
}
