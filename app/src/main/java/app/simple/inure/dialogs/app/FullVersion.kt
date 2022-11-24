package app.simple.inure.dialogs.app

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.fragments.WarningCallbacks
import app.simple.inure.util.MarketUtils

class FullVersion : ScopedBottomSheetFragment() {

    private lateinit var showMe: DynamicRippleTextView
    private var warningCallbacks: WarningCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_full_version, container, false)

        showMe = view.findViewById(R.id.show_me)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        showMe.setOnClickListener {
            MarketUtils.openAppOnPlayStore(requireContext(), "app.simple.inureunlocker")
        }
    }

    fun setWarningCallbacks(warningCallbacks: WarningCallbacks) {
        this.warningCallbacks = warningCallbacks
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!requireActivity().isDestroyed) {
            warningCallbacks?.onWarningDismissed()
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