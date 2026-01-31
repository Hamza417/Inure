package app.simple.inure.dialogs.virustotal

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.FileUtils.toFile

class ShouldUpload : ScopedBottomSheetFragment() {

    private lateinit var text: TypeFaceTextView
    private lateinit var data: TypeFaceTextView
    private lateinit var yes: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    private var shouldUploadListener: ShouldUploadListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_should_upload, container, false)

        text = view.findViewById(R.id.text)
        data = view.findViewById(R.id.data)
        yes = view.findViewById(R.id.yes)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kotlin.runCatching {
            data.text = buildString {
                append(packageInfo.safeApplicationInfo.name)
                append("(")
                append(packageInfo.versionName)
                append(")-")
                append(packageInfo.safeApplicationInfo.sourceDir.toFile().name)
                append(" ")
                append("(")
                append(packageInfo.safeApplicationInfo.sourceDir.toFile().length().toSize())
                append(")")
            }
        }.getOrElse {
            data.text = buildString {
                append(packageInfo.safeApplicationInfo.sourceDir.toFile().name)
                append(" ")
                append("(")
                append(packageInfo.safeApplicationInfo.sourceDir.toFile().length().toSize())
                append(")")
            }
        }

        yes.setOnClickListener {
            shouldUploadListener?.onYes().also {
                dismiss()
            }
        }

        close.setOnClickListener {
            dismiss().also {
                shouldUploadListener?.onClose()
            }
        }
    }

    fun setOnShouldUploadListener(shouldUploadListener: ShouldUploadListener) {
        this.shouldUploadListener = shouldUploadListener
    }

    companion object {
        private const val TAG = "ShouldUpload"

        fun newInstance(packageInfo: PackageInfo): ShouldUpload {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = ShouldUpload()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showShouldUpload(packageInfo: PackageInfo): ShouldUpload {
            val dialog = newInstance(packageInfo)
            dialog.show(this, TAG)
            return dialog
        }

        interface ShouldUploadListener {
            fun onYes()
            fun onClose()
        }
    }
}