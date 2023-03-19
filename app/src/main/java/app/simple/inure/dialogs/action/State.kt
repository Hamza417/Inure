package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.StateViewModelFactory
import app.simple.inure.viewmodels.dialogs.StateViewModel

class State : ScopedActionDialogBottomFragment() {

    override fun getLayoutViewId(): Int {
        return R.layout.dialog_enable_disable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(ViewModelProvider(this, StateViewModelFactory(packageInfo))[StateViewModel::class.java]) {
            getResults().observe(viewLifecycleOwner) {

            }

            getSuccessStatus().observe(viewLifecycleOwner) {
                when (it) {
                    "Done" -> {
                        loader.loaded()
                        if (requireContext().packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                            packageInfo.applicationInfo.enabled = true
                            status.setText(R.string.enabled)
                        } else {
                            packageInfo.applicationInfo.enabled = false
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
        fun newInstance(packageInfo: PackageInfo): State {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = State()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showState(packageInfo: PackageInfo): State {
            val fragment = newInstance(packageInfo)
            fragment.show(this, "state")
            return fragment
        }
    }
}