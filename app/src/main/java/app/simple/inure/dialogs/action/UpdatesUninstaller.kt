package app.simple.inure.dialogs.action

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.uninstallThisPackage
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.IntentConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class UpdatesUninstaller : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var status: TypeFaceTextView

    lateinit var appUninstallObserver: ActivityResultLauncher<Intent>

    private var listener: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_uninstaller, container, false)

        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kotlin.runCatching {
            status.setText(R.string.waiting)

            appUninstallObserver = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        sendUninstalledBroadcast()
                        loader.loaded()
                        status.setText(R.string.uninstalled)
                        listener?.invoke()
                    }
                    Activity.RESULT_CANCELED -> {
                        loader.error()
                        status.setText(R.string.cancelled)
                    }
                }
            }

            packageInfo.uninstallThisPackage(appUninstallObserver)
        }.onFailure {
            loader.error()
            status.setText(R.string.failed)
        }
    }

    private fun sendUninstalledBroadcast() {
        val intent = Intent(IntentConstants.ACTION_APP_UNINSTALLED)
        intent.data = Uri.parse("package:${packageInfo.packageName}")
        requireContext().sendBroadcast(intent)
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): UpdatesUninstaller {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = UpdatesUninstaller()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showUpdatesUninstaller(packageInfo: PackageInfo, listener: (() -> Unit)? = null) {
            newInstance(packageInfo).apply {
                this.listener = listener
            }.show(this, UpdatesUninstaller::class.java.simpleName)
        }
    }
}