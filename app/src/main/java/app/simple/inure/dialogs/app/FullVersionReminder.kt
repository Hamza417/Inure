package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.AppUtils
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.MarketUtils

class FullVersionReminder : ScopedBottomSheetFragment() {

    private lateinit var purchase: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private var playStore: DynamicRippleTextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_full_version_reminder, container, false)

        purchase = view.findViewById(R.id.purchase)
        close = view.findViewById(R.id.close)

        kotlin.runCatching {
            playStore = view.findViewById(R.id.play_store)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        purchase.setOnClickListener {
            if (AppUtils.isPlayFlavor()) {
                MarketUtils.openAppOnPlayStore(requireContext(), AppUtils.unlockerPackageName)
            } else if (AppUtils.isGithubFlavor()) {
                // Open GumRoad link in Browser
                getString(R.string.gumroad_link).asUri().openInBrowser(requireContext())
            }
        }

        playStore?.setOnClickListener {
            MarketUtils.openAppOnPlayStore(requireContext(), AppUtils.unlockerPackageName)
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(): FullVersionReminder {
            val args = Bundle()
            val fragment = FullVersionReminder()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showFullVersionReminder() {
            newInstance().show(this, "full_version_reminder")
        }
    }
}