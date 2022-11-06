package app.simple.inure.extensions.fragments

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Misc
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.inure.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils

open class ScopedDialogFragment : DialogFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * [ScopedBottomSheetFragment]'s own [ApplicationInfo] instance, needs
     * to be initialized before use
     *
     * @throws UninitializedPropertyAccessException
     */
    lateinit var packageInfo: PackageInfo

    internal val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        kotlin.runCatching {
            packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = dialog!!.window ?: return
        val displayMetrics = DisplayMetrics()

        window.attributes.windowAnimations = R.style.DialogAnimation
        window.attributes.height = FrameLayout.LayoutParams.WRAP_CONTENT
        @Suppress("deprecation")
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)

        if (BehaviourPreferences.isDimmingOn()) {
            dialog?.window?.setDimAmount(ViewUtils.getDimValue(requireContext()))
        } else {
            dialog?.window?.setDimAmount(0f)
        }

        if (BehaviourPreferences.isBlurringOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = Misc.blurRadius.toInt()
            }
        }

        window.attributes.gravity = Gravity.CENTER

        // TODO - fixe dialog height
        if (StatusBarHeight.isLandscape(requireContext())) {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 60f).toInt()
            // window.attributes.height = (displayMetrics.heightPixels * 1F / 100F * 90F).toInt()
        } else {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 85f).toInt()
            // window.attributes.height = (displayMetrics.heightPixels * 1F / 100F * 60F).toInt()
        }
    }

    override fun onResume() {
        super.onResume()
        registerSharedPreferenceChangeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPreferenceChangeListener()
    }

    /**
     * Called when any preferences is changed using [getSharedPreferences]
     *
     * Override this to get any preferences change events inside
     * the fragment
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    /**
     * Return the {@link Application} this fragment is currently associated with.
     */
    @Suppress("unused")
    protected fun requireApplication(): Application {
        return requireActivity().application
    }

    open fun openSettings() {
        openFragmentSlide(Preferences.newInstance(), "prefs_screen")
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
        (parentFragment as ScopedFragment).clearReEnterTransition()
        (parentFragment as ScopedFragment).clearExitTransition()

        requireActivity().supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(R.id.app_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    protected fun showError(error: String) {
        childFragmentManager.showError(error).setOnErrorCallbackListener {
            dismiss()
        }
    }
}