package app.simple.inure.extensions.fragments

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.*
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Misc
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.dialogs.miscellaneous.Warning.Companion.showWarning
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.inure.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils
import com.google.android.material.R.id.design_bottom_sheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class ScopedBottomSheetFragment : BottomSheetDialogFragment(),
                                           SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * [ScopedBottomSheetFragment]'s own [PackageInfo] instance, needs
     * to be initialized before use
     *
     * @throws UninitializedPropertyAccessException
     */
    lateinit var packageInfo: PackageInfo

    open val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomBottomSheetDialogTheme)
        kotlin.runCatching {
            packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation

        if (BehaviourPreferences.isDimmingOn()) {
            dialog?.window?.setDimAmount(ViewUtils.getDimValue(requireContext()))
        } else {
            dialog?.window?.setDimAmount(0f)
        }

        dialog?.setOnShowListener { dialog ->
            /**
             * In a previous life I used this method to get handles to the positive and negative buttons
             * of a dialog in order to change their Typeface. Good ol' days.
             */
            val sheetDialog = dialog as BottomSheetDialog

            /**
             * This is gotten directly from the source of BottomSheetDialog
             * in the wrapInBottomSheet() method
             */
            val bottomSheet = sheetDialog.findViewById<View>(design_bottom_sheet) as FrameLayout

            /**
             *  Right here!
             *  Make sure the dialog pops up being fully expanded
             */
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

            /**
             * Also make sure the dialog doesn't half close when we don't want
             * it to be, so we close them
             */
            BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // do nothing
                }
            })
        }

        if (BehaviourPreferences.isBlurringOn()) {
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dialog?.window?.attributes?.blurBehindRadius = Misc.blurRadius.toInt()
            }
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

    protected fun showError(error: String) {
        try {
            childFragmentManager.showError(error).setOnErrorCallbackListener {
                dismiss()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    protected fun showError(error: Throwable) {
        try {
            childFragmentManager.showError(error.stackTraceToString()).setOnErrorCallbackListener {
                dismiss()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * Return the {@link Application} this fragment is currently associated with.
     */
    @Suppress("unused")
    protected fun requireApplication(): Application {
        return requireActivity().application
    }

    protected fun openSettings() {
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

    open fun showWarning(warning: String) {
        childFragmentManager.showWarning(warning).setOnWarningCallbackListener {
            dismiss()
        }
    }

    open fun showWarning(@StringRes warning: Int) {
        childFragmentManager.showWarning(warning).setOnWarningCallbackListener {
            dismiss()
        }
    }
}