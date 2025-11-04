package app.simple.inure.dialogs.app

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.dialogs.app.Purchase.Companion.showPurchaseDialog
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.fragments.WarningCallbacks
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.ui.panels.Trial

class FullVersion : ScopedBottomSheetFragment() {

    private lateinit var trialInfo: DynamicRippleTextView
    private lateinit var purchase: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private var warningCallbacks: WarningCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_full_version, container, false)

        trialInfo = view.findViewById(R.id.trial_info)
        purchase = view.findViewById(R.id.purchase)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        trialInfo.setOnClickListener {
            dismiss()
            openFragmentSlide(Trial.newInstance(), Trial.TAG)
        }

        purchase.setOnClickListener {
            parentFragmentManager.showPurchaseDialog()
        }

        close.setOnClickListener {
            dismiss()
            if (!requireActivity().isDestroyed) {
                warningCallbacks?.onWarningDismissed()
            }
        }
    }

    fun setFullVersionCallbacks(warningCallbacks: WarningCallbacks) {
        this.warningCallbacks = warningCallbacks
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (!requireActivity().isDestroyed) {
            warningCallbacks?.onWarningDismissed()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            TrialPreferences.HAS_LICENSE_KEY -> {
                if (TrialPreferences.isAppFullVersionEnabled()) {
                    requireActivity().runOnUiThread {
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): FullVersion {
            val args = Bundle()
            val fragment = FullVersion()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showFullVersion(): FullVersion {
            val fullVersion = newInstance()
            fullVersion.show(this, "full_version")
            return fullVersion
        }
    }
}