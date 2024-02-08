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
import app.simple.inure.util.MarketUtils

class Purchase : ScopedBottomSheetFragment() {

    private lateinit var close: DynamicRippleTextView
    private lateinit var playStore: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_purchase, container, false)

        playStore = view.findViewById(R.id.play_store)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playStore.setOnClickListener {
            // Open in Play Store
            MarketUtils.openAppOnPlayStore(requireContext(), AppUtils.unlockerPackageName)
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