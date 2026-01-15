package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.os.BundleCompat
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionDescription
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.Button
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.AppOp

class AppOpState : ScopedBottomSheetFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var description: TypeFaceTextView
    private lateinit var allow: Button
    private lateinit var ignore: Button
    private lateinit var deny: Button

    private var appOp: AppOp? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_state_appop, container, false)

        name = view.findViewById(R.id.name)
        description = view.findViewById(R.id.description)
        allow = view.findViewById(R.id.allow)
        ignore = view.findViewById(R.id.ignore)
        deny = view.findViewById(R.id.deny)

        appOp = BundleCompat.getParcelable(requireArguments(), BundleConstants.appOp, AppOp::class.java) ?: AppOp()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allow.setButtonCheckedColor(allowColor)
        deny.setButtonCheckedColor(denyColor)

        name.text = appOp?.permission ?: getString(R.string.unknown)
        description.text = requireContext().getPermissionDescription(appOp?.id ?: "")
    }

    companion object {
        const val TAG = "AppOpState"

        fun newInstance(appOp: AppOp, packageInfo: PackageInfo): AppOpState {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putParcelable(BundleConstants.appOp, appOp)
            val fragment = AppOpState()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAppOpStateDialog(appOp: AppOp, packageInfo: PackageInfo): AppOpState {
            val dialog = newInstance(appOp, packageInfo)
            dialog.show(this, TAG)
            return dialog
        }

        private val allowColor = "#1F7A55".toColorInt()
        private val denyColor = "#C11007".toColorInt()
    }
}