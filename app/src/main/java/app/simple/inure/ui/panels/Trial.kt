package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.app.Purchase.Companion.showPurchaseDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.MainPreferences

class Trial : ScopedFragment() {

    private lateinit var title: TypeFaceTextView
    private lateinit var purchase: DynamicRippleTextView
    private lateinit var daysLeft: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trial_screen, container, false)

        title = view.findViewById(R.id.trial_title)
        purchase = view.findViewById(R.id.purchase)
        daysLeft = view.findViewById(R.id.days_left)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        if (requireArguments().getBoolean(BundleConstants.integrity)) {
            title.text = Warnings.getInureWarning05()
        }

        daysLeft.text = getString(R.string.days_trial_period_remaining, MainPreferences.getDaysLeft())

        purchase.setOnClickListener {
            childFragmentManager.showPurchaseDialog()
        }
    }

    companion object {
        fun newInstance(integrity: Boolean = false): Trial {
            val args = Bundle()
            args.putBoolean(BundleConstants.integrity, integrity)
            val fragment = Trial()
            fragment.arguments = args
            return fragment
        }
    }
}