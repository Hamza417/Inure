package app.simple.inure.dialogs.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.dialog.UninstallCallbacks

class Downgrade : ScopedBottomSheetFragment() {

    private lateinit var warning: TypeFaceTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var uninstall: DynamicRippleTextView

    private var uninstallCallbacks: UninstallCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_downgrade, container, false)

        warning = view.findViewById(R.id.install_failure_warning)
        close = view.findViewById(R.id.close)
        uninstall = view.findViewById(R.id.uninstall)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        warning.text = requireArguments().getString(BundleConstants.WARNING)

        uninstall.setOnClickListener {
            uninstallCallbacks?.onUninstalled()
            dismiss()
        }

        close.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setUninstallCallbacks(callback: UninstallCallbacks) {
        uninstallCallbacks = callback
    }

    companion object {
        fun newInstance(string: String): Downgrade {
            val args = Bundle()
            args.putString(BundleConstants.WARNING, string)
            val fragment = Downgrade()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showDowngradeDialog(warning: String): Downgrade {
            val dialog = newInstance(warning)
            dialog.show(this, "install_anyway")
            return dialog
        }
    }
}