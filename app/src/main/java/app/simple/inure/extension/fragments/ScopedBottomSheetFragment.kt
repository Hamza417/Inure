package app.simple.inure.extension.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import app.simple.inure.R
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
        dialog?.window?.setDimAmount(0.3f)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        onPreferenceChanged(sharedPreferences, key)
    }

    /**
     * Called when any preferences is changed using [getSharedPreferences]
     */
    abstract fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
}