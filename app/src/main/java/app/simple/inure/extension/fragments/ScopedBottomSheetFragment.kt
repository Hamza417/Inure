package app.simple.inure.extension.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class ScopedBottomSheetFragment : BottomSheetDialogFragment(), CoroutineScope, SharedPreferences.OnSharedPreferenceChangeListener {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation

        if (AppearancePreferences.isDimmingOn()) {
            dialog?.window?.setDimAmount(0.3f)
        } else {
            dialog?.window?.setDimAmount(0f)
        }
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

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
}