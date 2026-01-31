package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.ForceCloseViewModelFactory
import app.simple.inure.viewmodels.dialogs.ForceCloseViewModel

class ForceStop : ScopedActionDialogBottomFragment() {

    override fun getLayoutViewId(): Int {
        return R.layout.dialog_hide
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(ViewModelProvider(this, ForceCloseViewModelFactory(packageInfo))[ForceCloseViewModel::class.java]) {
            getResults().observe(viewLifecycleOwner) {

            }

            getSuccessStatus().observe(viewLifecycleOwner) {
                when (it) {
                    "Done" -> {
                        loader.loaded()
                        status.setText(R.string.closed)
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
        fun newInstance(packageInfo: PackageInfo): ForceStop {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = ForceStop()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "ForceStop"
    }
}
