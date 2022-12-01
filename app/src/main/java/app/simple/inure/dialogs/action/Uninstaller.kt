package app.simple.inure.dialogs.action

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.uninstallThisPackage
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.IntentConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.UninstallerViewModelFactory
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.viewmodels.dialogs.UninstallerViewModel

class Uninstaller : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var status: TypeFaceTextView

    lateinit var appUninstallObserver: ActivityResultLauncher<Intent>

    var listener: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_uninstaller, container, false)

        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.uninstall_result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ConfigurationPreferences.isUsingRoot()) {
            with(ViewModelProvider(this, UninstallerViewModelFactory(packageInfo))[UninstallerViewModel::class.java]) {
                getError().observe(viewLifecycleOwner) {
                    showError(it)
                }

                getSuccessStatus().observe(viewLifecycleOwner) {
                    when (it) {
                        "Done" -> {
                            sendUninstalledBroadcast()
                            loader.loaded()
                            status.setText(R.string.uninstalled)
                            listener?.invoke()
                        }
                        "Failed" -> {
                            loader.error()
                            status.setText(R.string.failed)
                        }
                    }
                }
            }
        } else {
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
        }
    }

    private fun sendUninstalledBroadcast() {
        val intent = Intent(IntentConstants.ACTION_APP_UNINSTALLED)
        intent.data = Uri.parse("package:${packageInfo.packageName}")
        requireContext().sendBroadcast(intent)
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Uninstaller {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Uninstaller()
            fragment.arguments = args
            return fragment
        }
    }
}