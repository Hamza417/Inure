package app.simple.inure.dialogs.app

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
import app.simple.inure.util.ParcelUtils.serializable
import java.io.File

class InstallUpdate : ScopedBottomSheetFragment() {

    private lateinit var updateVersion: TypeFaceTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var install: DynamicRippleTextView

    private var file: File? = null
    private var function: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_update, container, false)

        updateVersion = view.findViewById(R.id.update_version)
        close = view.findViewById(R.id.close)
        install = view.findViewById(R.id.install)

        file = requireArguments().serializable(BundleConstants.file)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateVersion.text = file?.name?.substringBeforeLast(".apk")
            ?: getString(R.string.unknown)

        close.setOnClickListener {
            dismiss()
        }

        install.setOnClickListener {
            function?.invoke()
            dismiss()
        }
    }

    fun setOnInstallCallbackListener(function: () -> Unit) {
        this.function = function
    }

    companion object {
        fun newInstance(update: File): InstallUpdate {
            val args = Bundle()
            val fragment = InstallUpdate()
            args.putSerializable(BundleConstants.file, update)
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showInstallUpdate(update: File): InstallUpdate {
            val dialog = newInstance(update)
            dialog.show(this, "install_update")
            return dialog
        }
    }
}