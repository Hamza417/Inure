package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getApplicationInfo
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.HideViewModelFactory
import app.simple.inure.factories.actions.StateViewModelFactory
import app.simple.inure.viewmodels.dialogs.HideViewModel

class Hide : ScopedActionDialogBottomFragment() {

    private lateinit var hideViewModel: HideViewModel

    override fun getLayoutViewId(): Int {
        return R.layout.dialog_hide
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hideViewModelFactory = HideViewModelFactory(packageInfo)
        hideViewModel = ViewModelProvider(this, hideViewModelFactory)[HideViewModel::class.java]

        with(ViewModelProvider(this, StateViewModelFactory(packageInfo))[HideViewModel::class.java]) {
            getResults().observe(viewLifecycleOwner) {

            }

            getSuccessStatus().observe(viewLifecycleOwner) {
                when (it) {
                    "Done" -> {
                        loader.loaded()
                        if (requireContext().packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                            packageInfo.safeApplicationInfo.enabled = true
                            status.setText(R.string.enabled)
                        } else {
                            packageInfo.safeApplicationInfo.enabled = false
                            status.setText(R.string.disabled)
                        }
                        onSuccess?.invoke()
                    }
                    "Failed" -> {
                        loader.error()
                        status.setText(R.string.failed)
                    }
                }
            }

            getWarning().observe(viewLifecycleOwner) {
                showWarning(it)
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Hide {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Hide()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showHide(packageInfo: PackageInfo): Hide {
            val fragment = newInstance(packageInfo)
            fragment.show(this, TAG)
            return fragment
        }

        const val TAG = "hide"
    }
}
