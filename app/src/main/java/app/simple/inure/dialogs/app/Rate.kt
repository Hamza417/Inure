package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.DateUtils
import app.simple.inure.util.MarketUtils

class Rate : ScopedBottomSheetFragment() {

    private lateinit var text: TypeFaceTextView
    private lateinit var sure: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var dontShowAgain: CheckBox
    private lateinit var dontShowAgainTextView: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_rate, container, false)

        text = view.findViewById(R.id.reminder_text)
        sure = view.findViewById(R.id.sure)
        close = view.findViewById(R.id.close)
        dontShowAgain = view.findViewById(R.id.show_again_checkbox)
        dontShowAgainTextView = view.findViewById(R.id.dont_show_again)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        dontShowAgain.isChecked = MainPreferences.isShowRateReminder().invert()
        text.text = getString(R.string.rate_reminder,
                              MainPreferences.getLaunchCount(),
                              DateUtils.formatDate(
                                      TrialPreferences.getFirstLaunchDate(), "dd MMM yyyy"))

        sure.setOnClickListener {
            dontShowAgain.check()
            MarketUtils.openAppOnPlayStore(requireContext(), requireContext().packageName)
        }

        close.setOnClickListener {
            dismiss()
        }

        dontShowAgain.setOnCheckedChangeListener { isChecked ->
            MainPreferences.setShowRateReminder(isChecked.invert())
        }

        dontShowAgainTextView.setOnClickListener {
            dontShowAgain.toggle(true)
        }
    }

    companion object {
        fun newInstance(): Rate {
            val args = Bundle()
            val fragment = Rate()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showRateDialog() {
            newInstance().show(this, TAG)
        }

        const val TAG = "Rate"
    }
}
