package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.app.Purchase.Companion.showPurchaseDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.SharedPreferences.registerEncryptedSharedPreferencesListener
import app.simple.inure.preferences.SharedPreferences.unregisterEncryptedSharedPreferencesListener
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.util.DateUtils.toDate

class Trial : ScopedFragment() {

    private lateinit var title: TypeFaceTextView
    private lateinit var purchase: DynamicRippleTextView
    private lateinit var daysLeft: TypeFaceTextView
    private lateinit var validTill: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trial_screen, container, false)

        title = view.findViewById(R.id.trial_title)
        purchase = view.findViewById(R.id.purchase)
        daysLeft = view.findViewById(R.id.days_left)
        validTill = view.findViewById(R.id.valid_till)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        validTill.text = getValidTill()

        if (TrialPreferences.getDaysLeft() == -1) {
            title.text = Warnings.getAppIntegrityFailedWarning()
        }

        daysLeft.text = getString(R.string.days_trial_period_remaining, TrialPreferences.getDaysLeft())

        purchase.setOnClickListener {
            childFragmentManager.showPurchaseDialog()
        }
    }

    private fun getValidTill(): String {
        return TrialPreferences.getFirstLaunchDate().plus(86_400_000L * 15L).toDate()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            TrialPreferences.HAS_LICENSE_KEY -> {
                if (TrialPreferences.isFullVersion()) {
                    requireActivity().runOnUiThread {
                        validTill.text = getString(R.string.full_version_activated)
                        daysLeft.text = getString(R.string.days_trial_period_remaining, 0)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerEncryptedSharedPreferencesListener(this)
    }

    override fun onPause() {
        super.onPause()
        unregisterEncryptedSharedPreferencesListener(this)
    }

    companion object {
        fun newInstance(): Trial {
            val args = Bundle()
            val fragment = Trial()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Trial"
    }
}
