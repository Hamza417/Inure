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

class Purchase : ScopedBottomSheetFragment() {

    private lateinit var close: DynamicRippleTextView
    private lateinit var playStore: DynamicRippleTextView
    private lateinit var gumroad: DynamicRippleTextView
    private lateinit var github: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_purchase, container, false)

        playStore = view.findViewById(R.id.play_store)
        gumroad = view.findViewById(R.id.gumroad)
        github = view.findViewById(R.id.github)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playStore.setOnClickListener {
            // Open in Play Store
            MarketUtils.openAppOnPlayStore(requireContext(), AppUtils.unlockerPackageName)
        }

        gumroad.setOnClickListener {
            // Open GumRoad link in Browser
            getString(R.string.gumroad_link).asUri().openInBrowser(requireContext())
        }

        github.setOnClickListener {
            // Open GitHub link in Browser
            getString(R.string.github_link).asUri().openInBrowser(requireContext())
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(): Purchase {
            val args = Bundle()
            val fragment = Purchase()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showPurchaseDialog() {
            newInstance().show(this, "purchase")
        }
    }
}