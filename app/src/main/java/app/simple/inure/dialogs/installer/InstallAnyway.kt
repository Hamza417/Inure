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
import app.simple.inure.interfaces.dialog.InstallAnywayCallback

class InstallAnyway : ScopedBottomSheetFragment() {

    private lateinit var warning: TypeFaceTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var installAnyway: DynamicRippleTextView

    private var installAnywayCallback: InstallAnywayCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_install_anyway, container, false)

        warning = view.findViewById(R.id.install_failure_warning)
        close = view.findViewById(R.id.close)
        installAnyway = view.findViewById(R.id.install_anyway)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        warning.text = requireArguments().getString(BundleConstants.warning)

        installAnyway.setOnClickListener {
            installAnywayCallback?.onInstallAnyway()
            dismiss()
        }

        close.setOnClickListener {
            dismiss().also {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun setInstallAnywayCallback(callback: InstallAnywayCallback) {
        installAnywayCallback = callback
    }

    companion object {
        fun newInstance(string: String): InstallAnyway {
            val args = Bundle()
            args.putString(BundleConstants.warning, string)
            val fragment = InstallAnyway()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showInstallAnyway(warning: String): InstallAnyway {
            val dialog = newInstance(warning)
            dialog.show(this, "install_anyway")
            return dialog
        }
    }
}
