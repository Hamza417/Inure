package app.simple.inure.ui.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.DateUtils
import app.simple.inure.util.MarketUtils

class Rate : ScopedFragment() {

    private lateinit var text: TypeFaceTextView
    private lateinit var sure: DynamicRippleTextView
    private lateinit var back: DynamicRippleTextView
    private lateinit var dontShowAgain: CheckBox
    private lateinit var dontShowAgainTextView: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rate, container, false)

        text = view.findViewById(R.id.reminder_text)
        sure = view.findViewById(R.id.sure)
        back = view.findViewById(R.id.back)
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

        back.setOnClickListener {
            goBack()
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
    }
}